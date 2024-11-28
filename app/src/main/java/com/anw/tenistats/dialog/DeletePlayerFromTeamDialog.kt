package com.anw.tenistats.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.anw.tenistats.R
import com.anw.tenistats.player.ViewTeamActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator

class DeletePlayerFromTeamDialog(
    context: Context,
    private val teamName: String,
    private val playerName: String
) : Dialog(context) {

    private lateinit var textViewConfirm: TextView
    private lateinit var buttonDeletePlayer: Button
    private lateinit var buttonCancel: Button
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_delete_player_from_team)

        textViewConfirm = findViewById(R.id.textViewConfirm)
        buttonDeletePlayer = findViewById(R.id.buttonDeletePlayerTeam)
        buttonCancel = findViewById(R.id.buttonCancel)

        textViewConfirm.text = "Are you sure you want to remove $playerName from $teamName?"

        buttonCancel.setOnClickListener {
            dismiss()
        }

        buttonDeletePlayer.setOnClickListener {
            deletePlayerFromTeam()
            dismiss()
            val intent = Intent(context, ViewTeamActivity   ::class.java)
            context.startActivity(intent)
        }
    }

    private fun deletePlayerFromTeam() {
        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        if (user == null) {
            Log.e("DeletePlayerDialog", "User not authenticated.")
            return
        }

        val database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString())

        // Odniesienie do listy zawodników w drużynie
        val teamRef = database.child("Teams").child(teamName).child("players")

        teamRef.get().addOnSuccessListener { snapshot ->
            Log.d("DeletePlayerDialog", "Snapshot value: ${snapshot.value}")

            val playersList = snapshot.value as? List<String>

            if (playersList != null) {
                Log.d("DeletePlayerDialog", "Fetched players list: $playersList")
                Log.d("DeletePlayerDialog", "Player name to remove: $playerName")

                val updatedPlayersList = playersList.filterNot { it.equals(playerName, ignoreCase = true) }

                // Zapisujemy zaktualizowaną listę w Firebase
                teamRef.setValue(updatedPlayersList).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("DeletePlayerDialog", "$playerName removed from $teamName's players list.")
                    } else {
                        Log.e("DeletePlayerDialog", "Failed to remove player from team: ${task.exception?.message}")
                    }
                }
            } else {
                Log.e("DeletePlayerDialog", "Failed to retrieve players list or list is empty. Snapshot value: ${snapshot.value}")
            }
        }.addOnFailureListener { exception ->
            Log.e("DeletePlayerDialog", "Failed to retrieve players list: ${exception.message}")
        }

        // Usunięcie drużyny z referencji zawodnika
        val playerRef = database.child("Players").child(playerName.trim())

        // Pobierz drużyny zawodnika
        playerRef.child("team").get().addOnSuccessListener { snapshot ->
            val teamsList = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()

            if (teamsList.isNotEmpty()) {
                Log.d("DeletePlayerDialog", "Fetched teams for player: $teamsList")

                if (teamName == "Favorites") {
                    // Jeśli drużyna to "Favorites", ustawiamy isFavorite na false
                    playerRef.child("isFavorite").setValue(false).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("DeletePlayerDialog", "Set isFavorite to false for player ${playerName.trim()}.")
                        } else {
                            Log.e("DeletePlayerDialog", "Failed to set isFavorite to false: ${task.exception?.message}")
                        }
                    }
                } else {
                    // Jeśli drużyna nie jest "Favorites", usuń ją z listy
                    val updatedTeamsList = teamsList.filterNot { it.equals(teamName, ignoreCase = true) }

                    playerRef.child("team").setValue(updatedTeamsList).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("DeletePlayerDialog", "$teamName removed from player's team list.")
                        } else {
                            Log.e("DeletePlayerDialog", "Failed to update team list for player: ${task.exception?.message}")
                        }
                    }
                }
            } else {
                Log.e("DeletePlayerDialog", "Player has no teams assigned or failed to fetch teams.")
            }
        }.addOnFailureListener { exception ->
            Log.e("DeletePlayerDialog", "Failed to retrieve team list for player: ${exception.message}")
        }
    }



}
