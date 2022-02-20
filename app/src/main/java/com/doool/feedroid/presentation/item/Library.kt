package com.doool.feedroid.presentation.item

import com.doool.feedroid.domain.model.Version

data class Library(
    val library: String,
    val releaseVersion: Version?,
    val latestVersion: Version?
)