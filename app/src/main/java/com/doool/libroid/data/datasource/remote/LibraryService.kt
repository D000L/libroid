package com.doool.libroid.data.datasource.remote

import com.doool.libroid.data.datasource.response.Feed
import retrofit2.http.GET

interface LibraryService {

    @GET("/feeds/androidx-release-notes.xml")
    suspend fun getFeed(): Feed
}



