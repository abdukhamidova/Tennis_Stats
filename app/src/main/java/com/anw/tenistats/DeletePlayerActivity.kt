package com.anw.tenistats

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DeletePlayerActivity(private val context: Context) {
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
                .getReference(user.toString()).child("Players").child(playerName)

            database.child("active").setValue(false)
                .addOnSuccessListener {
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
