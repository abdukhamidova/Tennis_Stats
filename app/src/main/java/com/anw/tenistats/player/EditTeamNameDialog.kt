package com.anw.tenistats.player

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import com.anw.tenistats.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditTeamNameDialog(private val context: Context, private val team: TeamView) {

    private lateinit var alertDialog: AlertDialog
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userRef: DatabaseReference

    // Add this property for the listener
    private var teamUpdateListener: TeamUpdateListener? = null

    @SuppressLint("MissingInflatedId")
    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.edit_team_name_dialog, null)
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

    // Method to set the listener
    fun setTeamUpdateListener(listener: TeamUpdateListener) {
        teamUpdateListener = listener
    }

    private fun updateTeamNameInDatabase(newTeamName: String) {
        val oldTeamName = team.name

        // Create the new team and copy players
        val updatedPlayers = ArrayList(team.players) // Copy the player list

        // Create new team
        val newTeam = TeamView(newTeamName, updatedPlayers)

        // Reference to the new team in "Teams"
        val newTeamRef = database.child("Teams").child(newTeamName)

        // Save the new team in Firebase
        newTeamRef.setValue(newTeam).addOnSuccessListener {
            // Copy players to the new team
            copyPlayersToNewTeam(oldTeamName, newTeamName)

            // Delete the old team from the database
            database.child("Teams").child(oldTeamName).removeValue().addOnSuccessListener {
                Log.d("EditTeamNameDialog", "Team name changed from $oldTeamName to $newTeamName")

                // Notify the listener that the team has been updated
                teamUpdateListener?.onTeamUpdated(newTeam)  // Call the listener in ViewTeamActivity

                // Update the local team list and refresh the UI in the activity
                val activity = context as? ViewTeamActivity
                activity?.onTeamUpdated(newTeam)  // Update the activity with the new team
            }.addOnFailureListener { exception ->
                Log.e("EditTeamNameDialog", "Error deleting old team: $exception")
            }
        }.addOnFailureListener { exception ->
            Log.e("EditTeamNameDialog", "Error saving new team: $exception")
        }
    }

    private fun copyPlayersToNewTeam(oldTeamName: String, newTeamName: String) {
        val oldTeamPlayersRef = database.child("Teams").child(oldTeamName).child("players")
        val newTeamPlayersRef = database.child("Teams").child(newTeamName).child("players")

        // Get the list of players from the old team
        oldTeamPlayersRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Save the list of players in the new team
                newTeamPlayersRef.setValue(snapshot.value).addOnSuccessListener {
                    Log.d("EditTeamNameDialog", "Players list copied to the new team")
                }.addOnFailureListener { exception ->
                    Log.e("EditTeamNameDialog", "Error copying players to the new team: $exception")
                }
            } else {
                Log.d("EditTeamNameDialog", "No players found in team: $oldTeamName")
            }
        }.addOnFailureListener { exception ->
            Log.e("EditTeamNameDialog", "Error retrieving players from team: $exception")
        }
    }

    private fun updatePlayersTeamName(oldTeamName: String, newTeamName: String) {
        // Get reference to the players node
        userRef.get().addOnSuccessListener { snapshot ->
            for (playerSnapshot in snapshot.children) {
                // Check if the player's team is the old team
                val playerTeamName = playerSnapshot.child("team").getValue(String::class.java)
                if (playerTeamName == oldTeamName) {
                    // Update the player's team name
                    playerSnapshot.ref.child("team").setValue(newTeamName).addOnSuccessListener {
                        Log.d("EditTeamNameDialog", "Updated player ${playerSnapshot.key}'s team")
                    }.addOnFailureListener { exception ->
                        Log.e("EditTeamNameDialog", "Error updating player's team: $exception")
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("EditTeamNameDialog", "Error retrieving players: $exception")
        }
    }

    // Listener interface to notify the activity
    interface TeamUpdateListener {
        fun onTeamUpdated(updatedTeam: TeamView)
    }
}
