package com.manus.motsmagiques

import retrofit2.http.GET
import retrofit2.http.Query

interface MyMemoryApiService {
    @GET("get")
    suspend fun translate(
        @Query("q") text: String,
        @Query("langpair") langPair: String = "fr|ar"
    ): TranslationResponse
}

data class TranslationResponse(
    val responseData: ResponseData
)

data class ResponseData(
    val translatedText: String
)
