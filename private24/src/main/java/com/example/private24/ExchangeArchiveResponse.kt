package com.example.private24

import com.google.gson.annotations.SerializedName


data class ExchangeArchiveResponse (

    @SerializedName("date") val date: String? = null,
    @SerializedName("bank") val bank: String? = null,
    @SerializedName("baseCurrency") val baseCurrency: Int? = null,
    @SerializedName("baseCurrencyLit") val baseCurrencyLit: String? = null,
    @SerializedName("exchangeRate") val exchangeRate: List<ExchangeRate>? = null
)