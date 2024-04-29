package com.anw.tenistats

import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.app.DatePickerDialog
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import com.anw.tenistats.data.CountryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PlayerDetailsActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth=FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        val playerId = "Brad Pitt" //intent.getStringExtra("playerID").toString()
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString()).child("Players").child(playerId)

        //ustawienie poczatkowe
        start()

        findViewById<AutoCompleteTextView>(R.id.autoCompleteTextViewNationality).setOnClickListener {
            val countryRepository = CountryRepository()

            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val countries = countryRepository.getCountries()
                    val countryNames = countries.map { it.name }.toTypedArray()
                    setupAutoCompleteTextView(countries.map { it.name })

                    val adapter = ArrayAdapter(
                        applicationContext,
                        android.R.layout.simple_dropdown_item_1line,
                        countryNames
                    )
                    findViewById<AutoCompleteTextView>(R.id.autoCompleteTextViewNationality).setAdapter(adapter)
                } catch (e: Exception) {
                    // Obsługa błędu
                    Log.e("CountryRepository", "Błąd podczas pobierania listy krajów", e)
                }
            }
        }
        findViewById<EditText>(R.id.editTextDate).setOnClickListener {
            showDatePickerDialog()
        }
        findViewById<Button>(R.id.buttonSubmit).setOnClickListener {
            setValues()
        }
    }

    fun start() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val firstName = dataSnapshot.child("firstName").getValue(String::class.java)
                val lastName = dataSnapshot.child("lastName").getValue(String::class.java)

                // Ustawienie tekstu w TextView
                findViewById<TextView>(R.id.textViewName).text = "$firstName $lastName"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Obsługa błędu
            }
        })
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val nationality = dataSnapshot.child("nationality").getValue(String::class.java)

                // Ustawienie tekstu w TextView
                if(nationality!=null) {

                    // Ustawienie tekstu w TextView
                    findViewById<TextView>(R.id.autoCompleteTextViewNationality).text = nationality
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Obsługa błędu
            }
        })
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val milliseconds = dataSnapshot.child("dateOfBirth").getValue(Long::class.java)

                // Ustawienie tekstu w TextView
                if(milliseconds!=null) {
                    val date = Date(milliseconds)
                    val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                    findViewById<TextView>(R.id.editTextDate).text = formattedDate
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Obsługa błędu
            }
        })
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val ranking = dataSnapshot.child("ranking").getValue()

                // Ustawienie tekstu w TextView
                if(ranking != null) {// Ustawienie tekstu w TextView
                    findViewById<TextView>(R.id.editTextRanking).text = ranking.toString()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Obsługa błędu
            }
        })
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val strength = dataSnapshot.child("strength").getValue(String::class.java)

                // Ustawienie tekstu w TextView
                if (strength != null) {
                    findViewById<TextView>(R.id.autoCompleteTextViewStrength).text = strength
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Obsługa błędu
            }
        })
        database.child("weakness").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val weakness = dataSnapshot.getValue(String::class.java)

                    // Ustawienie tekstu w TextView
                    if (weakness != null) {
                        findViewById<TextView>(R.id.autoCompleteTextViewWeakness).text = weakness
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Obsługa błędu
            }
        })
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
                findViewById<EditText>(R.id.editTextDate).setText(selectedDate)
                //zapisanie do bazy
                /*val databaseRef = database.child("dateOfBirth")
                databaseRef.setValue(selectedDate)
                    .addOnSuccessListener {
                        // Powodzenie operacji zapisu
                        Toast.makeText(this, "Data została zapisana do bazy danych", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        // Błąd podczas operacji zapisu
                        Toast.makeText(this, "Błąd podczas zapisywania daty do bazy danych", Toast.LENGTH_SHORT).show()
                    }*/
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun setupAutoCompleteTextView(countries: List<String>) {
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextViewNationality)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, countries)
        autoCompleteTextView.setOnClickListener {
            autoCompleteTextView.showDropDown()
        }
    }

    fun isNumber(input: String): Boolean {
        return input.toIntOrNull() != null
    }

    private fun setValues(){
        val nationality = findViewById<TextView>(R.id.autoCompleteTextViewNationality).text.toString()
        val date = findViewById<TextView>(R.id.editTextDate).text
        val ranking = findViewById<TextView>(R.id.editTextRanking).text.toString()
        val strength = findViewById<TextView>(R.id.autoCompleteTextViewStrength).text.toString()
        val weakness = findViewById<TextView>(R.id.autoCompleteTextViewWeakness).text.toString()

        if(!nationality.isNullOrEmpty()){
            database.child("nationality").setValue(nationality)
        }
        if(!date.isNullOrEmpty()){
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = dateFormat.parse(date.toString()) // Sparsowanie daty

            val milliseconds = date?.time ?: 0
            database.child("dateOfBirth").setValue(milliseconds)
        }
        if(!ranking.isNullOrEmpty()){
            if(isNumber(ranking))
            {
                database.child("ranking").setValue(ranking)
            }
            else {
                Toast.makeText(this, "Enter an integer.", Toast.LENGTH_SHORT).show()
            }
        }
        if(!strength.isNullOrEmpty()){
            database.child("strength").setValue(strength)
        }
        if(!weakness.isNullOrEmpty()){
            database.child("weakness").setValue(weakness)
        }

    }
}
