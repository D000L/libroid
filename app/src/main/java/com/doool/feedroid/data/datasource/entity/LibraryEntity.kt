package com.doool.feedroid.data.datasource.entity

import androidx.room.Entity
import com.doool.feedroid.domain.model.LibraryModel
import com.doool.feedroid.domain.model.Version

@Entity(primaryKeys = ["name", "version"])
data class LibraryEntity(
    val group: String,
    val name: String,
    val version: String,
    val updated: String,
    val url: String,
)

fun LibraryEntity.toModel() = LibraryModel(group, name, Version.parseVersion(version), updated, url)