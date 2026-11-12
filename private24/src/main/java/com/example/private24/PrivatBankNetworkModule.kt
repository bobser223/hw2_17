package com.example.private24

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PrivatBankNetworkModule {

    private val BASE_URL = "https://api.privatbank.ua/p24api/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: PrivatBankApi = retrofit.create(PrivatBankApi::class.java)
}