package com.doool.feedroid.datasource.remote

import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object AppRetrofit {

    private var instance: Retrofit? = null

    private fun newInstance(): Retrofit {
        instance?.let {
            return it
        } ?: run {
            val newRetrofit = Retrofit.Builder()
                .baseUrl("https://developer.android.com")
                .addConverterFactory(
                    TikXmlConverterFactory.create(
                        TikXml.Builder().exceptionOnUnreadXml(false).build()
                    )
                )
                .client(OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }).build())
                .build()
            instance = newRetrofit
            return newRetrofit
        }
    }

    fun getFeedService(): LibraryService {
        return newInstance().create(LibraryService::class.java)
    }
}