package com.doool.feedroid.datasource.local

import androidx.room.Entity

@Entity(primaryKeys = ["name", "version"])
data class LibraryEntity(
    val group: String,
    val name: String,
    val version: String,
    val updated: String,
    val url: String,
)