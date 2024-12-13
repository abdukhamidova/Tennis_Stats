package com.anw.tenistats.tournament

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.anw.tenistats.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChangesDialog(context: Context, private val tournamentId: String, private val matchNumber: String, private val drawSize: String) : Dialog(context) {

    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    init {
        // Ustawienie widoku dialogu
        setContentView(R.layout.dialog_changes)

        val buttonCancel = findViewById<Button>(R.id.buttonCancel)
        val buttonUpdate = findViewById<Button>(R.id.buttonUpdate)
        val textViewConfirm = findViewById<TextView>(R.id.TextViewChanges)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Tournaments").child(tournamentId).child(matchNumber)

        // Sprawdzamy zmiany w bazie
        checkChanges { changesCount ->
            // Zmieniamy tekst w dialogu, pokazując liczbę zmian
            textViewConfirm.text ="Number of changes: $changesCount"
        }

        // Przycisk Cancel - zamyka dialog
        buttonCancel.setOnClickListener {
            dismiss()
        }

        // Przycisk Update - otwiera odpowiednią aktywność
        buttonUpdate.setOnClickListener {
            val intent = Intent(context, ChangeEditMatchActivity::class.java)
            intent.putExtra("tournament_id", tournamentId)
            intent.putExtra("match_number", matchNumber)
            intent.putExtra("draw_size", drawSize)
            context.startActivity(intent)
            dismiss()
        }
    }

    private fun checkChanges(callback: (Int) -> Unit) {
        val keys = listOf("player1Edit", "player2Edit", "set1p1Edit", "set1p2Edit", "set2p1Edit", "set2p2Edit","set3p1Edit","set3p2Edit", "winnerEdit")
        var changesCount = 0

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (key in keys) {
                    val value = snapshot.child(key).getValue(String::class.java)
                    if (value != null && value != "None") {
                        changesCount++
                    }
                }
                callback(changesCount)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error: ${error.message}")
            }
        })
    }
}