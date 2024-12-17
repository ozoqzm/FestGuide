package com.example.festival

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.festival.data.FestivalDatabase
import com.example.festival.data.FestivalEntity
import com.example.festival.databinding.ActivityMainBinding
import com.example.festival.network.RefService
import com.example.festival.MyActivity
import com.example.festival.ui.FestivalAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    val TAG = "MainActivityTag"

    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    val database by lazy {
       FestivalDatabase.getDatabase(this)
    }

    lateinit var adapter : FestivalAdapter

    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback
    private lateinit var refService: RefService


    private lateinit var googleMap: GoogleMap
    private val mapReadyCallback = object : OnMapReadyCallback {
        override fun onMapReady(map: GoogleMap) {
            googleMap = map
            Log.d(TAG, "Google Map is ready!")
//            addMarker(LatLng(37.606537, 127.041758), "초기 위치", "날짜 칸")

            // 마커 클릭 리스너 추가
            googleMap.setOnMarkerClickListener { marker ->
                val festivalId = marker.tag as? Int // 마커의 tag에 저장된 festival id 가져오기
                festivalId?.let {
                    val intent = Intent(this@MainActivity, DetailActivity::class.java)
                    intent.putExtra("FESTIVAL_ID", festivalId)
                    startActivity(intent)
                }
                true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        refService = RefService(this)

        adapter = FestivalAdapter()
        binding.rvFestivals.adapter = adapter
        binding.rvFestivals.layoutManager = LinearLayoutManager(this)

        // 데이터 로드
        loadFestivalData()

        val mapFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map2) as SupportMapFragment
        mapFragment.getMapAsync(mapReadyCallback)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(10000)
            .setMinUpdateIntervalMillis(150000)
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val currentLocation: Location = locationResult.locations[0]
                Log.d(TAG, "위도: ${currentLocation.latitude}, " +
                        "경도: ${currentLocation.longitude}")
                val targetLoc = LatLng(currentLocation.latitude, currentLocation.longitude)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLoc, 17F))
            }
        }

        // 위치 확인 시작 버튼 누르면
        binding.btnStart.setOnClickListener {
            checkPermissions() // 먼저 퍼미션 체크!
            startLocationRequest()
        }

        binding.btnMy.setOnClickListener {
            val intent = Intent(this@MainActivity, MyActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadFestivalData() {
        val apiKey = getString(R.string.openapi_key)

        CoroutineScope(Dispatchers.Main).launch {
            val response = refService.getStores(apiKey)
            // 확인용
            Log.d(TAG, "Response: ${response.toString()}")

            if (response != null) {
                database.festivalDao().deleteAllFestival()

                for (store in response) {
                        // Room에 데이터 저장
                        val festivalEntity = FestivalEntity(
                            _id = 0,
                            name = store.name,
                            place = store.place,
                            startDate = store.startDate,
                            endDate = store.endDate,
                            content = store.content,
                            organizer = store.organizer,
                            supervisor = store.supervisor,
                            homepageUrl = store.homepageUrl,
                            roadNameAdr = store.roadNameAdr,
                            landNameAdr = store.landNameAdr,
                            latitude = store.latitude,
                            longitude = store.longitude,
                            memo = null,
                            scrap = false
                        )
                        database.festivalDao().insertFestival(festivalEntity)
                }
                addMarkersFromRoom()
            }
        }
    }
    private fun addMarkersFromRoom() {
        CoroutineScope(Dispatchers.Main).launch {
            val festivals = database.festivalDao().getAllFestivals()

            adapter.items = festivals
            adapter.notifyDataSetChanged()

            for (festival in festivals) {
                val lat = festival.latitude?.toDoubleOrNull()
                val lng = festival.longitude?.toDoubleOrNull()

                if (lat != null && lng != null) {
                    val title = festival.name
                    val date = festival.startDate
                    val festivalId = festival._id

                    // 마커 추가
                    addMarker(LatLng(lat, lng), title, date, festivalId)
                }
            }
        }
    }


    private lateinit var markerOptions: MarkerOptions
    private var centerMarker: Marker? = null

    fun addMarker(targetLoc: LatLng, title: String?, date: String?, festivalId: Int) {
        markerOptions = MarkerOptions().apply {
            position(targetLoc)
            title(title)
            snippet(date)
            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        }
        centerMarker = googleMap.addMarker(markerOptions)
//        centerMarker?.showInfoWindow()
        centerMarker?.tag = festivalId // 마커 관련 정보 저장.. 디비에서 가져올 수 있게
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback) // 화면 끄면 중지
    }

    private fun startLocationRequest() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback, Looper.getMainLooper())
    }

    // Permission 확인
    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions() ) { permissions ->
        when {
            permissions.getOrDefault(ACCESS_FINE_LOCATION, false) ->
                Log.d(TAG, "정확한 위치 사용")
            permissions.getOrDefault(ACCESS_COARSE_LOCATION, false) ->
                Log.d(TAG, "근사 위치 사용")
            else ->
                Log.d(TAG, "권한 미승인")
        }
    }

    private fun checkPermissions() {
        if ( checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            Log.d(TAG, "필요 권한 있음")
        } else {
            locationPermissionRequest.launch(
                arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
            )
        }
    }
}

