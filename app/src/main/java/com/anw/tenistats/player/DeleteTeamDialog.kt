package com.anw.tenistats.player

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.anw.tenistats.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DeleteTeamDialog(
    context: Context,
    private val team: TeamView
) : Dialog(context) {

    private lateinit var textViewConfirm: TextView
    private lateinit var textViewPlayerName: TextView
    private lateinit var buttonDeleteTeam: Button
    private lateinit var buttonCancel: Button
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.delete_team_dialog) // Użyj odpowiedniego layoutu

        textViewConfirm = findViewById(R.id.textViewConfirm)
        buttonDeleteTeam = findViewById(R.id.buttonDeleteTeam)
        buttonCancel = findViewById(R.id.buttonCancelP)

        textViewConfirm.text = "Would you like to delete group ${team.name}" // Ustawienie nazwy drużyny

        buttonCancel.setOnClickListener {
            dismiss()
        }

        buttonDeleteTeam.setOnClickListener {
            deleteTeamFromDatabase()
            dismiss()
            val intent = Intent(context, ViewTeamActivity   ::class.java)
            context.startActivity(intent)
        }
    }

    private fun deleteTeamFromDatabase() {
        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        val database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference(user.toString())

        database.child("Teams").child(team.name).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                team.players.forEach { playerName ->
                    database.child("Players").child(playerName).child("team").setValue(null)
                }
            }
        }
    }

}
