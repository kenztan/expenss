package com.expenss.tracker.data.network

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.expenss.online/"

    // TypeORM serializes decimal/numeric PostgreSQL columns as strings (e.g. "1234.00").
    // This adapter handles both JSON number and JSON string for Double fields so budget
    // amounts parse correctly regardless of how TypeORM returns them.
    private val doubleDeserializer = JsonDeserializer<Double> { json, _, _ ->
        runCatching {
            val p = json.asJsonPrimitive
            if (p.isNumber) p.asDouble else p.asString.toDouble()
        }.getOrDefault(0.0)
    }

    private val gson = GsonBuilder()
        .registerTypeAdapter(Double::class.javaObjectType, doubleDeserializer)
        .registerTypeAdapter(Double::class.java, doubleDeserializer)
        .create()

    fun create(authInterceptor: AuthInterceptor): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
