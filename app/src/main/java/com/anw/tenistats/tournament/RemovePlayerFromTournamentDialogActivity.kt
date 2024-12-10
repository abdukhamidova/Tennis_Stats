package com.anw.tenistats.tournament

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

class RemovePlayerFromTournamentDialogActivity (
    context: Context,
    private val tournamentId: String,
    private val playerName: String
) : Dialog(context) {

    private lateinit var textViewConfirm: TextView
    private lateinit var buttonDeletePlayer: Button
    private lateinit var buttonCancel: Button
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_remove_player_from_tournament)

        textViewConfirm = findViewById(R.id.textViewConfirm)
        buttonDeletePlayer = findViewById(R.id.buttonDeletePlayer)
        buttonCancel = findViewById(R.id.buttonCancel)

        var tournamentName: String? = null
        firebaseAuth = FirebaseAuth.getInstance()
        val databaseName = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference().child("Tournaments").child(tournamentId).child("name")
        databaseName.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    tournamentName = snapshot.getValue(String::class.java)
                    textViewConfirm.text = "Are you sure you want to remove $playerName from $tournamentName?"

                }
            }


        buttonCancel.setOnClickListener {
            dismiss()
        }

        buttonDeletePlayer.setOnClickListener {
            deletePlayerFromTournament()
            dismiss()
        }
    }

    private fun deletePlayerFromTournament() {
        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        val database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString()).child("Players").child(playerName).child("tournaments")

        // Pobieramy listę turniejów zawodnika
        database.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                Log.d("DeletePlayerDialog", "Snapshot value: ${snapshot.value}")

                val tournamentsList = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
                if (tournamentsList != null && tournamentsList.contains(tournamentId)) {
                    // Tworzymy nową listę bez `tournamentId`
                    val updatedTournamentsList = tournamentsList.filterNot { it == tournamentId }

                    // Aktualizujemy węzeł "tournaments"
                    database.setValue(updatedTournamentsList).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("DeletePlayerDialog", "$tournamentId removed from $playerName's tournaments.")
                        } else {
                            Log.e("DeletePlayerDialog", "Failed to remove tournament: ${task.exception?.message}")
                        }
                    }
                } else {
                    Log.w("DeletePlayerDialog", "Tournament ID $tournamentId not found in player's tournaments.")
                }
            } else {
                Log.e("DeletePlayerDialog", "Tournaments node is empty or does not exist.")
            }
        }.addOnFailureListener { exception ->
            Log.e("DeletePlayerDialog", "Failed to retrieve tournaments list: ${exception.message}")
        }
    }

}
