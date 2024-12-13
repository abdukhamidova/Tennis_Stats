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
import com.anw.tenistats.databinding.ActivityChangeEditMatchBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class ChangeEditMatchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangeEditMatchBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var databaseT: DatabaseReference
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var tournamentId : String
    private lateinit var matchNumber : String
    private lateinit var drawSize : String

    private lateinit var p1 : Spinner
    private lateinit var p2 : Spinner
    private lateinit var Set1p2 : Spinner
    private lateinit var Set1p1 : Spinner
    private lateinit var Set2p1 : Spinner
    private lateinit var Set2p2 : Spinner
    private lateinit var Set3p1 : Spinner
    private lateinit var Set3p2 : Spinner
    private lateinit var pN1 : TextView
    private lateinit var pN2 : TextView

    private lateinit var submitButton : Button
    private lateinit var cancelButton : Button
    private lateinit var rejectButton : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChangeEditMatchBinding.inflate(layoutInflater)
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

        p1 = binding.SpinnerPlayer1
        p2 = binding.SpinnerPlayer2

        Set1p1 = binding.set1p1Score
        Set1p2 = binding.set1p2Score
        Set2p1 = binding.set2p1Score
        Set2p2 = binding.set2p2Score
        Set3p1 = binding.set3p1Score
        Set3p2 = binding.set3p2Score

        /*pN1 = binding.TextViewPlayer1
        pN2 = binding.TextViewPlayer2*/

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Sprawdzenie, czy węzły istnieją
                val player1 = dataSnapshot.child("player1").getValue(String::class.java)
                val player1Edit = dataSnapshot.child("player1Edit").getValue(String::class.java)
                val player2 = dataSnapshot.child("player2").getValue(String::class.java)
                val player2Edit = dataSnapshot.child("player2Edit").getValue(String::class.java)

                // Wypełnianie spinnerów
                val playerAdapter1 = ArrayAdapter<String>(this@ChangeEditMatchActivity, android.R.layout.simple_spinner_item).apply {
                    setDropDownViewResource(R.layout.spinner_item_stats_right)
                }

                // Player 1
                if (player1Edit != null) {
                    playerAdapter1.add(player1Edit)
                    playerAdapter1.add(player1!!)
                } else {
                    playerAdapter1.add(player1!!)
                }
                p1.adapter = playerAdapter1
                if (playerAdapter1.count >1) {
                    p1.setBackgroundColor(getColor(R.color.app_statsViewButton))
                    playerAdapter1.setDropDownViewResource(R.layout.spinner_item_stats_left)
                }


                // Player 2
                val playerAdapter2 = ArrayAdapter<String>(this@ChangeEditMatchActivity, android.R.layout.simple_spinner_item).apply {
                    setDropDownViewResource(R.layout.spinner_item_stats_right)
                }
                if (player2Edit != null) {
                    playerAdapter2.add(player2Edit)
                    playerAdapter2.add(player2!!)
                } else {
                    playerAdapter2.add(player2!!)
                }
                p2.adapter = playerAdapter2
                if (playerAdapter2.count > 1) {
                    p2.setBackgroundColor(getColor(R.color.app_statsViewButton))
                    playerAdapter2.setDropDownViewResource(R.layout.spinner_item_stats_left)
                }

                // Set 1
                val set1p1 = dataSnapshot.child("set1p1").getValue(String::class.java)
                val set1p1Edit = dataSnapshot.child("set1p1Edit").getValue(String::class.java)
                val set1p2 = dataSnapshot.child("set1p2").getValue(String::class.java)
                val set1p2Edit = dataSnapshot.child("set1p2Edit").getValue(String::class.java)

                // Wypełnianie spinnerów dla setów
                val setAdapter11 = ArrayAdapter<String>(this@ChangeEditMatchActivity, android.R.layout.simple_spinner_item).apply {
                    setDropDownViewResource(R.layout.spinner_item_stats_right)
                }

                // Set 1 Player 1
                if (set1p1Edit != null) {
                    setAdapter11.add(set1p1Edit)
                    setAdapter11.add(set1p1!!)
                } else {
                    setAdapter11.add(set1p1!!)
                }
                Set1p1.adapter = setAdapter11
                if (setAdapter11.count > 1 ) {
                    Set1p1.setBackgroundColor(getColor(R.color.app_statsViewButton))
                    setAdapter11.setDropDownViewResource(R.layout.spinner_item_stats_left)
                }

                // Set 1 Player 2
                val setAdapter12 = ArrayAdapter<String>(this@ChangeEditMatchActivity, android.R.layout.simple_spinner_item).apply {
                    setDropDownViewResource(R.layout.spinner_item_stats_right)
                }
                if (set1p2Edit != null) {
                    setAdapter12.add(set1p2Edit)
                    setAdapter12.add(set1p2!!)
                } else {
                    setAdapter12.add(set1p2!!)
                }
                Set1p2.adapter = setAdapter12
                if (setAdapter12.count > 1) {
                    Set1p2.setBackgroundColor(getColor(R.color.app_statsViewButton))
                    setAdapter12.setDropDownViewResource(R.layout.spinner_item_stats_left)
                }

                // Set 2

                val set2p1 = dataSnapshot.child("set2p1").getValue(String::class.java)
                val set2p1Edit = dataSnapshot.child("set2p1Edit").getValue(String::class.java)
                val set2p2 = dataSnapshot.child("set2p2").getValue(String::class.java)
                val set2p2Edit = dataSnapshot.child("set2p2Edit").getValue(String::class.java)

                // Set 2 Player 1
                val setAdapter21 = ArrayAdapter<String>(this@ChangeEditMatchActivity, android.R.layout.simple_spinner_item).apply {
                    setDropDownViewResource(R.layout.spinner_item_stats_right)
                }
                if (set2p1Edit != null) {
                    setAdapter21.add(set2p1Edit)
                    setAdapter21.add(set2p1!!)
                } else {
                    setAdapter21.add(set2p1!!)
                }
                Set2p1.adapter = setAdapter21
                if (setAdapter21.count > 1) {
                    Set2p1.setBackgroundColor(getColor(R.color.app_statsViewButton))
                    setAdapter21.setDropDownViewResource(R.layout.spinner_item_stats_left)
                }

                // Set 2 Player 2
                val setAdapter22 = ArrayAdapter<String>(this@ChangeEditMatchActivity, android.R.layout.simple_spinner_item).apply {
                    setDropDownViewResource(R.layout.spinner_item_stats_right)
                }
                if (set2p2Edit != null) {
                    setAdapter22.add(set2p2Edit)
                    setAdapter22.add(set2p2!!)
                } else {
                    setAdapter22.add(set2p2!!)
                }
                Set2p2.adapter = setAdapter22
                if (setAdapter22.count >1 ) {
                    Set2p2.setBackgroundColor(getColor(R.color.app_statsViewButton))
                    setAdapter22.setDropDownViewResource(R.layout.spinner_item_stats_left)
                }
                // Set 3
                val set3p1 = dataSnapshot.child("set3p1").getValue(String::class.java)
                val set3p1Edit = dataSnapshot.child("set3p1Edit").getValue(String::class.java)
                val set3p2 = dataSnapshot.child("set3p2").getValue(String::class.java)
                val set3p2Edit = dataSnapshot.child("set3p2Edit").getValue(String::class.java)

                // Set 3 Player 1
                val setAdapter31 = ArrayAdapter<String>(this@ChangeEditMatchActivity, android.R.layout.simple_spinner_item).apply {
                    setDropDownViewResource(R.layout.spinner_item_stats_right)
                }
                if(set3p1!=null && set3p2!=null) {
                    if (set3p1Edit != null) {
                        setAdapter31.add(set3p1Edit)
                        setAdapter31.add(set3p1!!)
                    } else {
                        setAdapter31.add(set3p1!!)
                    }
                    Set3p1.adapter = setAdapter31
                    if (setAdapter31.count > 1) {
                        Set3p1.setBackgroundColor(getColor(R.color.app_statsViewButton))
                        setAdapter31.setDropDownViewResource(R.layout.spinner_item_stats_left)
                    }

                    // Set 3 Player 2
                    val setAdapter32 = ArrayAdapter<String>(
                        this@ChangeEditMatchActivity,
                        android.R.layout.simple_spinner_item
                    ).apply {
                        setDropDownViewResource(R.layout.spinner_item_stats_right)
                    }
                    setAdapter32.clear()
                    if (set3p2Edit != null) {
                        setAdapter32.add(set3p2Edit)
                        setAdapter32.add(set3p2!!)
                    } else {
                        setAdapter32.add(set3p2!!)
                    }
                    Set3p2.adapter = setAdapter32
                    if (setAdapter32.count > 1) {
                        Set3p2.setBackgroundColor(getColor(R.color.app_statsViewButton))
                        setAdapter32.setDropDownViewResource(R.layout.spinner_item_stats_left)
                    }
                }
                else
                {
                    Set3p1.isEnabled = false
                    Set3p2.isEnabled = false
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("EditMatchActivity", "loadPost:onCancelled", databaseError.toException())
            }
        })

        rejectButton = binding.btnReject
        rejectButton.setOnClickListener {
            database.child("changes").setValue(false)

            // Zmniejszenie wartości węzła "changes"
            databaseT.child("changes").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val changes = dataSnapshot.getValue(Int::class.java) ?: 0
                    databaseT.child("changes").setValue(changes - 1)

                    // Usunięcie niepotrzebnych wartości z bazy danych
                    database.child("player1Edit").removeValue()
                    database.child("player2Edit").removeValue()
                    database.child("set1p1Edit").removeValue()
                    database.child("set1p2Edit").removeValue()
                    database.child("set2p1Edit").removeValue()
                    database.child("set2p2Edit").removeValue()
                    database.child("set3p1Edit").removeValue()
                    database.child("set3p2Edit").removeValue()

                    // Przejście do EditMatchActivity
                    val intent = Intent(this@ChangeEditMatchActivity, GenerateDrawActivity::class.java)
                    intent.putExtra("tournament_id", tournamentId)
                    intent.putExtra("draw_size", drawSize)
                    startActivity(intent)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Obsługa błędu bazy danych
                    Log.e("DatabaseError", "onCancelled: ${databaseError.message}")
                }
            })
        }


        submitButton = binding.btnSubmit
        submitButton.setOnClickListener {
            val selectedPlayer1 = p1.selectedItem.toString()
            val selectedPlayer2 = p2.selectedItem.toString()
            val selectedSet1p1 = Set1p1.selectedItem.toString()
            val selectedSet1p2 = Set1p2.selectedItem.toString()
            val selectedSet2p1 = Set2p1.selectedItem.toString()
            val selectedSet2p2 = Set2p2.selectedItem.toString()

            val selectedSet3p1 = if (Set3p1.isEnabled) Set3p1.selectedItem.toString() else null
            val selectedSet3p2 = if (Set3p2.isEnabled) Set3p2.selectedItem.toString() else null

            // Zapis wyników w bazie danych
            database.child("player1").setValue(selectedPlayer1)
            database.child("player2").setValue(selectedPlayer2)
            database.child("set1p1").setValue(selectedSet1p1)
            database.child("set1p2").setValue(selectedSet1p2)
            database.child("set2p1").setValue(selectedSet2p1)
            database.child("set2p2").setValue(selectedSet2p2)

            if (selectedSet3p1 != null && selectedSet3p2 != null) {
                database.child("set3p1").setValue(selectedSet3p1)
                database.child("set3p2").setValue(selectedSet3p2)
            }

            // Obliczanie zwycięzcy
            var winner = calculateWinner()
            if(winner==selectedPlayer1)
                winner="player1"
            else if (winner==selectedPlayer2)
                winner="player2"
            if (winner != null) {
                database.child("winner").setValue(winner)
            }

            // Reszta logiki
            database.child("changes").setValue(false)
            databaseT.child("changes").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val changes = dataSnapshot.getValue(Int::class.java) ?: 0
                    databaseT.child("changes").setValue(changes - 1)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("EditMatchActivity", "loadPost:onCancelled", databaseError.toException())
                }
            })

            database.child("player1Edit").removeValue()
            database.child("player2Edit").removeValue()
            database.child("set1p1Edit").removeValue()
            database.child("set1p2Edit").removeValue()
            database.child("set2p1Edit").removeValue()
            database.child("set2p2Edit").removeValue()
            database.child("set3p1Edit").removeValue()
            database.child("set3p2Edit").removeValue()
            database.child("winnerEdit").removeValue()

            val intent = Intent(this@ChangeEditMatchActivity, GenerateDrawActivity::class.java)
            intent.putExtra("tournament_id", tournamentId)
            intent.putExtra("draw_size", drawSize)
            startActivity(intent)
        }


        cancelButton = binding.btnCancel
        cancelButton.setOnClickListener {
            val intent = Intent(this@ChangeEditMatchActivity, UpdateEditMatchActivity::class.java)
            intent.putExtra("tournament_id", tournamentId)
            intent.putExtra("match_number", matchNumber)
            intent.putExtra("draw_size", drawSize)
            startActivity(intent)
        }
    }
    private fun calculateWinner(): String? {
        // Pobierz wyniki setów
        val set1p1 = Set1p1.selectedItem.toString().toIntOrNull() ?: 0
        val set1p2 = Set1p2.selectedItem.toString().toIntOrNull() ?: 0
        val set2p1 = Set2p1.selectedItem.toString().toIntOrNull() ?: 0
        val set2p2 = Set2p2.selectedItem.toString().toIntOrNull() ?: 0
        val set3p1 = if (Set3p1.isEnabled) Set3p1.selectedItem.toString().toIntOrNull() ?: 0 else 0
        val set3p2 = if (Set3p2.isEnabled) Set3p2.selectedItem.toString().toIntOrNull() ?: 0 else 0

        // Liczba wygranych setów przez każdego gracza
        var player1Wins = 0
        var player2Wins = 0

        if (set1p1 > set1p2) player1Wins++ else if (set1p2 > set1p1) player2Wins++
        if (set2p1 > set2p2) player1Wins++ else if (set2p2 > set2p1) player2Wins++
        if (Set3p1.isEnabled && set3p1 > set3p2) player1Wins++ else if (set3p2 > set3p1) player2Wins++

        return when {
            player1Wins > player2Wins -> p1.selectedItem.toString() // Gracz 1 wygrywa
            player2Wins > player1Wins -> p2.selectedItem.toString() // Gracz 2 wygrywa
            else -> null // Remis lub brak rozstrzygnięcia
        }
    }


}
