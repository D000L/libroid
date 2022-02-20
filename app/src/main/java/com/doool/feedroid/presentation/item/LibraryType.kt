
package com.doool.feedroid.presentation.item

sealed class LibraryType {
    data class Item(val data: LibraryGroup) : LibraryType()
    object Divider : LibraryType()
}