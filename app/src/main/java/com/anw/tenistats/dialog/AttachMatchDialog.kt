package com.anw.tenistats.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import com.anw.tenistats.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AttachMatchDialog(private val context: Context) {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var alertDialog: AlertDialog

    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_attach_match, null)
        alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val btnCancel: Button = dialogView.findViewById(R.id.buttonCancel)
        val btnAdd: Button = dialogView.findViewById(R.id.buttonAdd)
        val btnPlay: Button = dialogView.findViewById(R.id.buttonPlay)
        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnPlay.setOnClickListener {
            /*firebaseAuth = FirebaseAuth.getInstance()
            val user = firebaseAuth.currentUser?.uid
            database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Tournaments").child(tournamentId!!)
            */
                    alertDialog.dismiss()
                    // Optionally, you can notify the user here
        }
        btnAdd.setOnClickListener {
            /*firebaseAuth = FirebaseAuth.getInstance()
            val user = firebaseAuth.currentUser?.uid
            database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Tournaments").child(tournamentId!!)
            */
                    alertDialog.dismiss()
                    // Optionally, you can notify the user here
        }
        btnCancel.setOnClickListener {
            alertDialog.dismiss()
            // Optionally, you can notify the user here
        }

        alertDialog.show()
    }
}