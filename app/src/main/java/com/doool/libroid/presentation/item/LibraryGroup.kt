package com.doool.libroid.presentation.item

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class LibraryGroup(
    val group: String = "",
    val items: List<Library> = emptyList(),
) {
    var bookmarked by mutableStateOf(false)
    var opened by mutableStateOf(false)
}