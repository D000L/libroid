package com.doool.libroid.presentation.item

import com.doool.libroid.domain.model.Version

data class Library(
    val library: String,
    val releaseVersion: Version?,
    val latestVersion: Version?
)