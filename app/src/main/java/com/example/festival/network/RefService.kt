package com.example.festival.network
import android.content.Context
import android.util.Log
import com.example.festival.R
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.concurrent.TimeUnit


class RefService(val context: Context) {
    val TAG = "RefService"
    val festivalService: IFestivalService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(context.resources.getString(R.string.openapi_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        festivalService = retrofit.create(IFestivalService::class.java)
    }

    suspend fun getStores(key: String): List<Item>? {
        val allItems = mutableListOf<Item>()
        var currentPage = 1
        var totalCount = 1

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val currentMonth = String.format("%04d-%02d", year, month)  // yyyy-MM 형식

        // 페이지 반복
        while (allItems.size < totalCount) {
            val root: Root = festivalService.getStoreInfo("json", key, currentPage.toString())

            root.response.body.items?.let {
                allItems.addAll(it)
            }
            totalCount = root.response.body.totalCount.toInt()
            currentPage++  // 다음 페이지
        }

        // 이번 달 데이터만 뽑기
        return allItems.filter {
            it.startDate?.startsWith(currentMonth) == true // 2024-12
        }
    }

}
