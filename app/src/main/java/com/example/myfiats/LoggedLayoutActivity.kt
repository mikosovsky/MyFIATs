package com.example.myfiats

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.HashMap

class LoggedLayoutActivity : AppCompatActivity() {
    // Views
    private lateinit var currenciesLinearLayout: LinearLayout
    private lateinit var currencyLayoutActivityIntent: Intent

    // To get data from previous view
    private var bundle: Bundle? = null
    private lateinit var emailString: String

    // Firestore
    private lateinit var database: FirebaseFirestore

    // Rest API data
    private lateinit var currencyApiKeyString: String
    private val baseUrlString = "https://v6.exchangerate-api.com/v6/"
    private val baseCurrency = "PLN"
    private lateinit var allCurrenciesDataModel: AllCurrenciesDataModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.logged_layout)
        setUp()
//        GlobalScope.launch(Dispatchers.IO) {
//
//            val dataModel = async { fetchCurrenciesData() }
//            for (currency in dataModel.await().conversion_rates) {
//                if (currency.key != baseCurrency) {
//                    val currencyDataModel = async { fetchCurrencyDetails(currency.key) }
//                    if (currencyDataModel.await() == null) {
//                        withContext(Dispatchers.Main) {
//                            updateUI(currency)
//                        }
//                    } else {
//                        val toSendCurrencyDataModel = currencyDataModel.await() as CurrencyDataModel
//                        val readyDataToUseModel =
//                            async { downloadImageByteArrayAndChangeObjectOfData(toSendCurrencyDataModel) }
//
//                        withContext(Dispatchers.Main) {
//                            updateUI(readyDataToUseModel.await())
//
//                        }
//                    }
//                }
//            }
//            Log.d("REST API", "END OF DOWNLOADING DATA")
//        }
    }

    // Function is setting up all things
    private fun setUp() {
        supportActionBar?.hide()
        currencyApiKeyString = getString(R.string.currencyApiKey)
        database = Firebase.firestore
        getDataFromPreviousView()
        getDataFromFirestore()
        setUpViews()
    }

    // Function to get data from previous view (email address)
    private fun getDataFromPreviousView() {
        bundle = intent.extras
        if (bundle != null) {
            val getEmailString = bundle!!.getString("email").toString()
            emailString = getEmailString
        } else {
            emailString = "xxx@xxx.pl"
        }
    }

    private fun getDataFromFirestore() {
        var favCurrencies = mutableMapOf<String, Any>()
        val docRef = database.collection("favCurrencies").document(emailString)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val getFavCurrencies = document.data
                    if (getFavCurrencies != null) {
                        favCurrencies = getFavCurrencies as MutableMap<String, Any>
                    }
                    Log.d("FIRESTORE.FAV", "DocumentSnapshot data: ${favCurrencies}")
                    if (favCurrencies.keys.size > 0) {
                        var anyCurrencyIsTrue = false
                        GlobalScope.launch(Dispatchers.IO) {
                        for (currency in favCurrencies) {
                            if (currency.value == true && anyCurrencyIsTrue == false) {
                                anyCurrencyIsTrue = true
                                val favouriteTextView = TextView(baseContext)
                                favouriteTextView.textSize = 30f
                                favouriteTextView.text = "Favourites"
                                withContext(Dispatchers.Main) {
                                    currenciesLinearLayout.addView(favouriteTextView)
                                }
                            }
                            if (currency.value == true) {

                                    val currencyDataModel = async { fetchCurrencyDetails(currency.key) }
                                    if (currencyDataModel.await() != null) {
                                        val readyCurrencyDataModel = currencyDataModel.await() as CurrencyDataModel
                                        val dataToShow = async{downloadImageByteArrayAndChangeObjectOfData(readyCurrencyDataModel)}
                                        withContext(Dispatchers.Main) {
                                            updateUI(dataToShow.await())
                                        }
                                    }
                                }
                            }

                        val allCurrenciesTextView = TextView(baseContext)
                        allCurrenciesTextView.textSize = 30f
                        allCurrenciesTextView.text = "All currencies"
                        currenciesLinearLayout.addView(allCurrenciesTextView)
                            val dataModel = async { fetchCurrenciesData() }
                            for (currency in dataModel.await().conversion_rates) {
                                if (currency.key != baseCurrency) {
                                    val currencyDataModel = async { fetchCurrencyDetails(currency.key) }
                                    if (currencyDataModel.await() == null) {
                                        withContext(Dispatchers.Main) {
                                            updateUI(currency)
                                        }
                                    } else {
                                        val toSendCurrencyDataModel = currencyDataModel.await() as CurrencyDataModel
                                        val readyDataToUseModel =
                                            async { downloadImageByteArrayAndChangeObjectOfData(toSendCurrencyDataModel) }

                                        withContext(Dispatchers.Main) {
                                            updateUI(readyDataToUseModel.await())

                                        }
                                    }
                                }
                            }
                    }}
                } else {
                    Log.d("FIRESTORE.FAV", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("FIRESTORE.FAV", "get failed with ", exception)
            }
    }

    // Function is responsible for setting up all views
    private fun setUpViews() {
        currenciesLinearLayout = findViewById(R.id.currenciesLinearLayout)
        currencyLayoutActivityIntent = Intent(this@LoggedLayoutActivity, CurrencyLayoutActivity::class.java)
    }

    // Function is responsible for get data from Rest API
    private suspend fun fetchCurrenciesData(): AllCurrenciesDataModel {
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

    private fun updateUI(readyDataToUseModel: ReadyDataToUseModel) {

        val exchangeRateFloat = 1 / readyDataToUseModel.exchangeRate
        var exchangeRateString = ""
        if (exchangeRateFloat > 0.01) {
            exchangeRateString = String.format("%.2f " + baseCurrency, exchangeRateFloat)
        } else {
            exchangeRateString = String.format("%.4f " + baseCurrency, exchangeRateFloat)
        }
        if (readyDataToUseModel.image != null) {
            val image = readyDataToUseModel.image as Bitmap
            createCurrencyInfoLayout(
                readyDataToUseModel.fullNameCurrency,
                readyDataToUseModel.shortNameCurrency,
                exchangeRateString,
                image
            )
        } else {
            createCurrencyInfoLayout(
                readyDataToUseModel.fullNameCurrency,
                readyDataToUseModel.shortNameCurrency,
                exchangeRateString
            )
        }

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

    private fun createCurrencyInfoLayout(
        fullNameString: String,
        shortNameString: String,
        exchangeRateString: String,
        bitmap: Bitmap
    ) {
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
        flagImageView.setImageBitmap(bitmap)
        currencyInfoLayoutSetOnClick(currencyInfoLayout)
        currenciesLinearLayout.addView(currencyInfoLayout)
    }

    // Function is responsible for changing view to CurrencyLayout
    private fun currencyInfoLayoutSetOnClick(currencyInfoLayout: View) {
        currencyInfoLayout.setOnClickListener {
            val shortNameCurrencyTextView = currencyInfoLayout.findViewById<TextView>(R.id.shortNameCurrencyTextView)
            val shortNameCurrencyString = shortNameCurrencyTextView.text.toString()
            currencyLayoutActivityIntent.putExtra("shortNameCurrency", shortNameCurrencyString)
            currencyLayoutActivityIntent.putExtra("email", emailString)
            startActivity(currencyLayoutActivityIntent)
        }
    }

    // Function is responsible for download details about one currency
    private suspend fun fetchCurrencyDetails(targetCurrency: String): CurrencyDataModel? {
        var currencyDataModel: CurrencyDataModel? = null
        val urlString = baseUrlString + currencyApiKeyString + "/enriched/" + baseCurrency + "/" + targetCurrency
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        if (connection.responseCode == 200) {
            val inputStream = connection.inputStream
            val inputStreamReader = InputStreamReader(inputStream, "UTF-8")
            currencyDataModel = Gson().fromJson(inputStreamReader, CurrencyDataModel::class.java)
            inputStreamReader.close()
            inputStream.close()
        }
        return currencyDataModel
    }

    private suspend fun downloadImageByteArrayAndChangeObjectOfData(currencyDataModel: CurrencyDataModel): ReadyDataToUseModel {
        val url = URL(currencyDataModel.target_data.flag_url)
        var bitmap: Bitmap? = null
        val connection = url.openConnection() as HttpURLConnection
        if (connection.responseCode == 200) {
            val inputStream = connection.inputStream
            bitmap = BitmapFactory.decodeStream(inputStream)
        }
        val fullNameCurrency = currencyDataModel.target_data.currency_name
        val shortNameCurrency = currencyDataModel.target_code
        val exchangeRate = currencyDataModel.conversion_rate
        return ReadyDataToUseModel(fullNameCurrency, shortNameCurrency, exchangeRate, bitmap)
    }
}