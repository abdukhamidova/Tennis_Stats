package com.anw.tenistats.player

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.anw.tenistats.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ChooseTeamDialog(private val context: Context, private val playerName: String) {

    private lateinit var alertDialog: AlertDialog
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private val teamList = ArrayList<TeamView>()
    private lateinit var spinnerTeamList: Spinner

    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_choose_team, null)
        alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference(user.toString())

        val textPlayerName: TextView = dialogView.findViewById(R.id.textViewChoose)
        textPlayerName.text = "Choose a team for $playerName"


        spinnerTeamList = dialogView.findViewById(R.id.spinnerTeamList)

        getTeamsFromDatabase()

        val buttonPlus: TextView = dialogView.findViewById(R.id.buttonPlus)
        val textTeamName: EditText = dialogView.findViewById(R.id.textViewTeamName)
        buttonPlus.setOnClickListener {
            textTeamName.visibility = View.VISIBLE
        }

        val buttonAddToTeam: Button = dialogView.findViewById(R.id.buttonAddToTeam)
        buttonAddToTeam.setOnClickListener {
            val selectedTeam = if (textTeamName.visibility == View.VISIBLE && textTeamName.text.isNotEmpty()) {
                val newTeamName = textTeamName.text.toString()
                val newTeam = TeamView(name = newTeamName, players = arrayListOf(playerName))

                addNewTeamToDatabase(newTeam)

                newTeam
            } else {
                val selectedPosition = spinnerTeamList.selectedItemPosition
                val selectedTeam = teamList[selectedPosition]

                if (!selectedTeam.players.contains(playerName)) {
                    selectedTeam.players.add(playerName)
                }
                updateTeamInDatabase(selectedTeam)

                selectedTeam
            }
            updatePlayerTeamInDatabase(selectedTeam.name)

            val intent = Intent(context, PlayerDetailsActivity::class.java)
            intent.putExtra("playerId", playerName)
            context.startActivity(intent)

            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun getTeamsFromDatabase() {
        database.child("Teams").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                teamList.clear()
                for (teamSnapshot in snapshot.children) {
                    val team = teamSnapshot.getValue(TeamView::class.java)
                    team?.let {
                        if (it.name != "Favorites") {
                            teamList.add(it)
                        }
                    }
                }
                val adapter = ArrayAdapter(context, R.layout.spinner_item_team_base, teamList.map { it.name })
                adapter.setDropDownViewResource(R.layout.spinner_item_team)
                spinnerTeamList.adapter = adapter
            }
        }.addOnFailureListener { exception ->
            Log.e("ChooseTeamDialog", "Błąd podczas pobierania drużyn: $exception")
        }
    }

    private fun addNewTeamToDatabase(team: TeamView) {
        val teamId = team.name


        database.child("Teams").child(teamId.trim()).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                Toast.makeText(context, "Drużyna o nazwie '$teamId' już istnieje!", Toast.LENGTH_SHORT).show()
            } else {

                database.child("Teams").child(teamId).setValue(team).addOnSuccessListener {
                }.addOnFailureListener {
                }
            }
        }.addOnFailureListener { exception ->
        }
    }

    private fun updateTeamInDatabase(team: TeamView) {
        val teamId = team.name
        val teamRef = database.child("Teams").child(teamId)

        teamRef.child("players").get().addOnSuccessListener { snapshot ->
            val players = snapshot.value as? ArrayList<String> ?: ArrayList()

            if (!players.contains(playerName)) {
                players.add(playerName)

                teamRef.child("players").setValue(players)

                Log.d("ChooseTeamDialog", "Zaktualizowano drużynę: ${team.name}")
            }
        }.addOnFailureListener { exception ->
            Log.e("ChooseTeamDialog", "Błąd podczas aktualizacji drużyny: $exception")
        }
    }

    private fun updatePlayerTeamInDatabase(teamName: String) {
        if (teamName.trim() == "Favorites") {
            Log.e("ChooseTeamDialog", "Nie można przypisać drużyny 'Favorites' dla zawodnika: $playerName")
            Toast.makeText(context, "You cannot assign the 'Favorites' team to a player.", Toast.LENGTH_SHORT).show()
            return
        }

        val playerRef = database.child("Players").child(playerName)

        playerRef.child("team").get().addOnSuccessListener { snapshot ->
            val currentTeams = snapshot.value as? ArrayList<String> ?: ArrayList()

            if (!currentTeams.contains(teamName)) {
                currentTeams.add(teamName)

                playerRef.child("team").setValue(currentTeams).addOnSuccessListener {
                    Log.d("ChooseTeamDialog", "Zaktualizowano drużynę dla zawodnika: $playerName")
                }.addOnFailureListener { exception ->
                    Log.e("ChooseTeamDialog", "Błąd podczas aktualizacji drużyny zawodnika: $exception")
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("ChooseTeamDialog", "Błąd podczas pobierania drużyn gracza: $exception")
        }
    }


}
