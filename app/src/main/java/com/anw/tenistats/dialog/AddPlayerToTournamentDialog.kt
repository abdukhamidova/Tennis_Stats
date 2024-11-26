package com.anw.tenistats.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import com.anw.tenistats.R
import com.anw.tenistats.player.TeamView
import com.anw.tenistats.player.ViewTeamActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

class AddPlayerToTournamentDialog(
    context: Context,
    private val tournamentId: String
) : Dialog(context) {
    private lateinit var database: DatabaseReference
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var buttonAddToTournament: Button
    private lateinit var firebaseAuth: FirebaseAuth

    // Lista zawodników dostępnych do przypisania
    private val availablePlayers: MutableList<String> = mutableListOf()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_add_player_to_tournament)

        autoCompleteTextView = findViewById(R.id.playerSpinner)
        buttonAddToTournament = findViewById(R.id.buttonAddToTournament)


        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference(user.toString()).child("Players")


        loadAvailablePlayers(tournamentId)

        buttonAddToTournament.setOnClickListener {
            val selectedPlayerName = autoCompleteTextView.text.toString().trim()
            if (selectedPlayerName.isNotEmpty()) {
                addPlayerToTournament(selectedPlayerName,tournamentId)
            }
        }
    }

    private fun loadAvailablePlayers(tournamentId: String) {
        database.child("Players").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                availablePlayers.clear()
                if (snapshot.exists()) {
                    for (playerSnapshot in snapshot.children) {
                        // Access the Tournaments node for the current player
                        val tournamentsSnapshot = playerSnapshot.child("Tournaments")

                        // Check if tournamentId does not exist for this player
                        if (!tournamentsSnapshot.hasChild(tournamentId)) {
                            // Retrieve player details and add to the list
                            val playerName = playerSnapshot.key.toString()
                            availablePlayers.add(playerName)
                        }
                    }
                    setupAutoCompleteTextView()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AddPlayerToTeamDialog", "Database error: ${error.message}")
            }
        })
    }

    private fun setupAutoCompleteTextView() {
        val adapter = ArrayAdapter(context, R.layout.spinner_item_team, availablePlayers)
        autoCompleteTextView.apply {
            setAdapter(adapter)
            threshold = 0
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) showDropDown()
            }
        }
    }

    private fun addPlayerToTournament(playerName: String,tournamentId: String) {
        database.child("Tournaments").setValue(tournamentId).addOnSuccessListener{
            Log.d("AddPlayerToTournamentDialog", "Add player $playerName to the tournament.")
            val intent = Intent(context, AddPlayerToTournamentDialog::class.java)
            context.startActivity(intent)
            dismiss()
        }
        .addOnFailureListener { e ->
            Log.e("AddPlayerToTournamentDialog", "Failed to add player to tournament: ${e.message}")
        }
    }
}