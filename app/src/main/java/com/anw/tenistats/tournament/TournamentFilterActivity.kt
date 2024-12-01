package com.anw.tenistats.tournament

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.anw.tenistats.R
import com.anw.tenistats.databinding.ActivityTournamentFilterBinding
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.anw.tenistats.stats.ViewHistoryActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TournamentFilterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTournamentFilterBinding
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private val availableVenueList = mutableListOf<String>()
    private val availableCountryList = mutableListOf<String>()
    private val availableSurfaceList = mutableListOf<String>()
    private var selectedVenueList = mutableListOf<String>()
    private var selectedCountryList = mutableListOf<String>()
    private var selectedSurfaceList = mutableListOf<String>()
    private lateinit var spinnerVenue: Spinner
    private lateinit var spinnerCountry: Spinner
    private lateinit var spinnerSurface: Spinner

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTournamentFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()

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

        spinnerVenue = binding.spinnerVenue
        spinnerCountry = binding.spinnerCountry
        spinnerSurface = binding.spinnerSurface

        selectedVenueList = (intent.getStringArrayExtra("venueFilter") ?: emptyArray()).toMutableList()
        selectedCountryList = (intent.getStringArrayExtra("countryFilter") ?: emptyArray()).toMutableList()
        val selectedDate = intent.getLongExtra("dateFilter", 0L)
        selectedSurfaceList = (intent.getStringArrayExtra("surfaceFilter") ?: emptyArray()).toMutableList()

        if(selectedVenueList != null){
            for(selected in selectedVenueList)
                addItem(binding.linearLayoutItemsVenue,selected)
        }
        if(selectedCountryList != null){
            for(selected in selectedCountryList)
                addItem(binding.linearLayoutItemsCountry,selected)
        }
        if (selectedDate != 0L) {
            // Convert the timestamp back to a formatted date string
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(selectedDate))

            // Set the formatted date to the EditText
            binding.editTextDate.setText(formattedDate)
        }
        if(selectedSurfaceList != null){
            for(selected in selectedSurfaceList)
                addItem(binding.linearLayoutItemsSurface,selected)
        }

        setDataInSpinners()
        binding.editTextDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
                    binding.editTextDate.setText(selectedDate)
                },
                year, month, day
            )
            datePickerDialog.show()
        }
        binding.buttonDeleteDate.setOnClickListener {
            binding.editTextDate.text.clear()
        }
        spinnerVenue.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                var selected = spinnerVenue.selectedItem.toString()
                if(selected == "All"){
                    selectedVenueList.clear()
                    binding.linearLayoutItemsVenue.removeAllViews()
                }
                else if(!selectedVenueList.contains(selected)) {
                    selectedVenueList.add(selected)
                    addItem(binding.linearLayoutItemsVenue,selected)
                }
                else{
                    Toast.makeText(this@TournamentFilterActivity,"This value is already selected",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) { }
        }
        spinnerCountry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                var selected = spinnerCountry.selectedItem.toString()
                if(selected == "All"){
                    selectedCountryList.clear()
                    binding.linearLayoutItemsCountry.removeAllViews()
                }
                else if(!selectedCountryList.contains(selected)) {
                    selectedCountryList.add(selected)
                    addItem(binding.linearLayoutItemsCountry,selected)
                }
                else{
                    Toast.makeText(this@TournamentFilterActivity,"This value is already selected",Toast.LENGTH_SHORT).show()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) { }
        }
        spinnerSurface.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                var selected = spinnerSurface.selectedItem.toString()
                if(selected == "All"){
                    selectedSurfaceList.clear()
                    binding.linearLayoutItemsSurface.removeAllViews()
                }
                else if(!selectedSurfaceList.contains(selected)) {
                    selectedSurfaceList.add(selected)
                    addItem(binding.linearLayoutItemsSurface,selected)
                }
                else{
                    Toast.makeText(this@TournamentFilterActivity,"This value is already selected",Toast.LENGTH_SHORT).show()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) { }
        }


        binding.buttonSubmit.setOnClickListener {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val milliseconds = if (binding.editTextDate.text.toString().isNotEmpty()) {
                dateFormat.parse(binding.editTextDate.text.toString())?.time ?: 0
            } else {
                null  // Use 0L or some default value if the date is empty
            }
            val intent = Intent(this@TournamentFilterActivity, ViewTournamentsActivity::class.java)
            intent.putExtra("venueFilter",selectedVenueList.toTypedArray())
            intent.putExtra("countryFilter",selectedCountryList.toTypedArray())
            intent.putExtra("dateFilter",milliseconds)
            intent.putExtra("surfaceFilter",selectedSurfaceList.toTypedArray())
            startActivity(intent)
        }
    }

    private fun setDataInSpinners(){
        availableVenueList.clear(); availableCountryList.clear(); availableSurfaceList.clear()
        availableVenueList.add("All"); availableCountryList.add("All"); availableSurfaceList.add("All")
        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Tournaments")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (tournamentSnapshot in snapshot.children) {
                        val tournament = tournamentSnapshot.getValue(TournamentDataClass::class.java)
                        if(tournament != null){
                            if(!availableVenueList.contains(tournament.place)){
                                availableVenueList.add(tournament.place!!)
                            }
                            if(!availableCountryList.contains(tournament.country)){
                                availableCountryList.add(tournament.country!!)
                            }
                            if(!availableSurfaceList.contains(tournament.surface)){
                                availableSurfaceList.add(tournament.surface!!)
                            }
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Obsłużenie błędu zapytania do bazy danych
            }
        })
        setSpinners()
    }

    private fun setSpinners(){
        val adapterVenue = ArrayAdapter(this, R.layout.spinner_item_stats_right, availableVenueList)
        spinnerVenue.adapter = adapterVenue

        val adapterCountry = ArrayAdapter(this, R.layout.spinner_item_stats_right, availableCountryList)
        spinnerCountry.adapter = adapterCountry

        val adapterSurface = ArrayAdapter(this, R.layout.spinner_item_stats_right, availableSurfaceList)
        spinnerSurface.adapter = adapterSurface
    }

    private fun addItem(linearLayout: LinearLayout,value: String){
        // Inflate the CardView layout
        val inflater = LayoutInflater.from(this)
        val itemView = inflater.inflate(R.layout.item_selected_value, linearLayout, false)

        // Set the value to the TextView inside the CardView
        val textViewValue = itemView.findViewById<TextView>(R.id.textViewValue)
        textViewValue.text = value

        // Handle delete button click
        val deleteButton = itemView.findViewById<ImageView>(R.id.imageViewDelete)
        deleteButton.setOnClickListener {
            linearLayout.removeView(itemView)
            selectedVenueList.remove(value)
        }

        // Add the CardView to the LinearLayout
        linearLayout.addView(itemView)
    }
}