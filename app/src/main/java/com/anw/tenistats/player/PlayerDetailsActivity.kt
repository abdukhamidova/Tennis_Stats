package com.anw.tenistats.player

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.anw.tenistats.R
import com.anw.tenistats.data.CountryRepository
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale



class PlayerDetailsActivity : AppCompatActivity() {
    private lateinit var playerId: String
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var database: DatabaseReference
    private lateinit var databaseT: DatabaseReference

    @OptIn(DelicateCoroutinesApi::class)
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

        //MENU
        firebaseAuth = FirebaseAuth.getInstance()
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.navigationViewMenu)
        val menu = findViewById<ImageButton>(R.id.buttonMenu)
        val headerView = navigationView.getHeaderView(0)
        menu.setOnClickListener{
            drawerLayout.open()
        }
        navigationDrawerHelper = NavigationDrawerHelper(this)
        navigationDrawerHelper.setupNavigationDrawer(drawerLayout, navigationView, firebaseAuth)
        val backButton = findViewById<ImageButton>(R.id.buttonUndo)
        backButton.setImageResource(R.drawable.icon_delete_30)


        val userEmail = firebaseAuth.currentUser?.email.toString()
        val userEmailView = headerView.findViewById<TextView>(R.id.textViewUserEmail)
        if(userEmail.isNotEmpty()) {
            userEmailView.text = userEmail
        }else {
            userEmailView.text = resources.getString(R.string.user_email)
        }
        //MENU

        val user = firebaseAuth.currentUser?.uid
        playerId = intent.getStringExtra("playerId").toString()
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString()).child("Players").child(playerId)

        val starImageButton=findViewById<ImageButton>(R.id.buttonStar)
        starImageButton.visibility = View.VISIBLE
        database.child("isFavorite")
            .get()
            .addOnSuccessListener { snapshot ->
                val isFavoriteInDatabase = snapshot.getValue(Boolean::class.java) ?: false
                if (isFavoriteInDatabase) {
                    starImageButton.setImageResource(R.mipmap.star_full)
                } else {
                    starImageButton.setImageResource(R.mipmap.star)
                }
            }
        databaseT = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString()).child("Teams").child("Favorites")
        starImageButton.setOnClickListener {
            database.child("isFavorite").get().addOnSuccessListener { snapshot ->
                val isFavorite = snapshot.getValue(Boolean::class.java) ?: false
                val favoriteTeamRef = databaseT.child("players")

                favoriteTeamRef.get().addOnSuccessListener { teamSnapshot ->
                    val playersList = teamSnapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {}) ?: mutableListOf()

                    if (isFavorite) {
                        playersList.remove(playerId)
                        Toast.makeText(this, "$playerId removed from favorites", Toast.LENGTH_SHORT).show()
                    } else {
                        playersList.add(playerId)
                        Toast.makeText(this, "$playerId added to favorites", Toast.LENGTH_SHORT).show()
                    }

                    favoriteTeamRef.setValue(playersList)
                    database.child("isFavorite").setValue(!isFavorite)
                    starImageButton.setImageResource(if (!isFavorite) R.mipmap.star_full else R.mipmap.star)
                }
            }
        }


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
        findViewById<ImageButton>(R.id.buttonUndo).setOnClickListener {
            val deletePlayerDialog = DeletePlayerActivity(this)
            deletePlayerDialog.show(playerId)
        }

        findViewById<Button>(R.id.buttonSubmit).setOnClickListener {
            setValues()
            Toast.makeText(
                applicationContext,
                "Updated player",
                Toast.LENGTH_SHORT
            ).show()
            startActivity(Intent(this, ViewPlayerActivity::class.java))
        }
    }

    fun start() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                /*val firstName = dataSnapshot.child("firstName").getValue(String::class.java)
                val lastName = dataSnapshot.child("lastName").getValue(String::class.java)*/
                val Name = dataSnapshot.child("player").getValue(String::class.java)

                // Ustawienie tekstu w TextView
                //findViewById<TextView>(R.id.textViewName).text = "$firstName $lastName"
                findViewById<TextView>(R.id.textViewName).text = Name
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
                val handedness = dataSnapshot.child("handedness").getValue()

                // Ustawienie tekstu w TextView
                if(handedness == "Righthanded") {// Ustawienie tekstu w TextView
                    findViewById<RadioButton>(R.id.radioButtonR).isChecked = true
                }
                else if(handedness == "Lefthanded") {// Ustawienie tekstu w TextView
                    findViewById<RadioButton>(R.id.radioButtonL).isChecked = true
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
        database.child("team").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val team = dataSnapshot.getValue(String::class.java)

                    if (team != null) {
                        val teamLayout = findViewById<LinearLayout>(R.id.linearLayoutTeam)
                        teamLayout.visibility = View.VISIBLE
                        val teamText = findViewById<TextView>(R.id.TextViewTeam)
                        teamText.text = team
                    }
                }else {
                    val addTeamLayout = findViewById<LinearLayout>(R.id.linearLayoutAddTeam)
                    addTeamLayout.visibility = View.VISIBLE
                    val textViewAdd=findViewById<TextView>(R.id.textViewAddToTeam)
                    textViewAdd.setOnClickListener {
                        val decisionDialog = DecisionAddToTeamActivity(this@PlayerDetailsActivity)
                        decisionDialog.show(playerId)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any potential database error here
                Log.e("DatabaseError", "Error fetching team data: ${databaseError.message}")
            }
        })

        database.child("note").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val notes = dataSnapshot.getValue(String::class.java)

                    // Ustawienie tekstu w TextView
                    if (notes != null) {
                        findViewById<TextView>(R.id.editTextNote).text = notes
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
            R.style.CustomDatePickerDialog,
            DatePickerDialog.OnDateSetListener { view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
                findViewById<EditText>(R.id.editTextDate).setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun setupAutoCompleteTextView(countries: List<String>) {
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextViewNationality)
        ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, countries)
        autoCompleteTextView.setOnClickListener {
            autoCompleteTextView.showDropDown()
        }
    }

    private fun setValues(){
        val nationality = findViewById<TextView>(R.id.autoCompleteTextViewNationality).text.toString()
        val date = findViewById<TextView>(R.id.editTextDate).text
        val right = findViewById<RadioButton>(R.id.radioButtonR)
        val left = findViewById<RadioButton>(R.id.radioButtonL)
        val strength = findViewById<TextView>(R.id.autoCompleteTextViewStrength).text.toString()
        val weakness = findViewById<TextView>(R.id.autoCompleteTextViewWeakness).text.toString()
        val notes = findViewById<EditText>(R.id.editTextNote).text.toString()

        if(nationality.isNotEmpty()){
            database.child("nationality").setValue(nationality)
        }
        if(!date.isNullOrEmpty()){
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = dateFormat.parse(date.toString()) // Sparsowanie daty

            val milliseconds = date?.time ?: 0
            database.child("dateOfBirth").setValue(milliseconds)
        }
        if(right.isChecked){
            database.child("handedness").setValue("Righthanded")
        }
        else if(left.isChecked){
            database.child("handedness").setValue("Lefthanded")
        }
        if(strength.isNotEmpty()){
            database.child("strength").setValue(strength)
        }
        if(weakness.isNotEmpty()){
            database.child("weakness").setValue(weakness)
        }
        if(notes.isNotEmpty()){
            database.child("note").setValue(notes)
        }

    }
}
