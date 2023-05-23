package com.example.myfiats

import android.content.Context
import android.content.Intent
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat


class CurrencyInfoLayout (context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    // Declarations of variables
    private lateinit var fullNameCurrencyTextView: TextView
    private lateinit var shortNameCurrencyTextView: TextView
    private lateinit var percentTextView: TextView
    private lateinit var exchangeRateTextView: TextView
    private lateinit var customAttributesStyle: TypedArray

    // Init function
    init {
        inflate(context, R.layout.currency_info_layout, this)
        setUpViews(attrs)
        setUpStyle()
        layoutOnClick()
    }
    // Function to set up all views
    private fun setUpViews(attrs: AttributeSet){
        fullNameCurrencyTextView = findViewById(R.id.fullNameCurrencyTextView)
        shortNameCurrencyTextView = findViewById(R.id.shortNameCurrencyTextView)
        percentTextView = findViewById(R.id.percentTextView)
        exchangeRateTextView = findViewById(R.id.exchangeRateTextView)
        customAttributesStyle = context.obtainStyledAttributes(attrs, R.styleable.CurrencyInfoLayout, 0, 0)
    }

    // Function to set attributes from xml or initiators
    private fun setUpStyle(){
        try {
            fullNameCurrencyTextView.text = customAttributesStyle.getString(R.styleable.CurrencyInfoLayout_fullNameCurrencyText)
            shortNameCurrencyTextView.text = customAttributesStyle.getString(R.styleable.CurrencyInfoLayout_shortNameCurrencyText)
            percentTextView.text = customAttributesStyle.getString(R.styleable.CurrencyInfoLayout_percentText)
            exchangeRateTextView.text = customAttributesStyle.getString(R.styleable.CurrencyInfoLayout_exchangeRateText)
            percentTextView.setTextColor(customAttributesStyle.getColor(R.styleable.CurrencyInfoLayout_upOrDownColor,ContextCompat.getColor(context,R.color.goDownColor)))
        } finally {
            customAttributesStyle.recycle()
        }
    }
    // Function setOnClickListener which showing details of currency
    private fun layoutOnClick(){
        setOnClickListener{
            val currencyLayoutActivityIntent = Intent(context.applicationContext,CurrencyLayoutActivity::class.java)
            val shortNameCurrencyString = shortNameCurrencyTextView.text.toString()
            currencyLayoutActivityIntent.putExtra("shortNameCurrency", shortNameCurrencyString)
            context.startActivity(currencyLayoutActivityIntent)
        }
    }
}