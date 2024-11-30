package com.anw.tenistats.tournament

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.anw.tenistats.R
import com.anw.tenistats.databinding.ActivityEditMatchBinding
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class EditMatchActivity: AppCompatActivity() {
    private lateinit var binding: ActivityEditMatchBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var tournamentId : String
    private lateinit var matchNumber : String

    private lateinit var p1 : EditText
    private lateinit var p2 : EditText
    private lateinit var set1p2 : Spinner
    private lateinit var set1p1 : Spinner
    private lateinit var set2p1 : Spinner
    private lateinit var set2p2 : Spinner
    private lateinit var set3p1 : Spinner
    private lateinit var set3p2 : Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEditMatchBinding.inflate(layoutInflater)
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

        tournamentId = intent.getStringExtra("tournament_id").toString()
        matchNumber = intent.getStringExtra("match_number").toString()

        database = FirebaseDatabase.getInstance(
            "https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/"
        ).getReference("Tournaments").child(tournamentId).child(matchNumber)

        p1 = binding.editTextPlayer1
        p2 = binding.editTextPlayer2

        set1p1 = binding.set1p1Score
        set1p2 = binding.set1p2Score
        set2p1 = binding.set2p1Score
        set2p2 = binding.set2p2Score
        set3p1 = binding.set3p1Score
        set3p2 = binding.set3p2Score

        //pobranie wartość z mapy i ustawienie pól
        setMatchInformation { firstUpdate ->

            val layout: View = findViewById(R.id.main) // np. ConstraintLayout, RelativeLayout, itd.
            layout.setOnClickListener {
                if (p1.hasFocus()) p1.clearFocus()
                else if (p2.hasFocus()) p2.clearFocus()
            }

            p1.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    // Jeśli EditText zyska fokus, ustawiamy kursor na końcu tekstu
                    updateValueInDatabase(database.child("player1"), p1.text.toString())
                }
            }
            p2.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    // Jeśli EditText zyska fokus, ustawiamy kursor na końcu tekstu
                    updateValueInDatabase(database.child("player2"), p2.text.toString())
                }
            }

            set1p1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    // Pobierz wybraną wartość
                    val selectedItem = parent?.getItemAtPosition(position).toString()
                    //if (firstUpdate["set1p1"] == true)
                        updateValueInDatabase(database.child("set1p1"), selectedItem)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Obsługa przypadku, gdy nic nie zostanie wybrane
                    Log.d("Spinner", "Nic nie zostało wybrane")
                }
            }
            set1p2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    // Pobierz wybraną wartość
                    val selectedItem = parent?.getItemAtPosition(position).toString()
                    //if (firstUpdate["set1p2"] == true)
                        updateValueInDatabase(database.child("set1p2"), selectedItem)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Obsługa przypadku, gdy nic nie zostanie wybrane
                    Log.d("Spinner", "Nic nie zostało wybrane")
                }
            }
            set2p1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    // Pobierz wybraną wartość
                    val selectedItem = parent?.getItemAtPosition(position).toString()
                    //if (firstUpdate["set2p1"] == true)
                        updateValueInDatabase(database.child("set2p1"), selectedItem)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Obsługa przypadku, gdy nic nie zostanie wybrane
                    Log.d("Spinner", "Nic nie zostało wybrane")
                }
            }
            set2p2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    // Pobierz wybraną wartość
                    val selectedItem = parent?.getItemAtPosition(position).toString()
                    //if (firstUpdate["set2p2"] == true)
                        updateValueInDatabase(database.child("set2p2"), selectedItem)

                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Obsługa przypadku, gdy nic nie zostanie wybrane
                    Log.d("Spinner", "Nic nie zostało wybrane")
                }
            }
            set3p1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    // Pobierz wybraną wartość
                    val selectedItem = parent?.getItemAtPosition(position).toString()
                    //if (firstUpdate["set3p1"] == true)
                        updateValueInDatabase(database.child("set3p1"), selectedItem)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Obsługa przypadku, gdy nic nie zostanie wybrane
                    Log.d("Spinner", "Nic nie zostało wybrane")
                }
            }
            set3p2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    // Pobierz wybraną wartość
                    val selectedItem = parent?.getItemAtPosition(position).toString()
                    //if (firstUpdate["set3p2"] == true)
                        updateValueInDatabase(database.child("set3p2"), selectedItem)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Obsługa przypadku, gdy nic nie zostanie wybrane
                    Log.d("Spinner", "Nic nie zostało wybrane")
                }
            }
        }

        //lista rozwijana punktów
        pointsList()
    }

    private fun updateValueInDatabase(db: DatabaseReference, value: String){
        db.setValue(value)
            .addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    // Zmiana wartości zakończona sukcesem
                    Log.d("Firebase", "Wartość została zmieniona!")
                } else {
                    // Obsługa błędu
                    Log.e("Firebase", "Błąd podczas zmiany wartości", task.exception)
                }
            }
    }
    private fun setMatchInformation(callback: (MutableMap<String, Boolean>) -> Unit) {
        val keys = listOf("p1", "p2", "set1p1", "set1p2", "set2p1", "set2p2", "set3p1", "set3p2")
        val firstUpdate = keys.associateWith { false }.toMutableMap()
        val options = listOf("None", "0", "1", "2", "3", "4", "5", "6")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Pobieramy wartości z bazy danych
                val player1Value = snapshot.child("player1").getValue(String::class.java)
                val player2Value = snapshot.child("player2").getValue(String::class.java)
                val set1p1Value = snapshot.child("set1p1").getValue(String::class.java)
                val set1p2Value = snapshot.child("set1p2").getValue(String::class.java)
                val set2p1Value = snapshot.child("set2p1").getValue(String::class.java)
                val set2p2Value = snapshot.child("set2p2").getValue(String::class.java)
                val set3p1Value = snapshot.child("set3p1").getValue(String::class.java)
                val set3p2Value = snapshot.child("set3p2").getValue(String::class.java)

                // Modyfikowanie wartości w mapie
                if (player1Value.isNullOrEmpty()) firstUpdate["p1"] = true
                if (player2Value.isNullOrEmpty()) firstUpdate["p2"] = true
                if (set1p1Value == "None") firstUpdate["set1p1"] = true
                if (set1p2Value == "None") firstUpdate["set1p2"] = true
                if (set2p1Value.isNullOrEmpty()) firstUpdate["set2p1"] = true
                if (set2p2Value.isNullOrEmpty()) firstUpdate["set2p2"] = true
                if (set3p1Value.isNullOrEmpty()) firstUpdate["set3p1"] = true
                if (set3p2Value.isNullOrEmpty()) firstUpdate["set3p2"] = true

                // Przypisanie danych do widoków
                p1.setText(player1Value)
                p2.setText(player2Value)
                //jeżeli punkty już były wprowadzaone to wstawiane są wartości z bazy
                //w przeciwnym razie będą dostępne do jednokrotnej edycji
                if (firstUpdate["set1p1"] == false) set1p1.setSelection(options.indexOf(set1p1Value))
                if (firstUpdate["set1p2"] == false) set1p2.setSelection(options.indexOf(set1p2Value))
                if (firstUpdate["set2p1"] == false) set2p1.setSelection(options.indexOf(set2p1Value))
                if (firstUpdate["set2p2"] == false) set2p2.setSelection(options.indexOf(set2p2Value))
                if (firstUpdate["set3p1"] == false) set3p1.setSelection(options.indexOf(set3p1Value))
                if (firstUpdate["set3p2"] == false) set3p2.setSelection(options.indexOf(set3p2Value))

                // Wywołanie callbacka z zaktualizowaną mapą
                callback(firstUpdate)
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsługa błędów
                Log.e("Firebase", "Error: ${error.message}")
            }
        })
    }

    private fun pointsList() {
        val options = listOf("None", "0", "1", "2", "3", "4", "5", "6")
        //point set1p1
        val adapter = ArrayAdapter(
            this, // kontekst (np. activity)
            android.R.layout.simple_spinner_item, // domyślny układ elementu
            options // dane do wyświetlenia
        )

        binding.set1p1Score.adapter = adapter
        binding.set1p2Score.adapter = adapter
        binding.set2p1Score.adapter = adapter
        binding.set2p2Score.adapter = adapter
        binding.set3p1Score.adapter = adapter
        binding.set3p2Score.adapter = adapter
    }
}
