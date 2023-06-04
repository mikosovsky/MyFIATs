package com.example.myfiats

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
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
    private lateinit var currencyApiKeyString: String
    private lateinit var emailString: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.currency_layout)
        setUp()

        GlobalScope.launch(Dispatchers.IO) {
            for (daysAgo in 0..364) {
                val historyDataMap = async { fetchHistoryData(daysAgo) }
                Log.d("Historical data", historyDataMap.await().toString())
                if (this@CurrencyLayoutActivity.isFinishing) {
                    break
                }
            }
//            Log.d("REST API", historyDataMap.await().toString())
        }

    }

    private fun setUp(){
        supportActionBar?.hide()
        getDataFromPreviousView()
        setUpViews()
        goBackImageButtonOnClick()
        currencyApiKeyString = getString(R.string.currencyApiKey)

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

    private suspend fun fetchHistoryData(daysAgo: Int): Pair<String,Float> {
        lateinit var historyDataMap: Pair<String, Float>
            val calendar = Calendar.getInstance()
            val dayFormatter = SimpleDateFormat("dd")
            val monthFormatter = SimpleDateFormat("MM")
            val yearFormatter = SimpleDateFormat("yyyy")
            val fullDateFormatter = SimpleDateFormat("dd-MM-yyyy")
            calendar.add(Calendar.DATE, -daysAgo)
            var dayString = dayFormatter.format(calendar.time)
            var monthString = monthFormatter.format(calendar.time)
            val yearString = yearFormatter.format(calendar.time)
            val fullDateString = fullDateFormatter.format(calendar.time)
            val dayInt = dayString.toInt()
            val monthInt = monthString.toInt()
            dayString = dayInt.toString()
            monthString = monthInt.toString()
            val urlString = baseUrlString + currencyApiKeyString + "/history/" + baseCurrencyString + "/" + yearString + "/" + monthString + "/" + dayString
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            if (connection.responseCode == 200) {
                val inputStream = connection.inputStream
                val inputStreamReader = InputStreamReader(inputStream,"UTF-8")
                // Have to create dataModel class ! ! !
                val allCurrenciesDataModel = Gson().fromJson(inputStreamReader, AllCurrenciesDataModel::class.java)
                val exchangeRate = 1/allCurrenciesDataModel.conversion_rates[currencyString] as Float
                historyDataMap = fullDateString as String to exchangeRate
                inputStreamReader.close()
                inputStream.close()
            }
        return historyDataMap
    }
}