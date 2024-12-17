package com.example.festival.network

import com.google.gson.annotations.SerializedName

data class Root(
    val response: Response
)

data class Response(
    val header: Header,
    val body: Body
)

data class Header(
    val resultCode: String,
    val resultMsg: String,
    val type: String
)

data class Body(
    val items: List<Item>,
    val numOfRows: String, // 한 페이지당 출력 개수
    val pageNo: String, // 페이지 번호
    val totalCount: String // 총 데이터 개수
)

data class Item(
    @SerializedName("fstvlNm")
    val name: String, // 축제 이름
    @SerializedName("opar")
    val place: String?, // 축제 장소
    @SerializedName("fstvlStartDate")
    val startDate: String?, // 시작일
    @SerializedName("fstvlEndDate")
    val endDate: String?, // 종료일
    @SerializedName("fstvlCo")
    val content: String?, // 축제 내용
    @SerializedName("mnnstNm")
    val organizer: String?, // 주관 기관
    @SerializedName("auspcInsttNm")
    val supervisor: String?, // 주최 기관
    val homepageUrl: String?, // 홈페이지 주소
    @SerializedName("rdnmadr")
    val roadNameAdr: String?, // 도로명주소
    @SerializedName("lnmadr")
    val landNameAdr: String?,
    val latitude: String?, // 위도
    val longitude: String? // 경도
)
