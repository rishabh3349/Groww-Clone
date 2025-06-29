// RetrofitInstance.kt
package com.rs.groww.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import com.rs.groww.util.Constants.API_KEY

object RetrofitInstance {
    private const val BASE_URL = "https://www.alphavantage.co/"
//    private const val API_KEY = "demo"
    private const val API_KEY = "4BUDGEZSOZXXR3Z5"
    private val gson = GsonBuilder()
        .registerTypeAdapter(TopMoversResponse::class.java, TopMoversDeserializer())
        .create()
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
    fun getApiKey() = API_KEY
}
