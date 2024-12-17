package com.example.festival

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.festival.data.FestivalDatabase
import com.example.festival.data.FestivalEntity
import com.example.festival.databinding.ActivityMyBinding
import com.example.festival.ui.FestivalAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class MyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyBinding
    val REQUEST_THUMBNAIL_CAPTURE = 1

    private val festivalAdapter = FestivalAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        loadScrappedFestivals()

        binding.btnThumbnail.setOnClickListener {
            dispatchTakeThumbnailIntent()
        }
    }

    private fun setupRecyclerView() {
        binding.rvFestivals.apply {
            layoutManager = LinearLayoutManager(this@MyActivity)
            adapter = festivalAdapter
        }
    }

    private fun loadScrappedFestivals() {
        lifecycleScope.launch {
            val scrapFestivals = withContext(Dispatchers.IO) {
                val database = FestivalDatabase.getDatabase(this@MyActivity)
                database.festivalDao().getScrapFestivals()
            }
            festivalAdapter.items = scrapFestivals
            festivalAdapter.notifyDataSetChanged()
        }
    }

    private fun dispatchTakeThumbnailIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_THUMBNAIL_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_THUMBNAIL_CAPTURE -> {
                if (resultCode == RESULT_OK) {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    // 이미지 저장
                    saveBitmap(imageBitmap)
                    binding.imageView.setImageBitmap(imageBitmap)
                }
            }
        }
    }

    lateinit var currentPhotoPath: String
    var currentPhotoFileName: String? = null

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES) // 외부저장소 앱 전용 폴더

        val file = File("${storageDir?.path}/${timeStamp}.jpg")

        currentPhotoFileName = file.name
        currentPhotoPath = file.absolutePath
        return file
    }

    private fun saveBitmap(bitmap: Bitmap) {
        try {
            val file = createImageFile()
            // Bitmap 파일로 저장
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            currentPhotoPath = file.absolutePath
            setPic()

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun setPic() {
        Glide.with(this)
            .load(File(currentPhotoPath))
            .into(binding.imageView)
    }

}
