package com.example.private24

import com.google.gson.annotations.SerializedName

data class ExchangeRate (

    @SerializedName("baseCurrency") val baseCurrency: String? = null,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("saleRateNB") val saleRateNB: Double? = null,
    @SerializedName("purchaseRateNB") val purchaseRateNB: Double? = null,
    @SerializedName("saleRate") val saleRate: Double? = null,
    @SerializedName("purchaseRate") val purchaseRate: Double? = null
)
