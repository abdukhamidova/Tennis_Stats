package com.anw.tenistats.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.anw.tenistats.R
import com.anw.tenistats.player.TeamView
import com.anw.tenistats.player.ViewTeamActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator

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
        setContentView(R.layout.dialog_delete_team) // Użyj odpowiedniego layoutu

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
        if (user == null) {
            return
        }

        val database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString())

        // Usuń drużynę z bazy danych
        database.child("Teams").child(team.name).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                team.players.forEach { playerName ->
                    val playerRef = database.child("Players").child(playerName).child("team")

                    // Pobierz aktualną listę drużyn zawodnika
                    playerRef.get().addOnSuccessListener { snapshot ->
                        val teamsList = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()

                        if (teamsList.isNotEmpty()) {
                            // Usuń tylko drużynę `team.name` z listy
                            val updatedTeamsList = teamsList.filterNot { it.equals(team.name, ignoreCase = true) }

                            // Zaktualizuj listę drużyn zawodnika w bazie danych
                            playerRef.setValue(updatedTeamsList).addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    // Opcjonalnie: logowanie dla celów debugowania
                                    android.util.Log.d("DeleteTeamDialog", "Removed ${team.name} from $playerName's team list.")
                                } else {
                                    android.util.Log.e("DeleteTeamDialog", "Failed to update team list for $playerName: ${updateTask.exception?.message}")
                                }
                            }
                        }
                    }.addOnFailureListener { exception ->
                        // Obsługa błędu pobierania danych
                        android.util.Log.e("DeleteTeamDialog", "Failed to retrieve team list for $playerName: ${exception.message}")
                    }
                }
            } else {
                android.util.Log.e("DeleteTeamDialog", "Failed to delete team: ${task.exception?.message}")
            }
        }
    }


}
