package com.example.festival

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.festival.data.FestivalDatabase
import com.example.festival.databinding.ActivityDetailBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity(), OnMapReadyCallback {

    val binding by lazy {
        ActivityDetailBinding.inflate(layoutInflater)
    }

    val database by lazy {
        FestivalDatabase.getDatabase(this)
    }

    private lateinit var googleMap: GoogleMap
    private lateinit var memoEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map2) as SupportMapFragment?

        if (mapFragment == null) {
            val fragment = SupportMapFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .replace(R.id.map2, fragment)
                .commit()
            fragment.getMapAsync(this)
        } else {
            mapFragment.getMapAsync(this)
        }

        // 인텐트로 받은 festivalId
        val festivalId = intent.getIntExtra("FESTIVAL_ID", 0)


        val festivalNameTextView = binding.tvFestivalName
        val festivalDescriptionTextView = binding.tvFestivalDescription
        val startDateTextView = binding.tvStartDate
        val endDateTextView = binding.tvEndDate
        val homepageUrlTextView = binding.tvHomepageUrl
        val roadNameAddressTextView = binding.tvRoadNameAddress

        memoEditText = binding.etWord

        if (festivalId != 0) {
            CoroutineScope(Dispatchers.Main).launch {
                val festival = database.festivalDao().getFestivalById(festivalId)

                // 축제 정보들을 각 TextView에 설정
                festivalNameTextView.text = festival.name
                festivalDescriptionTextView.text = festival.content
                startDateTextView.text = "시작일: ${festival.startDate}"
                endDateTextView.text = "종료일: ${festival.endDate}"
                homepageUrlTextView.text = "홈페이지: ${festival.homepageUrl}"
                roadNameAddressTextView.text = "도로명주소: ${festival.roadNameAdr}"

                memoEditText.setText(festival.memo)

                val lat = festival.latitude?.toDoubleOrNull()
                val lng = festival.longitude?.toDoubleOrNull()

                if (lat != null && lng != null) {
                    val festivalLocation = LatLng(lat, lng)
                    googleMap.addMarker(MarkerOptions().position(festivalLocation).title(festival.name))
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(festivalLocation, 15f))
                }
            }
        }

        binding.btnSave.setOnClickListener {
            // 메모 수정 저장
            val updatedMemo = memoEditText.text.toString()
            if (festivalId != 0) {
                CoroutineScope(Dispatchers.Main).launch {
                    database.festivalDao().updateMemo(festivalId, updatedMemo)
                }
            }
        }

        binding.btnDelete.setOnClickListener {
            if (festivalId != 0) {
                CoroutineScope(Dispatchers.Main).launch {
                    database.festivalDao().deleteMemo(festivalId)
                    memoEditText.setText("")
                }
            }
        }

        binding.btnScrap.setOnClickListener {
            if (festivalId != 0) {
                CoroutineScope(Dispatchers.Main).launch {
                    database.festivalDao().scrapFestival(festivalId)
                }
            }
        }
    }

    // 구글 맵 준비 시....
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }
}
