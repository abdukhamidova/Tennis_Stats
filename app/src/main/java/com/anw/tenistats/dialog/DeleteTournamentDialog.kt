package com.anw.tenistats.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import com.anw.tenistats.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DeleteTournamentDialog(private val context: Context) {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var alertDialog: AlertDialog

    fun show(tournamentId: String?) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete_tournament, null)
        alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val btnCancel: Button = dialogView.findViewById(R.id.buttonCancel)
        val btnDelete: Button = dialogView.findViewById(R.id.buttonDelete)
        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnDelete.setOnClickListener {
            firebaseAuth = FirebaseAuth.getInstance()
            val user = firebaseAuth.currentUser?.uid
            database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Tournaments").child(tournamentId!!)

            // Remove the tournament entry
            database.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(
                        context,
                        "Tournament successfully deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                    alertDialog.dismiss()

                    // Call to remove tournament from all players
                    removeTournamentFromPlayers(tournamentId)
                }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        "Failed to delete tournament",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        alertDialog.show()
    }

    private fun removeTournamentFromPlayers(tournamentId: String) {
        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        val playersRef = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString()).child("Players")

        playersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (playerSnapshot in snapshot.children) {
                    val tournamentsRef = playerSnapshot.child("tournaments").ref
                    val tournamentsList = playerSnapshot.child("tournaments").children
                    for (tournament in tournamentsList) {
                        if (tournament.key == tournamentId) {
                            tournamentsRef.child(tournamentId).removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "Removed tournament from player: ${playerSnapshot.key}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        "Failed to remove tournament from player: ${playerSnapshot.key}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Failed to fetch players: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}