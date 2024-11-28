package com.anw.tenistats.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import com.anw.tenistats.R
import com.anw.tenistats.player.TeamView
import com.anw.tenistats.player.ViewTeamActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator

class EditTeamNameDialog(private val context: Context, private val team: TeamView) {

    private lateinit var alertDialog: AlertDialog
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userRef: DatabaseReference

    // Add this property for the listener
    private var teamUpdateListener: TeamUpdateListener? = null

    @SuppressLint("MissingInflatedId")
    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_team_name, null)
        alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference(user.toString())
        userRef = database.child("Players")

        val editTextNewTeamName: EditText = dialogView.findViewById(R.id.editTextNewTeamName)
        editTextNewTeamName.setText(team.name)

        val buttonCancel: Button=dialogView.findViewById(R.id.buttonCancelChange)
        buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        val buttonChangeName: Button = dialogView.findViewById(R.id.buttonChangeName)
        buttonChangeName.setOnClickListener {
            val newTeamName = editTextNewTeamName.text.toString().trim()
            if (newTeamName.isNotEmpty() && newTeamName != team.name) {
                updateTeamNameInDatabase(newTeamName)

                alertDialog.dismiss()
                val intent = Intent(context, ViewTeamActivity   ::class.java)
                context.startActivity(intent)
            } else {
                editTextNewTeamName.error = context.getString(R.string.error_empty_name)
            }
        }
        alertDialog.show()
    }


    fun setTeamUpdateListener(listener: TeamUpdateListener) {
        teamUpdateListener = listener
    }

    private fun updateTeamNameInDatabase(newTeamName: String) {
        val oldTeamName = team.name

        val updatedPlayers = ArrayList(team.players) // Copy the player list
        val newTeam = TeamView(newTeamName, updatedPlayers)
        val newTeamRef = database.child("Teams").child(newTeamName)

        newTeamRef.setValue(newTeam).addOnSuccessListener {
            updatePlayersTeamName(oldTeamName, newTeamName)

            database.child("Teams").child(oldTeamName).removeValue().addOnSuccessListener {
                Log.d("EditTeamNameDialog", "Team name changed from $oldTeamName to $newTeamName")

                teamUpdateListener?.onTeamUpdated(newTeam)

                val activity = context as? ViewTeamActivity
                activity?.onTeamUpdated(newTeam)
            }.addOnFailureListener { exception ->
                Log.e("EditTeamNameDialog", "Error deleting old team: $exception")
            }
        }.addOnFailureListener { exception ->
            Log.e("EditTeamNameDialog", "Error saving new team: $exception")
        }
    }

    private fun updatePlayersTeamName(oldTeamName: String, newTeamName: String) {
        userRef.get().addOnSuccessListener { snapshot ->
            for (playerSnapshot in snapshot.children) {
                val teamsList = playerSnapshot.child("team").getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()

                if (teamsList.contains(oldTeamName)) {
                    val updatedTeamsList = teamsList.map { teamName ->
                        if (teamName == oldTeamName) newTeamName else teamName
                    }

                    playerSnapshot.ref.child("team").setValue(updatedTeamsList).addOnSuccessListener {
                        Log.d("EditTeamNameDialog", "Updated player ${playerSnapshot.key}'s team list: $updatedTeamsList")
                    }.addOnFailureListener { exception ->
                        Log.e("EditTeamNameDialog", "Error updating player's team list: $exception")
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("EditTeamNameDialog", "Error retrieving players: $exception")
        }
    }

    interface TeamUpdateListener {
        fun onTeamUpdated(updatedTeam: TeamView)
    }
}
