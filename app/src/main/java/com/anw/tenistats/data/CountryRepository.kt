package com.anw.tenistats.data

import android.util.Log
import android.widget.TextView
import com.anw.tenistats.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class Country(
    val name: String
)

class CountryRepository{
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://raw.githubusercontent.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(CountryService::class.java)

    suspend fun getCountries(): List<Country> {
        return service.getCountries()
        //return service.getCountries().map { it.name }
    }

    interface CountryService {
        @GET("lukes/ISO-3166-Countries-with-Regional-Codes/master/all/all.json")
        suspend fun getCountries(): List<Country>
    }
}