package com.doool.feedroid.domain.model

data class LibraryModel(
    val group: String,
    val name: String,
    val version: Version,
    val updated: String,
    val url: String,
)