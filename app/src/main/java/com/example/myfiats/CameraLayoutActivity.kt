package com.example.myfiats

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class CameraLayoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_layout)
        setUp()
    }

    private fun setUp(){
        supportActionBar?.hide()
    }
}