package com.example.myfiats

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.net.URL

class LoggedLayoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.logged_layout)
    }

    private fun fetchCurrencyData(): Thread {
        return Thread {
            val url = URL("")
        }
    }
}