package com.anw.tenistats.player

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AddPlayerToTeamDialog(
    context: Context,
    private val teamView: TeamView
) : Dialog(context) {

    private lateinit var database: DatabaseReference
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var buttonAddToTeam: Button
    private lateinit var firebaseAuth: FirebaseAuth

    // Lista zawodników dostępnych do przypisania
    private val availablePlayers: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.add_player_to_team_dialog)

        autoCompleteTextView = findViewById(R.id.autoNamePlayer)
        buttonAddToTeam = findViewById(R.id.buttonAddToTeam)


        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference(user.toString())


        loadAvailablePlayers()

        buttonAddToTeam.setOnClickListener {
            val selectedPlayerName = autoCompleteTextView.text.toString().trim()
            if (selectedPlayerName.isNotEmpty()) {
                addPlayerToTeam(selectedPlayerName)
            }
        }
    }

    private fun loadAvailablePlayers() {
        // Pobranie listy zawodników z węzła "Players"
        database.child("Players").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                availablePlayers.clear()
                snapshot.children.forEach { playerSnapshot ->
                    val playerName = playerSnapshot.key ?: ""
                    val playerTeam = playerSnapshot.child("team").getValue(String::class.java)

                    // Dodaj zawodnika tylko jeśli nie jest już w tej drużynie
                    if (playerTeam.isNullOrEmpty() && !teamView.players.contains(playerName)) {
                        availablePlayers.add(playerName)
                    }
                }
                setupAutoCompleteTextView()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AddPlayerToTeamDialog", "Database error: ${error.message}")
            }
        })
    }

    private fun setupAutoCompleteTextView() {
        // Konfiguracja listy rozwijanej z dostępnych zawodników
        val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, availablePlayers)
        autoCompleteTextView.setAdapter(adapter)
    }

    private fun addPlayerToTeam(playerName: String) {
        // Dodanie zawodnika do drużyny w węźle "Teams"
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
        }
    }
}
