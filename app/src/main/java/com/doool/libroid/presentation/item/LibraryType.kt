
package com.doool.libroid.presentation.item

sealed class LibraryType {
    data class Item(val data: LibraryGroup) : LibraryType()
    object Divider : LibraryType()
}