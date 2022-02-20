package com.doool.libroid.domain.model

data class LibraryModel(
    val group: String,
    val name: String,
    val version: Version,
    val updated: String,
    val url: String,
)