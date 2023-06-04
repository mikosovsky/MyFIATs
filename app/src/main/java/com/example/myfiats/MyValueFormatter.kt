package com.example.myfiats

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

class MyValueFormatter(private val xValsDateLabel: Array<String>): ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        var returnString = xValsDateLabel[value.toInt()].replace("-",".")
        returnString = returnString.removeRange(5,10)
        return returnString
    }
}