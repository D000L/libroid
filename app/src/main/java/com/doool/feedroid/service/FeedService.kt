package com.doool.feedroid.service

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import retrofit2.http.GET

interface FeedService {

    @GET("/feeds/androidx-release-notes.xml")
    suspend fun getFeed(): Feed
}

@Xml(name = "feed")
data class Feed(
    @PropertyElement val updated: String,
    @Element val entry: List<Entry>,
)

@Xml(name = "entry")
data class Entry(
    @PropertyElement val title : String,
    @PropertyElement val updated: String,
    @PropertyElement val content: String
)