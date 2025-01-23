package com.anw.tenistats.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.anw.tenistats.CalendarCoachActivity
import com.anw.tenistats.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SelectPlayersForCalendarDialog(
    context: Context,
    var selectedPlayers: ArrayList<String>,
    var isCoachChecked: Boolean
) : Dialog(context) {

    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private var selectedCount: Int = 0

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_select_players_for_calendar)

        // Ustaw liczbę zaznaczonych zawodników na podstawie `selectedPlayers`
        selectedCount = selectedPlayers.size

        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference(user.toString()).child("Players")

        val playersContainer = findViewById<LinearLayout>(R.id.playersContainer)
        val buttonCancel = findViewById<Button>(R.id.buttonCancel)
        val buttonAdd = findViewById<Button>(R.id.buttonSelect)
        val counter = findViewById<TextView>(R.id.textViewCounter)

        // Dodaj "Coach" checkbox
        val coachCheckBox = CheckBox(context).apply {
            text = "Coach"
            isChecked = isCoachChecked // Odtwórz stan "Coach"
            setOnCheckedChangeListener { _, isChecked ->
                isCoachChecked = isChecked
            }
        }
        playersContainer.addView(coachCheckBox)

        // Pobierz zawodników z bazy danych i wyświetl ich w liście
        fetchAndDisplayPlayers(playersContainer, counter)

        buttonCancel.setOnClickListener {
            dismiss()
        }

        buttonAdd.setOnClickListener {
            // Przekaż aktualnie zaznaczonych zawodników do CalendarCoachActivity
            val intent = Intent(context, CalendarCoachActivity::class.java).apply {
                putExtra("selectedPlayers", ArrayList(selectedPlayers)) // Kopia listy
                putExtra("isCoachChecked", isCoachChecked)
            }
            context.startActivity(intent)
            dismiss()
        }
    }

    private fun fetchAndDisplayPlayers(playersContainer: LinearLayout, counter: TextView) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                playersContainer.removeAllViews()

                // Dodaj "Coach" checkbox
                val coachCheckBox = CheckBox(context).apply {
                    text = "Coach"
                    isChecked = isCoachChecked // Odtwórz stan "Coach"
                    setOnCheckedChangeListener { _, isChecked ->
                        isCoachChecked = isChecked
                    }
                }
                playersContainer.addView(coachCheckBox)

                for (playerSnapshot in snapshot.children) {
                    val playerName = playerSnapshot.child("player").getValue(String::class.java)
                    val active = playerSnapshot.child("active").getValue(Boolean::class.java)

                    if (playerName != null && active == true) {
                        val isCheckedInitially = selectedPlayers.contains(playerName) // Sprawdź, czy zawodnik był wcześniej zaznaczony
                        val checkBox = CheckBox(context).apply {
                            text = playerName
                            isChecked = isCheckedInitially
                            setOnCheckedChangeListener { buttonView, isChecked ->
                                if (isChecked) {
                                    if (selectedCount < 4) {
                                        selectedPlayers.add(playerName)
                                        selectedCount++
                                        counter.text = "$selectedCount/4"
                                    } else {
                                        Toast.makeText(context, "You can select up to 4 players only", Toast.LENGTH_SHORT).show()
                                        buttonView.isChecked = false
                                    }
                                } else {
                                    selectedPlayers.remove(playerName)
                                    selectedCount--
                                    counter.text = "$selectedCount/4"
                                }
                            }
                        }
                        playersContainer.addView(checkBox)
                    }
                }
                // Zaktualizuj licznik po załadowaniu listy
                counter.text = "$selectedCount/4"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load players: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
