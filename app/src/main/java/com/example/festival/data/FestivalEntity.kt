package com.example.festival.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "festival_table")
data class FestivalEntity(
    @PrimaryKey(autoGenerate = true)
    val _id: Int,
    val name: String, // 축제 이름
    val place: String?, // 축제 장소
    val startDate: String?, // 시작일
    val endDate: String?, // 종료일
    val content: String?, // 축제 내용
    val organizer: String?, // 주관 기관
    val supervisor: String?, // 주최 기관
    val homepageUrl: String?, // 홈페이지 주소
    val roadNameAdr: String?, // 도로명
    val landNameAdr: String?, // 지번
    val latitude: String?, // 위도
    val longitude: String?, // 경도

    val memo: String?, // 메모
    val scrap: Boolean // 스크랩 여부
)
