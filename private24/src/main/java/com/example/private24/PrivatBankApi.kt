package com.example.private24


import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface PrivatBankApi {
    @GET("exchange_rates?json")
    fun getCurrencyExchange(
        @Query("date") date: String
    ): Call<ExchangeArchiveResponse>
}
