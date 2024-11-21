package com.anw.tenistats.tournament

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import com.anw.tenistats.R
import com.anw.tenistats.databinding.AddPlayerToTournamentDialogBinding
import com.anw.tenistats.player.ViewTeamActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

class AddPlayerToTournamentDialog(context: Context) : Dialog(context) {
    private lateinit var binding: AddPlayerToTournamentDialogBinding
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    private val availablePlayers: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = AddPlayerToTournamentDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference(user.toString())


        loadAvailablePlayers()

        binding.buttonAddToTournament.setOnClickListener {
            val selectedPlayerName = binding.AutoCompleteTextViewPlayer.text.toString().trim()
            if (selectedPlayerName.isNotEmpty()) {
                addPlayerToTournament(selectedPlayerName)
            }
        }
    }

    private fun loadAvailablePlayers() {
        /*database.child("Tournaments").
        database.child("Players").orderByChild("active").equalTo(true)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    availablePlayers.clear()
                    snapshot.children.forEach { playerSnapshot ->
                        val playerName = playerSnapshot.key ?: ""
                        val playerTeam = playerSnapshot.child("team").getValue(String::class.java)

                        if (playerTeam.isNullOrEmpty() && !teamView.players.contains(playerName)) {
                            availablePlayers.add(playerName)
                        }
                    }
                    setupAutoCompleteTextView()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("AddPlayerToTeamDialog", "Database error: ${error.message}")
                }
            })*/
    }


    private fun setupAutoCompleteTextView() {
        /*val adapter = ArrayAdapter(context, R.layout.spinner_item_team, availablePlayers)
        autoCompleteTextView.apply {
            setAdapter(adapter)
            threshold = 0 // Pokazuje listę od razu, niezależnie od liczby wpisanych znaków
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) showDropDown()
            }
        }*/
    }


    private fun addPlayerToTournament(playerName: String) {
        /*// Dodanie zawodnika do drużyny w węźle "Teams"
        val teamName = teamView.name
        database.child("Teams").child(teamName).child("players").get().addOnSuccessListener { snapshot ->
            val players = snapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {}) ?: mutableListOf()
            if (!players.contains(playerName)) {
                players.add(playerName)
                database.child("Teams").child(teamName).child("players").setValue(players)
                    .addOnSuccessListener {
                        Log.d("AddPlayerToTeamDialog", "Player $playerName added to team $teamName.")

                        // Zaktualizowanie teamu u zawodnika
                        database.child("Players").child(playerName).child("team").setValue(teamName)
                            .addOnSuccessListener {
                                Log.d("AddPlayerToTeamDialog", "Team updated for player $playerName.")
                                val intent = Intent(context, ViewTeamActivity   ::class.java)
                                context.startActivity(intent)
                                dismiss()
                            }
                            .addOnFailureListener { e ->
                                Log.e("AddPlayerToTeamDialog", "Failed to update player's team: ${e.message}")
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("AddPlayerToTeamDialog", "Failed to add player to team: ${e.message}")
                    }
            } else {
                Log.d("AddPlayerToTeamDialog", "Player $playerName is already in team $teamName.")
            }
        }*/
    }
}