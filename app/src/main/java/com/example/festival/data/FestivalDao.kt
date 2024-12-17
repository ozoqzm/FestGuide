package com.example.festival.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FestivalDao {
    @Insert
    suspend fun insertFestival(festival: FestivalEntity)

    @Query("SELECT * FROM festival_table")
    suspend fun getAllFestivals(): List<FestivalEntity>

    @Query("DELETE FROM festival_table")
    suspend fun deleteAllFestival()

    @Query("SELECT * FROM festival_table WHERE _id = :festivalId")
    suspend fun getFestivalById(festivalId: Int) : FestivalEntity

    // memo 수정
    @Query("UPDATE festival_table SET memo = :newMemo WHERE _id = :festivalId")
    suspend fun updateMemo(festivalId: Int, newMemo: String)

    // memo 삭제
    @Query("UPDATE festival_table SET memo = NULL WHERE _id = :festivalId")
    suspend fun deleteMemo(festivalId: Int)

    // 스크랩
    @Query("UPDATE festival_table SET scrap = 1 WHERE _id = :festivalId")
    suspend fun scrapFestival(festivalId: Int)

    // 스크랩 조회
    @Query("SELECT * FROM festival_table WHERE scrap = 1")
    suspend fun getScrapFestivals() : List<FestivalEntity>

}