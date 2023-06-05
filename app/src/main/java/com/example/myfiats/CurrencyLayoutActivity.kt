package com.example.myfiats

import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Comparator



class CurrencyLayoutActivity : AppCompatActivity() {
    // Views
    private lateinit var goBackImageButton: ImageButton
    private lateinit var currencyTextView: TextView
    private lateinit var starImageButton: ImageButton
    private lateinit var lineChart: LineChart
    private lateinit var yearButton: Button
    private lateinit var monthButton: Button
    private lateinit var weekButton: Button
    private lateinit var currentExchangeRateTextView: TextView
    private lateinit var loggedLayoutActivityIntent: Intent
    // Firestore
    private lateinit var database: FirebaseFirestore
    // Data
    private var bundle: Bundle? = null
    private lateinit var currencyString: String
    private val baseCurrencyString = "PLN"
    private val baseUrlString = "https://v6.exchangerate-api.com/v6/"
    private lateinit var currencyApiKeyString: String
    private lateinit var emailString: String
    private val historyDataMap = mutableMapOf<String, Float>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.currency_layout)
        setUp()

        GlobalScope.launch(Dispatchers.IO) {
            for (daysAgo in 364 downTo 0) {
                val historyDataPair = async { fetchHistoryData(daysAgo) }
                historyDataMap.set(historyDataPair.await().first,historyDataPair.await().second)
//                Log.d("Historical data", historyDataPair.await().toString())
                if (this@CurrencyLayoutActivity.isFinishing) {
                    break
                }
            }
            historyDataMap.toSortedMap(Comparator.reverseOrder())
            Log.d("MAP",historyDataMap.toString())
            withContext(Dispatchers.Main) {
                updateUI()
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
        database = Firebase.firestore
    }

    private fun setUpViews(){
        goBackImageButton = findViewById(R.id.goBackImageButton)
        currencyTextView = findViewById(R.id.currencyTextView)
        starImageButton = findViewById(R.id.starImageButton)
        lineChart = findViewById(R.id.lineChart)
        yearButton = findViewById(R.id.yearButton)
        monthButton = findViewById(R.id.monthButton)
        weekButton = findViewById(R.id.weekButton)
        currentExchangeRateTextView = findViewById(R.id.currentExchangeRateTextView)
        loggedLayoutActivityIntent = Intent(this@CurrencyLayoutActivity, LoggedLayoutActivity::class.java)
        currencyTextView.text = currencyString
        buttonsOnClick()
    }

    private fun buttonsOnClick() {
        yearButtonOnClick()
        monthButtonOnClick()
        weekButtonOnClick()
    }

    private fun getDataFromPreviousView() {
        bundle = intent.extras
        if (bundle != null) {
            val getCurrencyString = bundle!!.getString("shortNameCurrency").toString()
            val getEmailString = bundle!!.getString("email").toString()
            emailString = getEmailString
            currencyString = getCurrencyString
        } else {
            currencyString = "PLN"
            emailString = "xxx@xxx.pl"
        }
    }

    private fun goBackImageButtonOnClick(){
        goBackImageButton.setOnClickListener {
            finish()
        }
    }

    private suspend fun fetchHistoryData(daysAgo: Int): Pair<String,Float> {
        lateinit var historyDataPair: Pair<String, Float>
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
                historyDataPair = fullDateString as String to exchangeRate
                inputStreamReader.close()
                inputStream.close()
            } else {
                val calendar2 = Calendar.getInstance()
                calendar2.add(Calendar.DATE,-(daysAgo+1))
                val fullDateDayAgoString = fullDateFormatter.format(calendar2.time)
                historyDataPair = fullDateString as String to historyDataMap[fullDateDayAgoString] as Float
            }
        return historyDataPair
    }

    private fun updateUI() {
        val today = Calendar.getInstance().time
        val formatter = SimpleDateFormat("dd-MM-yyyy")
        val date = formatter.format(today)
        val currentExchangeRateFloat = historyDataMap[date] as Float
        if ( currentExchangeRateFloat > 0.01) {
            val currentExchangeRateString = String.format("%.2f " + baseCurrencyString, currentExchangeRateFloat)
            currentExchangeRateTextView.text = currentExchangeRateString
        } else {
            val currentExchangeRateString = String.format("%.4f " + baseCurrencyString, currentExchangeRateFloat)
            currentExchangeRateTextView.text = currentExchangeRateString
        }
        setDataOnChart(364)
    }

    private fun setDataOnChart(daysAgo: Int) {
        var arrayList = ArrayList<Entry>()
        val dateStringArray = historyDataMap.keys.toTypedArray()
        val exchangeRateFloatArray = historyDataMap.values.toTypedArray()
        val startValue = exchangeRateFloatArray.size - 1 - daysAgo
        val endValue = exchangeRateFloatArray.size - 1
        for (i in startValue..endValue) {
            val entry: Entry = Entry(i.toFloat(), exchangeRateFloatArray[i])
            arrayList.add(entry)
        }
        val setComp = LineDataSet(arrayList, "Exchange rate")
        setComp.axisDependency = YAxis.AxisDependency.LEFT
        setComp.setDrawCircles(false)
        setComp.setDrawValues(false)
        val chartData = LineData(setComp)
        lineChart.data = chartData
        val xAxis = lineChart.xAxis
        xAxis.granularity = 1f
        val valueFormatter = MyValueFormatter(dateStringArray)
        xAxis.valueFormatter = valueFormatter
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.xAxis.setDrawGridLines(false)
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false
        lineChart.invalidate()
    }

    private fun setButtonActive(activeButton: Button) {
        val unactiveColor = getColor(R.color.currencyLayoutSecondColor)
        val activeColor = getColor(R.color.chosenChartColor)
        val blackTintColor = getColor(R.color.blackTintColor)
        val whiteTintColor = getColor(R.color.whiteTintColor)
        yearButton.setTextColor(blackTintColor)
        monthButton.setTextColor(blackTintColor)
        weekButton.setTextColor(blackTintColor)
        yearButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.currencyLayoutSecondColor))
        monthButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.currencyLayoutSecondColor))
        weekButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.currencyLayoutSecondColor))
        activeButton.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.chosenChartColor))
        activeButton.setTextColor(whiteTintColor)

    }

    private fun yearButtonOnClick() {
        yearButton.setOnClickListener {
            if (historyDataMap.keys.size >= 365) {
                setButtonActive(yearButton)
                setDataOnChart(364)
            }
        }
    }

    private fun monthButtonOnClick() {
        monthButton.setOnClickListener {
            if (historyDataMap.keys.size >= 365) {
                setButtonActive(monthButton)
                setDataOnChart(29)
            }
        }
    }

    private fun weekButtonOnClick() {
        weekButton.setOnClickListener {
            if (historyDataMap.keys.size >= 365) {
                setButtonActive(weekButton)
                setDataOnChart(6)
            }
        }
    }

    private fun starImageButtonOnClick() {
        starImageButton.setOnClickListener {

        }
    }
}