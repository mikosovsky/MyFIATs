package com.example.myfiats

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Calendar

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
    private lateinit var loggedLayoutActivityIntent: Intent
    // Data
    private var bundle: Bundle? = null
    private lateinit var currencyString: String
    private val baseCurrencyString = "PLN"
    private val baseUrlString = "https://v6.exchangerate-api.com/v6/"
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
        goBackImageButtonOnClick()
        fetchHistoryData(7)
    }

    private fun setUpViews(){
        goBackImageButton = findViewById(R.id.goBackImageButton)
        currencyTextView = findViewById(R.id.currencyTextView)
        starImageButton = findViewById(R.id.starImageButton)
        lineChart = findViewById(R.id.lineChart)
        yearButton = findViewById(R.id.yearButton)
        monthButton = findViewById(R.id.monthButton)
        weekButton = findViewById(R.id.weekButton)
        dayButton = findViewById(R.id.dayButton)
        currentExchangeRateTextView = findViewById(R.id.currentExchangeRateTextView)
        loggedLayoutActivityIntent = Intent(this@CurrencyLayoutActivity, LoggedLayoutActivity::class.java)
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

    private fun goBackImageButtonOnClick(){
        goBackImageButton.setOnClickListener {
            finish()
        }
    }

    private fun fetchHistoryData(daysAgo: Int) {
        val calendar = Calendar.getInstance()
        val dayFormatter = SimpleDateFormat("dd")
        val monthFormatter = SimpleDateFormat("MM")
        val yearFormatter = SimpleDateFormat("yyyy")
        calendar.add(Calendar.DATE,-daysAgo)
        var dayString = dayFormatter.format(calendar.time)
        var monthString = monthFormatter.format(calendar.time)
        val yearString = yearFormatter.format(calendar.time)
        val dayInt = dayString.toInt()
        val monthInt = monthString.toInt()
        dayString = dayInt.toString()
        monthString = monthInt.toString()
        Log.d("DATE",dayString)
        Log.d("DATE",monthString)
        Log.d("DATE",yearString)
    }
}