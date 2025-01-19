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
import com.anw.tenistats.stats.Player
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SelectPlayersForCalendarDialog(
    context: Context,
    private var selectedPlayers: ArrayList<String>,
    private var isCoachChecked: Boolean
) : Dialog(context) {

    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    //private var isCoachChecked: Boolean = true
    private var selectedCount: Int = 0
    //private val selectedPlayers: ArrayList<Player> = ArrayList()

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_select_players_for_calendar)

        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference(user.toString()).child("Players")

        val playersContainer = findViewById<LinearLayout>(R.id.playersContainer)
        val buttonCancel = findViewById<Button>(R.id.buttonCancel)
        val buttonAdd = findViewById<Button>(R.id.buttonSelect)
        val counter = findViewById<TextView>(R.id.textViewCounter)

        // Add "Coach" checkbox
        val coachCheckBox = CheckBox(context).apply {
            text = "Coach"
            isChecked = true
            setOnCheckedChangeListener { _, isChecked ->
                isCoachChecked = isChecked
            }
        }
        playersContainer.addView(coachCheckBox)

        // Fetch players and populate the list
        fetchAndDisplayPlayers(playersContainer, counter)

        buttonCancel.setOnClickListener {
            dismiss()
        }

        buttonAdd.setOnClickListener {
            val intent = Intent(context, CalendarCoachActivity::class.java).apply {
                putExtra("selectedPlayers", selectedPlayers)
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

                // Add "Coach" checkbox at the top
                val coachCheckBox = CheckBox(context).apply {
                    text = "Coach"
                    isChecked = true
                    setOnCheckedChangeListener { _, isChecked ->
                        isCoachChecked = isChecked
                    }
                }
                playersContainer.addView(coachCheckBox)

                for (playerSnapshot in snapshot.children) {
                    val playerName = playerSnapshot.child("player").getValue(String::class.java)
                    val active = playerSnapshot.child("active").getValue(Boolean::class.java)

                    if (playerName != null && active == true) {
                        //val player = Player(playerName)
                        val checkBox = CheckBox(context).apply {
                            text = playerName
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
                // Update counter text initially
                counter.text = "$selectedCount/4"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load players: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
