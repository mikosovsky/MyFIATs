package com.example.myfiats

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class LoggedLayoutActivity : AppCompatActivity() {
    private lateinit var currencyApiKeyString: String
    private val baseUrlString = "https://v6.exchangerate-api.com/v6/"
    private val baseCurrency = "PLN"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.logged_layout)
        setUp()
        fetchCurrencyData().start()
    }

    private fun setUp() {
        currencyApiKeyString = getString(R.string.currencyApiKey)
    }

    private fun fetchCurrencyData(): Thread {
        Log.d("REST API", "fetchCurrencyData()")
        return Thread {
            val urlString = baseUrlString + currencyApiKeyString + "/latest/" + baseCurrency
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            Log.d("REST API",connection.responseCode.toString())
            if(connection.responseCode == 200) {
                val inputStream = connection.inputStream
                val inputStreamReader = InputStreamReader(inputStream, "UTF-8")
            }
        }
    }

}