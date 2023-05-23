package com.example.myfiats

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat


class CurrencyInfoLayout (context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private lateinit var fullNameCurrencyTextView: TextView
    private lateinit var shortNameCurrencyTextView: TextView
    private lateinit var percentTextView: TextView
    private lateinit var exchangeRateTextView: TextView

    init {
        inflate(context, R.layout.currency_info_layout, this)

        val customAttributesStyle = context.obtainStyledAttributes(attrs, R.styleable.CurrencyInfoLayout, 0, 0)

        fullNameCurrencyTextView = findViewById(R.id.fullNameCurrencyTextView)
        shortNameCurrencyTextView = findViewById(R.id.shortNameCurrencyTextView)
        percentTextView = findViewById(R.id.percentTextView)
        exchangeRateTextView = findViewById(R.id.exchangeRateTextView)

        try {
            fullNameCurrencyTextView.text = customAttributesStyle.getString(R.styleable.CurrencyInfoLayout_fullNameCurrencyText)
            shortNameCurrencyTextView.text = customAttributesStyle.getString(R.styleable.CurrencyInfoLayout_shortNameCurrencyText)
            percentTextView.text = customAttributesStyle.getString(R.styleable.CurrencyInfoLayout_percentText)
            exchangeRateTextView.text = customAttributesStyle.getString(R.styleable.CurrencyInfoLayout_exchangeRateText)
            percentTextView.setTextColor(customAttributesStyle.getColor(R.styleable.CurrencyInfoLayout_upOrDownColor,ContextCompat.getColor(context,R.color.goDownColor)))
        } finally {
            customAttributesStyle.recycle()
        }

        setOnClickListener{
            val currencyLayoutActivityIntent = Intent(context.applicationContext,CurrencyLayoutActivity::class.java)
            val shortNameCurrencyString = shortNameCurrencyTextView.text.toString()
            currencyLayoutActivityIntent.putExtra("shortNameCurrency", shortNameCurrencyString)
            context.startActivity(currencyLayoutActivityIntent)
        }
    }
}