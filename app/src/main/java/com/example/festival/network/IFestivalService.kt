package com.example.festival.network

import retrofit2.http.GET
import retrofit2.http.Query

interface IFestivalService {
    @GET("openapi/tn_pubr_public_cltur_fstvl_api")

    suspend fun getStoreInfo(
        @Query("type") type: String,
        @Query("ServiceKey") key: String,
        @Query("pageNo") pageNo: String,
        @Query("numOfRows") numOfRows: String = "100" // 100개씩 요청하기
    ): Root
}
