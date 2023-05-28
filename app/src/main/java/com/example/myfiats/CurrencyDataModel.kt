package com.example.myfiats

class CurrencyDataModel(
    var result: String,
    var target_code: String,
    var conversion_rate: Float,
    var target_data: TargetData
)

class TargetData(
    var currency_name: String,
    var flag_url: String
)