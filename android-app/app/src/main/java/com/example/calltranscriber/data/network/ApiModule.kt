package com.example.calltranscriber.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiModule {
    fun moshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun okHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    inline fun <T> create(
        baseUrl: String,
        crossinline builder: Retrofit.Builder.() -> Retrofit.Builder,
        crossinline init: Retrofit.Builder.() -> Retrofit,
        factory: T,
    ): T = Retrofit.Builder()
        .apply(builder)
        .baseUrl(baseUrl)
        .client(okHttp())
        .addConverterFactory(MoshiConverterFactory.create(moshi()))
        .build()
        .init()
        .create(factory::class.java)
}
