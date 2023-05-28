package com.example.myfiats

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class LoggedLayoutActivity : AppCompatActivity() {
    // Views
    private lateinit var currenciesLinearLayout: LinearLayout

    // Rest API data
    private lateinit var currencyApiKeyString: String
    private val baseUrlString = "https://v6.exchangerate-api.com/v6/"
    private val baseCurrency = "PLN"
    private lateinit var allCurrenciesDataModel: AllCurrenciesDataModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.logged_layout)
        setUp()
        GlobalScope.launch(Dispatchers.IO) {
            val dataModel = async { fetchCurrenciesData() }
            Log.d("REST API",dataModel.await().conversion_rates.size.toString())
            for (currency in dataModel.await().conversion_rates) {
                if (currency.key != baseCurrency) {
                    val currencyDataModel = async { fetchCurrencyDetails(currency.key) }
                    if (currencyDataModel.await() == null) {
                        withContext(Dispatchers.Main) {
                            updateUI(currency)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            val toSendCurrencyDataModel = currencyDataModel.await() as CurrencyDataModel
                            updateUI(toSendCurrencyDataModel)
                        }
                    }
                }
            }
        }
    }

    // Function is setting up all things
    private fun setUp() {
        supportActionBar?.hide()
        currencyApiKeyString = getString(R.string.currencyApiKey)
        setUpViews()
    }

    // Function is responsible for setting up all views
    private fun setUpViews() {
        currenciesLinearLayout = findViewById(R.id.currenciesLinearLayout)
    }

    // Function is responsible for get data from Rest API
    private suspend fun fetchCurrenciesData(): AllCurrenciesDataModel {
        Log.d("REST API", "fetchCurrencyData()")
        val urlString = baseUrlString + currencyApiKeyString + "/latest/" + baseCurrency
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        Log.d("REST API", connection.responseCode.toString())
        if (connection.responseCode == 200) {
            val inputStream = connection.inputStream
            val inputStreamReader = InputStreamReader(inputStream, "UTF-8")
            allCurrenciesDataModel = Gson().fromJson(inputStreamReader, AllCurrenciesDataModel::class.java)
            inputStreamReader.close()
            inputStream.close()
        }
        return allCurrenciesDataModel
    }

    // Function is responsible for update UI with all currencies data (add all CurrencyInfoLayout)
    private fun updateUI(currency: Map.Entry<String, Float>) {

            val exchangeRateFloat = 1 / currency.value
            var exchangeRateString = ""
            if (exchangeRateFloat > 0.01) {
                exchangeRateString = String.format("%.2f " + baseCurrency, exchangeRateFloat)
            } else {
                exchangeRateString = String.format("%.4f " + baseCurrency, exchangeRateFloat)
            }
            createCurrencyInfoLayout("", currency.key, exchangeRateString)

    }

    private fun updateUI(currencyDataModel: CurrencyDataModel) {

            val exchangeRateFloat = 1 / currencyDataModel.conversion_rate
            var exchangeRateString = ""
            if (exchangeRateFloat > 0.01) {
                exchangeRateString = String.format("%.2f " + baseCurrency, exchangeRateFloat)
            } else {
                exchangeRateString = String.format("%.4f " + baseCurrency, exchangeRateFloat)
            }
            createCurrencyInfoLayout(currencyDataModel.target_data.currency_name, currencyDataModel.target_code, exchangeRateString)

    }

    // Function is responsible for create CurrencyInfoLayout for one currency
    private fun createCurrencyInfoLayout(fullNameString: String, shortNameString: String, exchangeRateString: String) {
        val currencyInfoLayout =
            LayoutInflater.from(this).inflate(R.layout.currency_info_layout, currenciesLinearLayout, false)
        val fullNameCurrencyTextView = currencyInfoLayout.findViewById<TextView>(R.id.fullNameCurrencyTextView)
        fullNameCurrencyTextView.text = fullNameString
        val shortnameCurrencyTextView = currencyInfoLayout.findViewById<TextView>(R.id.shortNameCurrencyTextView)
        shortnameCurrencyTextView.text = shortNameString
        val exchangeRateTextView = currencyInfoLayout.findViewById<TextView>(R.id.exchangeRateTextView)
        exchangeRateTextView.text = exchangeRateString
        val percentTextView = currencyInfoLayout.findViewById<TextView>(R.id.percentTextView)
        percentTextView.visibility = View.INVISIBLE
        val flagImageView = currencyInfoLayout.findViewById<ImageView>(R.id.flagImageView)
        flagImageView.visibility = View.INVISIBLE
        currencyInfoLayoutSetOnClick(currencyInfoLayout)
        currenciesLinearLayout.addView(currencyInfoLayout)
    }

    // Function is responsible for changing view to CurrencyLayout
    private fun currencyInfoLayoutSetOnClick(currencyInfoLayout: View) {
        currencyInfoLayout.setOnClickListener {
            val currencyLayoutActivityIntent = Intent(applicationContext, CurrencyLayoutActivity::class.java)
            val shortNameCurrencyTextView = currencyInfoLayout.findViewById<TextView>(R.id.shortNameCurrencyTextView)
            val shortNameCurrencyString = shortNameCurrencyTextView.text.toString()
            currencyLayoutActivityIntent.putExtra("shortNameCurrency", shortNameCurrencyString)
            startActivity(currencyLayoutActivityIntent)
        }
    }

    // Function is responsible for download details about one currency
    private suspend fun fetchCurrencyDetails(targetCurrency: String): CurrencyDataModel?{
        Log.d("REST API", "fetchCurrencyDetails()")
        var currencyDataModel: CurrencyDataModel? = null
        val urlString = baseUrlString + currencyApiKeyString + "/enriched/" + baseCurrency + "/" + targetCurrency
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        Log.d("REST API", connection.responseCode.toString())
        if (connection.responseCode == 200) {
            val inputStream = connection.inputStream
            val inputStreamReader = InputStreamReader(inputStream, "UTF-8")
            currencyDataModel = Gson().fromJson(inputStreamReader, CurrencyDataModel::class.java)
            inputStreamReader.close()
            inputStream.close()
        }
        return currencyDataModel
    }
}