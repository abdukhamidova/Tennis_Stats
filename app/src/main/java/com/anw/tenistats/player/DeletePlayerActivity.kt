package com.anw.tenistats.player

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anw.tenistats.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator

class DeletePlayerActivity(private val context: Context)  {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var alertDialog: AlertDialog

    @SuppressLint("MissingInflatedId")
    fun show(playerName: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.activity_delete_player, null)
        alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val btnCancel: Button = dialogView.findViewById(R.id.buttonCancelP)
        val btnDelete: Button = dialogView.findViewById(R.id.buttonDeleteP)

        val name :TextView = dialogView.findViewById(R.id.textViewPlayerName)
        name.text=playerName

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnDelete.setOnClickListener {
            firebaseAuth = FirebaseAuth.getInstance()
            val user = firebaseAuth.currentUser?.uid
            database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(user.toString())

            val playerRef = database.child("Players").child(playerName)
            val teamRef = database.child("Teams") // Referencja do wszystkich drużyn

            playerRef.child("active").setValue(false)
                .addOnSuccessListener {
                    // Pobierz drużyny, w których zawodnik występuje
                    playerRef.child("teams").get()
                        .addOnSuccessListener { snapshot ->
                            val teams = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()
                            for (teamName in teams) {
                                val playersRef = teamRef.child(teamName).child("players")
                                playersRef.get()
                                    .addOnSuccessListener { playersSnapshot ->
                                        val players = playersSnapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {}) ?: mutableListOf()
                                        if (players.contains(playerName)) {
                                            players.remove(playerName)
                                            playersRef.setValue(players)
                                        }

                                    }
                            }
                        }

                    Toast.makeText(
                        context,
                        "Player successfully deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                    alertDialog.dismiss()

                    val intent = Intent(context, ViewPlayerActivity::class.java)
                    context.startActivity(intent) // Uruchomienie nowej aktywności
                }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        "Failed to delete player",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
        alertDialog.show()
    }
}
