package com.jordansilva.ocrscanner.ui.ocr

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jordansilva.ocrscanner.databinding.ActivityCameraOcrBinding

class CameraOcrActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCameraOcrBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraOcrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }
}