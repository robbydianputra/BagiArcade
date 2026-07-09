package com.bagicode.games.repository.`interface`

import com.bagicode.games.repository.model.UpdateResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ApiService {
    @GET("arcade-version")
    suspend fun getAppVersion(): UpdateResponse

    companion object {
        private const val BASE_URL = "https://bagicode.free.beeceptor.com/"

        fun create(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
