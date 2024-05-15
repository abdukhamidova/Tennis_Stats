package com.anw.tenistats

import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.anw.tenistats.data.CountryRepository
import com.anw.tenistats.databinding.ActivityAddPlayerBinding
import com.anw.tenistats.ui.theme.NavigationDrawerHelper
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
import java.util.Locale

class AddPlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPlayerBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private val playersList = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize Firebase Auth
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()

        //MENU
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.navigationViewMenu)
        val menu = findViewById<ImageButton>(R.id.buttonMenu)
        val headerView = navigationView.getHeaderView(0)
        menu.setOnClickListener{
            drawerLayout.open()
        }
        navigationDrawerHelper = NavigationDrawerHelper(this)
        navigationDrawerHelper.setupNavigationDrawer(drawerLayout, navigationView, firebaseAuth, true)
        val backButton = findViewById<ImageButton>(R.id.buttonUndo)
        backButton.visibility = View.GONE

        val userEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        val userEmailView = headerView.findViewById<TextView>(R.id.textViewUserEmail)
        if(userEmail.isNotEmpty()) {
            userEmailView.text = userEmail
        }else {
            userEmailView.text = resources.getString(R.string.user_email)
        }
        //MENU

        binding.autoCompleteTextViewNationalityAP.setOnClickListener {
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
                    binding.autoCompleteTextViewNationalityAP.setAdapter(adapter)
                } catch (e: Exception) {
                    // Obsługa błędu
                    Log.e("CountryRepository", "Błąd podczas pobierania listy krajów", e)
                }
            }
        }
        binding.editTextDateAP.setOnClickListener {
            showDatePickerDialog()
        }

        //Weronika 23 marca ~ zapisywanie danych playera do bazy
        //11.04 ~u
        val user = firebaseAuth.currentUser?.uid
        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(user.toString()).child("Players")

        // Pobierz listę graczy z bazy danych
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                playersList.clear()
                for (playerSnapshot in snapshot.children) {
                    val playerName = playerSnapshot.key
                    playerName?.let { playersList.add(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsługa błędów odczytu danych z bazy danych
                Toast.makeText(
                    this@AddPlayerActivity,
                    "Failed to read players from database",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // Obsługa przycisku rozpoczęcia gry
        binding.buttonAdd.setOnClickListener {
            var player1 = binding.editTextName.text.toString().trimEnd() //pobiera Imię i Nazwisko gracza
            val pl1Inticap=getStringWithoutDigits(initcap(player1))
            val (player1FirstName, player1LastName) = splitNameToFirstAndLastName(pl1Inticap) //zwraca Imię i Nazwisko jako odzielne gracza
            val nationality = binding.autoCompleteTextViewNationalityAP.text.toString()
            val dateOfBirth = binding.editTextDateAP.text
            val rbR = binding.radioButtonR
            val rbL = binding.radioButtonL
            val strength = binding.autoCompleteTextViewStrengthAP.text.toString()
            val weakness = binding.autoCompleteTextViewWeaknessAP.text.toString()
            var milliseconds: Long? = null
            if(!dateOfBirth.isNullOrEmpty()) {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = dateFormat.parse(dateOfBirth.toString()) // Sparsowanie daty

                milliseconds = date?.time ?: 0
            }
            //~ru 14.04 optymalizacja i poprawa dodawania playera do bazy
            //tak, aby w kolejnym activity obowiązywało nazewnictwo z bazy
            if (player1.isEmpty()) {
                Toast.makeText(this, "Don't leave empty fields.", Toast.LENGTH_SHORT).show()
            }else{
                //sprawdzenie istnienia player1, jego ewentualne dodanie/duplikowanie
                checkPlayerExistence(pl1Inticap,player1FirstName, player1LastName,nationality, milliseconds,rbR,rbL,strength,weakness) { updatedPlayer1 ->
                    player1 = updatedPlayer1
                }
            }
            startActivity(Intent(this,ViewPlayerActivity::class.java))
        }
    }

    private fun splitNameToFirstAndLastName(playerName: String): Pair<String?, String?> {
        var firstName: String? = null
        var lastName: String? = null

        val splitName = playerName.split(" ")

        if (splitName.size >= 2) {
            firstName = splitName[0]
            lastName = splitName.subList(1, splitName.size).joinToString("").trimEnd()
        } else if (splitName.size == 1) {
            firstName = splitName[0]
        }

        return Pair(firstName, lastName)
    }
    //optymalizacja funkcji ~ru 12.04
    private fun checkPlayerExistence(
        playerName: String,
        firstName: String?,
        lastName: String?,
        nationality: String?,
        milliseconds: Long?,
        rbF: RadioButton,
        rbL: RadioButton,
        strength: String?,
        weakness: String?,
        callback: (String) -> Unit
    ) {
        database.child(playerName).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                var duplicateCount = 0
                database.orderByChild("firstName").equalTo(firstName)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (childSnapshot in dataSnapshot.children) {
                                val player = childSnapshot.getValue(Player::class.java)
                                if (player?.lastName == lastName && player?.firstName == firstName) {
                                    duplicateCount++
                                }
                            }
                            duplicateCount++
                            for (childSnapshot in dataSnapshot.children) {
                                val player = childSnapshot.getValue(Player::class.java)
                                if (player != null) {
                                    if (player.lastName == lastName && player.firstName == firstName && duplicateCount < player.duplicate!!) {
                                        duplicateCount = player.duplicate!! + 1
                                    }
                                }
                            }

                            val playerToReturn = playerName + duplicateCount.toString()

                            val player: Player
                            if (rbF.isChecked) {
                                player = Player(
                                    firstName,
                                    lastName,
                                    playerToReturn,
                                    duplicateCount,
                                    nationality,
                                    milliseconds,
                                    "Righthanded",
                                    strength,
                                    weakness,
                                    true
                                )
                            } else if (rbL.isChecked) {
                                player = Player(
                                    firstName,
                                    lastName,
                                    playerToReturn,
                                    duplicateCount,
                                    nationality,
                                    milliseconds,
                                    "Lefthanded",
                                    strength,
                                    weakness,
                                    true
                                )
                            } else {
                                player = Player(
                                    firstName,
                                    lastName,
                                    playerToReturn,
                                    duplicateCount,
                                    nationality,
                                    milliseconds,
                                    null,
                                    strength,
                                    weakness,
                                    true
                                )
                            }

                            database.child(playerToReturn).setValue(player)
                                .addOnSuccessListener {
                                    callback(playerToReturn)
                                }.addOnFailureListener {
                                    Log.e(TAG, "Failed to save player $playerName", it)
                                    // Obsługa błędu zapisu
                                }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "Failed to get duplicate count", error.toException())
                            // Obsługa błędu pobierania danych
                        }
                    })
            } else {
                val player: Player
                if (rbF.isChecked) {
                    player = Player(
                        firstName,
                        lastName,
                        playerName,
                        1,
                        nationality,
                        milliseconds,
                        "Righthanded",
                        strength,
                        weakness,
                        true
                    )
                } else {
                    player = Player(
                        firstName,
                        lastName,
                        playerName,
                        1,
                        nationality,
                        milliseconds,
                        "Lefthanded",
                        strength,
                        weakness,
                        true
                    )
                }

                database.child(playerName).setValue(player)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this@AddPlayerActivity,
                            "Player $playerName Successfully Saved",
                            Toast.LENGTH_SHORT
                        ).show()
                        callback(playerName)
                    }.addOnFailureListener {
                        Log.e(TAG, "Failed to save player $playerName", it)
                        // Obsługa błędu zapisu
                    }
            }
        }.addOnFailureListener{
            Log.e(TAG, "Failed to get player $playerName", it)
            // Obsługa błędu pobierania danych
        }
    }
    fun initcap(input: String): String {
        if (input.isEmpty()) {
            return ""
        }
        val words = input.split(" ")
        val capitalizedWords = words.map { word ->
            if (word.isNotEmpty()) {
                word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase()
            } else {
                ""
            }
        }
        return capitalizedWords.joinToString(" ")
    }

    fun getStringWithoutDigits(input: String): String {
        val result = StringBuilder()
        for (char in input) {
            if (!char.isDigit()) {
                result.append(char)
            }
        }
        return result.toString()
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
                binding.editTextDateAP.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun setupAutoCompleteTextView(countries: List<String>) {
        val autoCompleteTextView = binding.autoCompleteTextViewNationalityAP
        ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, countries)
        autoCompleteTextView.setOnClickListener {
            autoCompleteTextView.showDropDown()
        }
    }
}