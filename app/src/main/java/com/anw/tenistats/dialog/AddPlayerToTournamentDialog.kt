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
import android.widget.CheckBox
import android.widget.LinearLayout
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
    private val selectedPlayers = mutableSetOf<String>()
    private lateinit var firebaseAuth: FirebaseAuth

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_add_players_to_tournament)

        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference(user.toString()).child("Players")

        val playersContainer = findViewById<LinearLayout>(R.id.playersContainer)
        val buttonCancel = findViewById<Button>(R.id.buttonCancel)
        val buttonAdd = findViewById<Button>(R.id.buttonAddPTT)

        // Fetch players and populate the list
        fetchAndDisplayPlayers(playersContainer)

        buttonCancel.setOnClickListener {
            dismiss()
        }

        buttonAdd.setOnClickListener {
            addSelectedPlayersToTournament()
        }
    }

    private fun fetchAndDisplayPlayers(playersContainer: LinearLayout) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                playersContainer.removeAllViews()

                for (playerSnapshot in snapshot.children) {
                    val playerName = playerSnapshot.child("player").getValue(String::class.java)
                    val active = playerSnapshot.child("active").getValue(Boolean::class.java)
                    val tournaments = playerSnapshot.child("tournaments").getValue(object :
                        GenericTypeIndicator<List<String>>() {}) ?: emptyList()

                    if (playerName != null && !tournaments.contains(tournamentId)&&active==true) {
                        val checkBox = CheckBox(context).apply {
                            text = playerName
                            setOnCheckedChangeListener { _, isChecked ->
                                if (isChecked) {
                                    selectedPlayers.add(playerSnapshot.key!!)
                                } else {
                                    selectedPlayers.remove(playerSnapshot.key!!)
                                }
                            }
                        }
                        playersContainer.addView(checkBox)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load players: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addSelectedPlayersToTournament() {
        for (playerId in selectedPlayers) {
            val tournamentsRef = database.child(playerId).child("tournaments")
            tournamentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tournaments = snapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {})
                        ?: mutableListOf()
                    if (!tournaments.contains(tournamentId)) {
                        tournaments.add(tournamentId)
                        tournamentsRef.setValue(tournaments).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("Dialog", "Tournament added for player $playerId")
                            } else {
                                Log.e("Dialog", "Failed to update player $playerId")
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Dialog", "Database error: ${error.message}")
                }
            })
        }
        dismiss()
    }
}
