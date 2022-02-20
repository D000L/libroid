package com.doool.libroid.data.datasource.response

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "feed")
data class Feed(
    @PropertyElement val updated: String,
    @Element val entry: List<Entry>,
)

@Xml(name = "entry")
data class Entry(
    @PropertyElement val title: String,
    @PropertyElement val updated: String,
    @PropertyElement val content: String
)