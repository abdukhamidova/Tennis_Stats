package com.anw.tenistats.player

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.anw.tenistats.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddTeamDialog(private val context: Context) {

    fun show() {
        // Tworzymy dialog
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.add_new_team_dialog) // musisz stworzyć odpowiedni layout XML dla tego dialogu
        dialog.setTitle("Add Team")

        // Pobieramy widoki z dialogu
        val teamNameEditText = dialog.findViewById<EditText>(R.id.textViewTeamName)
        val addButton = dialog.findViewById<Button>(R.id.buttonAdd)
        val cancelButton = dialog.findViewById<Button>(R.id.buttonCancel)

        addButton.setOnClickListener {
            val teamName = teamNameEditText.text.toString().trim()

            if (teamName.isNotEmpty()) {
                addTeamToDatabase(teamName)
                dialog.dismiss()
                val intent = Intent(context, ViewTeamActivity   ::class.java)
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Team name cannot be empty!", Toast.LENGTH_SHORT).show()
            }
        }
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addTeamToDatabase(teamName: String) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser
        if (user != null) {
            val database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(user.uid)
                .child("Teams")

            database.child(teamName.trim()).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    Toast.makeText(context, "Team with this name already exists!", Toast.LENGTH_SHORT).show()
                } else {
                    database.child(teamName).setValue(teamName)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Team added successfully!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error adding team. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    database.child(teamName).child("name").setValue(teamName)
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Error checking if team exists. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
