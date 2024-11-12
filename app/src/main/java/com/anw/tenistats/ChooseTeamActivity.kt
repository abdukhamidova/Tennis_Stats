package com.anw.tenistats

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ChooseTeamDialog(private val context: Context, private val playerName: String) {

    private lateinit var alertDialog: AlertDialog
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private val teamList = ArrayList<TeamView>() // Lista drużyn z bazy
    private lateinit var spinnerTeamList: Spinner

    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.choose_team_dialog, null)
        alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString())

        val textPlayerName: TextView = dialogView.findViewById(R.id.textViewChoose)
        textPlayerName.text = "Choose a team for $playerName"

        // Przygotowanie Spinnera
        spinnerTeamList = dialogView.findViewById(R.id.spinnerTeamList)

        // Pobieranie listy drużyn z Firebase
        getTeamsFromDatabase()

        // Wyświetlenie pola do wpisania nowej drużyny
        val buttonPlus: Button = dialogView.findViewById(R.id.buttonPlus)
        val textTeamName: EditText = dialogView.findViewById(R.id.textViewTeamName)
        buttonPlus.setOnClickListener {
            textTeamName.visibility = View.VISIBLE
        }

        // Dodanie drużyny do listy (po kliknięciu "Add to team")
        val buttonAddToTeam: Button = dialogView.findViewById(R.id.buttonAddToTeam)
        buttonAddToTeam.setOnClickListener {
            val selectedTeam = if (textTeamName.visibility == View.VISIBLE && textTeamName.text.isNotEmpty()) {
                // Dodanie nowej drużyny
                val newTeamName = textTeamName.text.toString()
                val newTeam = TeamView(name = newTeamName, players = arrayListOf(playerName))

                // Dodanie nowej drużyny do Firebase
                addNewTeamToDatabase(newTeam)

                newTeam
            } else {
                // Wybranie istniejącej drużyny
                val selectedPosition = spinnerTeamList.selectedItemPosition
                val selectedTeam = teamList[selectedPosition]

                // Dodanie nowego zawodnika do listy graczy drużyny, jeśli nie ma go już na liście
                if (!selectedTeam.players.contains(playerName)) {
                    selectedTeam.players.add(playerName)  // Dodaj zawodnika do listy
                }

                // Aktualizacja drużyny w bazie
                updateTeamInDatabase(selectedTeam)

                selectedTeam
            }

            // Aktualizowanie drużyny gracza w bazie
            updatePlayerTeamInDatabase(selectedTeam.name)

            val intent = Intent(context, ViewPlayerActivity::class.java)
            intent.putExtra("PLAYER_NAME", playerName)
            context.startActivity(intent)

            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    // Funkcja do pobierania drużyn z bazy danych Firebase
    private fun getTeamsFromDatabase() {
        database.child("Teams").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                teamList.clear()
                for (teamSnapshot in snapshot.children) {
                    val team = teamSnapshot.getValue(TeamView::class.java)
                    team?.let { teamList.add(it) }
                }

                // Aktualizacja adaptera Spinnera
                val adapter = ArrayAdapter(context, R.layout.spinner_item_team_base, teamList.map { it.name })
                adapter.setDropDownViewResource(R.layout.spinner_item_team)
                spinnerTeamList.adapter = adapter
            }
        }.addOnFailureListener { exception ->
            Log.e("ChooseTeamDialog", "Błąd podczas pobierania drużyn: $exception")
        }
    }

    // Funkcja do dodawania nowej drużyny do bazy
    private fun addNewTeamToDatabase(team: TeamView) {
        val teamId = team.name // Zakładając, że nazwa drużyny jest unikalna
        database.child("Teams").child(teamId).setValue(team).addOnSuccessListener {
            Log.d("ChooseTeamDialog", "Dodano nową drużynę: ${team.name}")
        }.addOnFailureListener { exception ->
            Log.e("ChooseTeamDialog", "Błąd podczas dodawania nowej drużyny: $exception")
        }
    }

    // Funkcja do aktualizacji drużyny w bazie danych
    private fun updateTeamInDatabase(team: TeamView) {
        val teamId = team.name
        val teamRef = database.child("Teams").child(teamId)

        // Dodajemy zawodnika do drużyny, jeśli jeszcze go tam nie ma
        teamRef.child("players").get().addOnSuccessListener { snapshot ->
            val players = snapshot.value as? ArrayList<String> ?: ArrayList()

            if (!players.contains(playerName)) {
                players.add(playerName)  // Dodajemy zawodnika do listy

                // Aktualizujemy drużynę i jej listę graczy w bazie
                teamRef.child("players").setValue(players)

                Log.d("ChooseTeamDialog", "Zaktualizowano drużynę: ${team.name}")
            }
        }.addOnFailureListener { exception ->
            Log.e("ChooseTeamDialog", "Błąd podczas aktualizacji drużyny: $exception")
        }
    }

    // Funkcja do aktualizacji drużyny gracza w bazie danych
    private fun updatePlayerTeamInDatabase(teamName: String) {
        val playerRef = database.child("Players").child(playerName)

        // Sprawdzamy, czy zawodnik istnieje w bazie, jeśli tak, przypisujemy drużynę
        playerRef.child("team").setValue(teamName).addOnSuccessListener {
            Log.d("ChooseTeamDialog", "Zaktualizowano drużynę dla zawodnika: $playerName")
        }.addOnFailureListener { exception ->
            Log.e("ChooseTeamDialog", "Błąd podczas aktualizacji drużyny zawodnika: $exception")
        }
    }
}
