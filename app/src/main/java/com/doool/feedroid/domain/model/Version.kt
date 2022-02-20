package com.doool.feedroid.domain.model

data class Version(val number: String, val state: VersionState, val code: Int?) :
    Comparable<Version> {

    override fun compareTo(other: Version): Int {
        return if (number == other.number) {
            if (state == other.state) compareValues(other.code, code)
            else compareValues(state.order, other.state.order)
        } else compareValues(other.number, number)
    }

    companion object {
        fun parseVersion(version: String): Version {
            val number = version.split("-")[0]

            val (state, code) = version.split("-").getOrNull(1)?.let { string ->
                val state = VersionState.values().first { string.contains(it.name.lowercase()) }
                val code = string.removePrefix(state.name.lowercase()).toInt()
                Pair(state, code)
            } ?: Pair(VersionState.Release, null)

            return Version(number, state, code)
        }
    }

    override fun toString(): String {
        return if (state == VersionState.Release) number
        else "%s-%s%02d".format(number, state, code)
    }
}