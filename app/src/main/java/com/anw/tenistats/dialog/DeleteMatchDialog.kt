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

class DeleteMatchDialog(private val context: Context) {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var alertDialog: AlertDialog

    fun show(matchId:String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.activity_delete_match, null)
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
                .getReference(user.toString()).child("Matches").child(matchId)

            // Implement the delete functionality here
            database.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(
                        context,
                        "Match successfully deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                    alertDialog.dismiss()
                    // Optionally, you can notify the user here
                }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        "Failed to delete match",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        alertDialog.show()
    }
}
