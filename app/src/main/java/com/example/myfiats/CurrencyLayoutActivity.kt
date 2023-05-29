package com.example.myfiats

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart

class CurrencyLayoutActivity : AppCompatActivity() {
    // Views
    private lateinit var goBackImageButton: ImageButton
    private lateinit var currencyTextView: TextView
    private lateinit var starImageButton: ImageButton
    private lateinit var lineChart: LineChart
    private lateinit var yearButton: Button
    private lateinit var monthButton: Button
    private lateinit var weekButton: Button
    private lateinit var dayButton: Button
    private lateinit var currentExchangeRateTextView: TextView
    // Data
    private var bundle: Bundle? = null
    private lateinit var currencyString: String
    private val baseCurrencyString = "PLN"
    private lateinit var emailString: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.currency_layout)
        setUp()
    }

    private fun setUp(){
        supportActionBar?.hide()
        getDataFromPreviousView()
        setUpViews()
    }

    private fun setUpViews(){
        goBackImageButton = findViewById(R.id.goBackImageButton)
        Log.d("VIEW","goBackButton")
        currencyTextView = findViewById(R.id.currencyTextView)
        Log.d("VIEW","currencyTextView")
        starImageButton = findViewById(R.id.starImageButton)
        Log.d("VIEW","starButton")
        lineChart = findViewById(R.id.lineChart)
        yearButton = findViewById(R.id.yearButton)
        Log.d("VIEW","yearButton")
        monthButton = findViewById(R.id.monthButton)
        Log.d("VIEW","monthButton")
        weekButton = findViewById(R.id.weekButton)
        Log.d("VIEW","weekButton")
        dayButton = findViewById(R.id.dayButton)
        Log.d("VIEW","dayButton")
        currentExchangeRateTextView = findViewById(R.id.currentExchangeRateTextView)
        Log.d("VIEW","currentExchangeRateTextView")
        currencyTextView.text = currencyString
    }

    private fun getDataFromPreviousView() {
        bundle = intent.extras
        if (bundle != null) {
            val getCurrencyString = bundle!!.getString("shortNameCurrency").toString()
            currencyString = getCurrencyString
        } else {
            currencyString = "PLN"
        }

    }
}