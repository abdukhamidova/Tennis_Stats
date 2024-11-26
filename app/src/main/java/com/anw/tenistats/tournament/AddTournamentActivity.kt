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
import androidx.recyclerview.widget.RecyclerView
import com.anw.tenistats.R
import com.anw.tenistats.adapter.PlayerAdapter
import com.anw.tenistats.data.CountryRepository
import com.anw.tenistats.databinding.ActivityAddTournamentBinding
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.anw.tenistats.player.PlayerView
import com.anw.tenistats.stats.ViewHistoryActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTournamentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTournamentBinding
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddTournamentBinding.inflate(layoutInflater);
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()
        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Tournaments")

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
        binding.buttonAdd.setOnClickListener {
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
            createAndSaveTournament()
            startActivity(Intent(this,ViewTournamentsActivity::class.java))
        }
    }
    private fun setupAutoCompleteTextView(surfaces: List<String>) {
        ArrayAdapter(this, R.layout.spinner_item_stats_right, surfaces)
        binding.autoCompleteTextViewCountry.setOnClickListener {
            binding.autoCompleteTextViewCountry.showDropDown()
        }
    }
    private fun createAndSaveTournament() {
        val user = firebaseAuth.currentUser?.uid
        // Generowanie unikalnego identyfikatora dla turnieju
        val tournamentId = database.push().key
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
        // Tworzenie danych turnieju
        val tournamentData = TournamentDataClass(
            tournamentId,
            binding.editTextName.text.toString(),
            binding.editTextCity.text.toString(),
            binding.autoCompleteTextViewCountry.text.toString(),
            millisecondsStart,
            millisecondsEnd,
            binding.autoCompleteTextViewSurface.selectedItem.toString(),
            binding.editTextNote.text.toString(),
            user.toString()
            )
        // Zapisywanie danych meczu do bazy danych pod unikalnym identyfikatorem turnieju
        if (tournamentId != null) {
            database.child(tournamentId.toString()).setValue(tournamentData).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("DatabaseSuccess", "Data saved successfully")
                } else {
                    Log.e("DatabaseError", "Failed to save data", task.exception)
                }
            }
        }
    }
}