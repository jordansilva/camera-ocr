package com.jordansilva.ocrscanner

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jordansilva.ocrscanner.databinding.ActivityMainBinding
import com.jordansilva.ocrscanner.ui.ocr.CameraOcrActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonCameraOcr.setOnClickListener {
            val intent = Intent(this, CameraOcrActivity::class.java)
            startActivity(intent)
        }
    }
}