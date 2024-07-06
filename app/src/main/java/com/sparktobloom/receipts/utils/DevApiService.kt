package com.sparktobloom.receipts.utils

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object DevApiService {
    private const val BASE_URL = "https://47.160.170.247:8000/receipts/"

    fun getService(context: Context): ApiConsumer {

        val client: OkHttpClient = DevOkHttpClient.getDevOkHttpClient(context)
            .newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val builder: Retrofit.Builder = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())

        val retrofit: Retrofit = builder.build()
        return retrofit.create(ApiConsumer::class.java)
    }
}