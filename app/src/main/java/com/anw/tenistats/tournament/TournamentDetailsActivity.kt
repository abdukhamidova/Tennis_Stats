package com.anw.tenistats.tournament

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.anw.tenistats.R
import com.anw.tenistats.data.CountryRepository
import com.anw.tenistats.databinding.ActivityTournamentDetailsBinding
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.google.android.material.navigation.NavigationView
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TournamentDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTournamentDetailsBinding
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var tournamentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTournamentDetailsBinding.inflate(layoutInflater);
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        
        //region ---MENU---
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val menu = findViewById<ImageButton>(R.id.buttonMenu)
        val navigationView = findViewById<NavigationView>(R.id.navigationViewMenu)
        val headerView = navigationView.getHeaderView(0)

        menu.setOnClickListener {
            drawerLayout.open()
        }
        navigationDrawerHelper = NavigationDrawerHelper(this)
        navigationDrawerHelper.setupNavigationDrawer(drawerLayout, navigationView, firebaseAuth)
        val backButton = findViewById<ImageButton>(R.id.buttonUndo)
        backButton.visibility = View.GONE

        val userEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        if (userEmail.isNotEmpty()) {
            headerView.findViewById<TextView>(R.id.textViewUserEmail).text = userEmail
        } else {
            findViewById<TextView>(R.id.textViewUserEmail).text =
                resources.getString(R.string.user_email)
        }
        //endregion

        tournamentId = intent.getStringExtra("tournament_id").toString()

        clickableOptions()
        setData()
        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Tournaments").child(tournamentId)
        
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.child("creator").value != user.toString()){ //read-only
                    binding.editTextName.isEnabled = false
                    binding.editTextCity.isEnabled = false
                    binding.autoCompleteTextViewCountry.isEnabled = false
                    binding.editTextStartDate.isEnabled = false
                    binding.editTextEndDate.isEnabled = false
                    binding.autoCompleteTextViewSurface.isEnabled = false
                    binding.editTextNote.isEnabled = false
                }
                else{
                    binding.editTextName.isEnabled = true
                    binding.editTextCity.isEnabled = true
                    binding.autoCompleteTextViewCountry.isEnabled = true
                    binding.editTextStartDate.isEnabled = true
                    binding.editTextEndDate.isEnabled = true
                    binding.autoCompleteTextViewSurface.isEnabled = true
                    binding.editTextNote.isEnabled = true
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
        binding.buttonSubmit.setOnClickListener {
            if (binding.editTextName.text.isEmpty() ||
                binding.editTextCity.text.isEmpty() ||
                binding.autoCompleteTextViewCountry.text.isEmpty() ||
                binding.editTextStartDate.text.isEmpty() ||
                binding.editTextEndDate.text.isEmpty() ||
                binding.autoCompleteTextViewSurface.selectedItem == null) {
                Toast.makeText(this, "Don't leave empty fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Zatrzymujemy dalsze przetwarzanie
            }
            val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            if((dateFormat.parse(binding.editTextStartDate.text.toString())?.time!!) > (dateFormat.parse(binding.editTextEndDate.text.toString())?.time!!)){
                Toast.makeText(this, "Wrong dates", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Zatrzymujemy dalsze przetwarzanie
            }
            updateTournament()
            startActivity(Intent(this,ViewTournamentsActivity::class.java))
        }
    }
    
    private fun clickableOptions(){
        binding.autoCompleteTextViewCountry.setOnClickListener {
            val countryRepository = CountryRepository()

            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val countries = countryRepository.getCountries()
                    val countryNames = countries.map { it.name }.toTypedArray()
                    setupAutoCompleteTextView(countries.map { it.name })

                    val adapter = ArrayAdapter(
                        applicationContext,
                        R.layout.spinner_item_stats_right,
                        countryNames
                    )
                    adapter.setDropDownViewResource(R.layout.spinner_item_stats_right)
                    binding.autoCompleteTextViewCountry.setAdapter(adapter)
                } catch (e: Exception) {
                    // Obsługa błędu
                    Log.e("CountryRepository", "Błąd podczas pobierania listy krajów", e)
                }
            }
        }
        val surfaces = arrayOf("Hard", "Clay", "Grass", "Carpet")
        val adapter = ArrayAdapter(applicationContext,R.layout.spinner_item_stats_right,surfaces)
        adapter.setDropDownViewResource(R.layout.spinner_item_stats_right)
        binding.autoCompleteTextViewSurface.adapter = adapter
        binding.autoCompleteTextViewSurface.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {}
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        binding.editTextStartDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
                    binding.editTextStartDate.setText(selectedDate)
                },
                year, month, day
            )
            datePickerDialog.show()
        }
        binding.editTextEndDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
                    binding.editTextEndDate.setText(selectedDate)
                },
                year, month, day
            )
            datePickerDialog.show()
        }
    }

    private fun setData() {
        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Tournaments").child(tournamentId)

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.textViewName.text = dataSnapshot.child("name").getValue(String::class.java)
                binding.editTextName.setText(dataSnapshot.child("name").getValue(String::class.java))
                binding.editTextCity.setText(dataSnapshot.child("place").getValue(String::class.java))
                binding.autoCompleteTextViewCountry.setText(dataSnapshot.child("country").getValue(String::class.java))

                val startDate = dataSnapshot.child("startDate").getValue(Long::class.java)
                val formattedStartDate = dateFormat.format(Date(startDate!!))
                binding.editTextStartDate.setText(formattedStartDate)

                val endDate = dataSnapshot.child("endDate").getValue(Long::class.java)
                val formattedEndDate = dateFormat.format(Date(endDate!!))
                binding.editTextEndDate.setText(formattedEndDate)

                val value = dataSnapshot.child("surface").getValue(String::class.java)
                val adapter = binding.autoCompleteTextViewSurface.adapter
                for(i in 0 until adapter.count){
                    if(value == adapter.getItem(i).toString()){
                        binding.autoCompleteTextViewSurface.setSelection(i)
                        break
                    }
                }
                binding.editTextNote.setText(dataSnapshot.child("note").getValue(String::class.java))
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun setupAutoCompleteTextView(surfaces: List<String>) {
        ArrayAdapter(this, R.layout.spinner_item_stats_right, surfaces)
        binding.autoCompleteTextViewCountry.setOnClickListener {
            binding.autoCompleteTextViewCountry.showDropDown()
        }
    }

    private fun updateTournament() {
        val user = firebaseAuth.currentUser?.uid
        var millisecondsStart: Long? = 0
        var millisecondsEnd: Long? = 0
        if (!binding.editTextStartDate.text.isNullOrEmpty()) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = dateFormat.parse(binding.editTextStartDate.text.toString()) // Sparsowanie daty
            millisecondsStart = date?.time ?: 0
        }
        if (!binding.editTextEndDate.text.isNullOrEmpty()) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = dateFormat.parse(binding.editTextEndDate.text.toString()) // Sparsowanie daty
            millisecondsEnd = date?.time ?: 0
        }
        val updates: Map<String, Any?> = mapOf(
            "name" to binding.editTextName.text.toString(),
            "place" to binding.editTextCity.text.toString(),
            "country" to binding.autoCompleteTextViewCountry.text.toString(),
            "startDate" to millisecondsStart,
            "endDate" to millisecondsEnd,
            "surface" to binding.autoCompleteTextViewSurface.selectedItem.toString(),
            "note" to binding.editTextNote.text.toString()
        )
        if (tournamentId != null) {
            database.updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("DatabaseSuccess", "Data saved successfully")
                } else {
                    Log.e("DatabaseError", "Failed to save data", task.exception)
                }
            }
        }
    }
}