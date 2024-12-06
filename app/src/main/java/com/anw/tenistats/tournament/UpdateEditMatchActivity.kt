package com.anw.tenistats.tournament

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.anw.tenistats.R
import com.anw.tenistats.databinding.ActivityUpdateEditMatchBinding
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class UpdateEditMatchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateEditMatchBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var databaseT: DatabaseReference
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var tournamentId : String
    private lateinit var matchNumber : String
    private lateinit var drawSize : String

    private lateinit var p1 : EditText
    private lateinit var p2 : EditText
    private lateinit var set1p2 : Spinner
    private lateinit var set1p1 : Spinner
    private lateinit var set2p1 : Spinner
    private lateinit var set2p2 : Spinner
    private lateinit var set3p1 : Spinner
    private lateinit var set3p2 : Spinner
    private lateinit var pN1 : TextView
    private lateinit var pN2 : TextView
    private lateinit var submitButton : Button

    // Declare variables to store original values
    private var originalPlayer1Value: String? = null
    private var originalPlayer2Value: String? = null
    private var originalSet1p1Value: String? = null
    private var originalSet1p2Value: String? = null
    private var originalSet2p1Value: String? = null
    private var originalSet2p2Value: String? = null
    private var originalSet3p1Value: String? = null
    private var originalSet3p2Value: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUpdateEditMatchBinding.inflate(layoutInflater)
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
        drawSize = intent.getStringExtra("draw_size").toString()

        database = FirebaseDatabase.getInstance(
            "https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/"
        ).getReference("Tournaments").child(tournamentId).child(matchNumber)
        databaseT=FirebaseDatabase.getInstance(
            "https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/"
        ).getReference("Tournaments").child(tournamentId)

        p1 = binding.editTextPlayer1U
        p2 = binding.editTextPlayer2U

        set1p1 = binding.set1p1ScoreU
        set1p2 = binding.set1p2ScoreU
        set2p1 = binding.set2p1ScoreU
        set2p2 = binding.set2p2ScoreU
        set3p1 = binding.set3p1ScoreU
        set3p2 = binding.set3p2ScoreU

        pN1 = binding.TextViewPlayer1
        pN2 = binding.TextViewPlayer2

        // Ładowanie danych z Firebase do pól
        loadMatchData()
        pointsList()

        submitButton = binding.buttonSubmitEdit
        submitButton.setOnClickListener {
            saveMatchData()
            val intent = Intent(this, GenerateDrawActivity::class.java)
            intent.putExtra("tournament_id", tournamentId)
            intent.putExtra("match_number", matchNumber)
            intent.putExtra("draw_size", drawSize)
            startActivity(intent)
        }
    }

    private fun loadMatchData() {
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

                // Ustawienie pól w UI
                p1.setText(player1Value)
                p2.setText(player2Value)
                set1p1.setSelection(getIndexOfOption(set1p1Value))
                set1p2.setSelection(getIndexOfOption(set1p2Value))
                set2p1.setSelection(getIndexOfOption(set2p1Value))
                set2p2.setSelection(getIndexOfOption(set2p2Value))
                set3p1.setSelection(getIndexOfOption(set3p1Value))
                set3p2.setSelection(getIndexOfOption(set3p2Value))

                pN1.text = player1Value
                pN2.text = player2Value

                // Save original values for comparison later
                originalPlayer1Value = player1Value
                originalPlayer2Value = player2Value
                originalSet1p1Value = set1p1Value
                originalSet1p2Value = set1p2Value
                originalSet2p1Value = set2p1Value
                originalSet2p2Value = set2p2Value
                originalSet3p1Value = set3p1Value
                originalSet3p2Value = set3p2Value
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error: ${error.message}")
            }
        })
    }

    private fun getIndexOfOption(value: String?): Int {
        val options = listOf("None", "0", "1", "2", "3", "4", "5", "6")
        return options.indexOf(value)
    }
    private fun pointsList() {
        val options = listOf("None", "0", "1", "2", "3", "4", "5", "6")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, // domyślny układ elementu
            options // dane do wyświetlenia
        )

        binding.set1p1ScoreU.adapter = adapter
        binding.set1p2ScoreU.adapter = adapter
        binding.set2p1ScoreU.adapter = adapter
        binding.set2p2ScoreU.adapter = adapter
        binding.set3p1ScoreU.adapter = adapter
        binding.set3p2ScoreU.adapter = adapter
    }

    private fun saveMatchData() {
        // Zbieranie danych z pól
        val player1Value = p1.text?.toString() ?: ""
        val player2Value = p2.text?.toString() ?: ""
        val set1p1Value = set1p1.selectedItem?.toString() ?: "None"
        val set1p2Value = set1p2.selectedItem?.toString() ?: "None"
        val set2p1Value = set2p1.selectedItem?.toString() ?: "None"
        val set2p2Value = set2p2.selectedItem?.toString() ?: "None"
        val set3p1Value = set3p1.selectedItem?.toString() ?: "None"
        val set3p2Value = set3p2.selectedItem?.toString() ?: "None"

        // Sprawdzenie, czy wartości zostały zmienione, jeśli tak to zapisujemy je w Firebase
        if (player1Value != originalPlayer1Value && player1Value.isNotEmpty()) {
            database.child("player1Edit").setValue(player1Value)
            database.child("changes").setValue(true)
            databaseT.child("changes").get().addOnSuccessListener { snapshot ->
                val currentChanges = snapshot.getValue(Int::class.java) ?: 0
                val updatedChanges = currentChanges + 1

                databaseT.child("changes").setValue(updatedChanges)
            }.addOnFailureListener {
                Log.e("Firebase", "Error getting changes", it)
            }
        }
        if (player2Value != originalPlayer2Value && player2Value.isNotEmpty()) {
            database.child("player2Edit").setValue(player2Value)
            database.child("changes").setValue(true)
            databaseT.child("changes").get().addOnSuccessListener { snapshot ->
                val currentChanges = snapshot.getValue(Int::class.java) ?: 0
                val updatedChanges = currentChanges + 1

                databaseT.child("changes").setValue(updatedChanges)
            }.addOnFailureListener {
                Log.e("Firebase", "Error getting changes", it)
            }
        }
        if (set1p1Value != originalSet1p1Value && set1p1Value != "None") {
            database.child("set1p1Edit").setValue(set1p1Value)
            database.child("changes").setValue(true)
            databaseT.child("changes").get().addOnSuccessListener { snapshot ->
                val currentChanges = snapshot.getValue(Int::class.java) ?: 0
                val updatedChanges = currentChanges + 1

                databaseT.child("changes").setValue(updatedChanges)
            }.addOnFailureListener {
                Log.e("Firebase", "Error getting changes", it)
            }
        }
        if (set1p2Value != originalSet1p2Value && set1p2Value != "None") {
            database.child("set1p2Edit").setValue(set1p2Value)
            database.child("changes").setValue(true)
            databaseT.child("changes").get().addOnSuccessListener { snapshot ->
                val currentChanges = snapshot.getValue(Int::class.java) ?: 0
                val updatedChanges = currentChanges + 1

                databaseT.child("changes").setValue(updatedChanges)
            }.addOnFailureListener {
                Log.e("Firebase", "Error getting changes", it)
            }
        }
        if (set2p1Value != originalSet2p1Value && set2p1Value != "None") {
            database.child("set2p1Edit").setValue(set2p1Value)
            database.child("changes").setValue(true)
            databaseT.child("changes").get().addOnSuccessListener { snapshot ->
                val currentChanges = snapshot.getValue(Int::class.java) ?: 0
                val updatedChanges = currentChanges + 1

                databaseT.child("changes").setValue(updatedChanges)
            }.addOnFailureListener {
                Log.e("Firebase", "Error getting changes", it)
            }
        }
        if (set2p2Value != originalSet2p2Value && set2p2Value != "None") {
            database.child("set2p2Edit").setValue(set2p2Value)
            database.child("changes").setValue(true)
            databaseT.child("changes").get().addOnSuccessListener { snapshot ->
                val currentChanges = snapshot.getValue(Int::class.java) ?: 0
                val updatedChanges = currentChanges + 1

                databaseT.child("changes").setValue(updatedChanges)
            }.addOnFailureListener {
                Log.e("Firebase", "Error getting changes", it)
            }
        }
        if (set3p1Value != originalSet3p1Value && set3p1Value != "None") {
            database.child("set3p1Edit").setValue(set3p1Value)
            database.child("changes").setValue(true)
            databaseT.child("changes").get().addOnSuccessListener { snapshot ->
                val currentChanges = snapshot.getValue(Int::class.java) ?: 0
                val updatedChanges = currentChanges + 1

                databaseT.child("changes").setValue(updatedChanges)
            }.addOnFailureListener {
                Log.e("Firebase", "Error getting changes", it)
            }
        }
        if (set3p2Value != originalSet3p2Value && set3p2Value != "None") {
            database.child("set3p2Edit").setValue(set3p2Value)
            database.child("changes").setValue(true)
            databaseT.child("changes").get().addOnSuccessListener { snapshot ->
                val currentChanges = snapshot.getValue(Int::class.java) ?: 0
                val updatedChanges = currentChanges + 1

                databaseT.child("changes").setValue(updatedChanges)
            }.addOnFailureListener {
                Log.e("Firebase", "Error getting changes", it)
            }
        }

        // Informacja o zapisaniu danych
        Log.d("Firebase", "Dane zostały zapisane!")
    }

}
