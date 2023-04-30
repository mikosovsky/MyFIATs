package com.example.myfiats

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class RegisterLayoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_layout)
        supportActionBar?.hide()
    }
}