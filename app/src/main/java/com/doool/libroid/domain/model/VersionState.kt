package com.doool.libroid.domain.model

enum class VersionState(val order: Int) {
    Release(0), Rc(1), Beta(2), Alpha(3)
}