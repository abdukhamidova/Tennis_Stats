package com.anw.tenistats

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.anw.tenistats.databinding.ActivityMatchShortSummaryBinding
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.anw.tenistats.matchplay.getGoldenDrawable
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityMatchShortSummary : AppCompatActivity() {
    private lateinit var binding: ActivityMatchShortSummaryBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private var userId = ""
    private lateinit var database: DatabaseReference

    private lateinit var pl1: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMatchShortSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //MENU
        firebaseAuth = FirebaseAuth.getInstance()
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.navigationViewMenu)
        val menu = findViewById<ImageButton>(R.id.buttonMenu)
        val headerView = navigationView.getHeaderView(0)
        menu.setOnClickListener{
            drawerLayout.open()
        }
        navigationDrawerHelper = NavigationDrawerHelper(this)
        navigationDrawerHelper.setupNavigationDrawer(drawerLayout, navigationView, firebaseAuth)
        val backButton = findViewById<ImageButton>(R.id.buttonUndo)
        backButton.visibility = View.GONE

        val userEmail = firebaseAuth.currentUser?.email.toString()
        val userEmailView = headerView.findViewById<TextView>(R.id.textViewUserEmail)
        if(userEmail.isNotEmpty()) {
            userEmailView.text = userEmail
        }else {
            userEmailView.text = resources.getString(R.string.user_email)
        }
        //MENU

        //pola w tabeli wyniku
        val player1 = binding.textviewPlayer1Stats
        val player2 = binding.textviewPlayer2Stats
        val serve1 = binding.textViewServe1Stats
        val serve2 = binding.textViewServe2Stats
        val set1p1 = binding.textViewP1Set1Stats
        val set2p1 = binding.textViewP1Set2Stats
        val set3p1 = binding.textViewP1Set3Stats
        val set1p2 = binding.textViewP2Set1Stats
        val set2p2 = binding.textViewP2Set2Stats
        val set3p2 = binding.textViewP2Set3Stats
        val pkt1 = binding.textViewPlayer1PktStats
        val pkt2 = binding.textViewPlayer2PktStats

        // Odbierz datę meczu w formacie milisekund z poprzedniej aktywności
        val matchDateInMillis = intent.getLongExtra("matchDateInMillis", 0L)
        // Sformatuj datę
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(matchDateInMillis))
        // Przypisz do TextView
        binding.textviewDateVM.text = formattedDate

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        // Wywołaj funkcję, aby znaleźć MatchID na podstawie daty
        getMatchIdByDate(userId, matchDateInMillis) { matchId ->
            if (matchId != null) {
                Log.d("Firebase", "Found matchId: $matchId")
                database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference(userId).child("Matches").child(matchId)
                // Ustaw wynik w tabeli
                setscore(player1, player2, serve1, serve2, set1p1, set2p1, set3p1, set1p2, set2p2, set3p2, pkt1, pkt2)

                binding.textViewSave.setOnClickListener {
                    val noteText = binding.editTextNote.text.toString().trim() // Get the text from the EditText
                    if (noteText.isNotEmpty()) {
                            val databaseReference = database.child("note")
                            databaseReference.setValue(noteText) // Save the note
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Note saved successfully!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(this, "Failed to save note: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                    } else {
                        Toast.makeText(this, "Note cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.d("Firebase", "No match found for the given date")
                Toast.makeText(this, "No match found for the given date", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setscore(player1: TextView, player2: TextView, serve1: TextView, serve2: TextView,
                         set1p1: TextView, set2p1: TextView, set3p1: TextView,
                         set1p2: TextView, set2p2: TextView, set3p2: TextView,
                         pkt1: TextView, pkt2: TextView) {
        lifecycleScope.launch(Dispatchers.IO) {
            val dataSnapshot = database.get().await()

            withContext(Dispatchers.Main) {
                Log.d("FirebaseData", "Snapshot: $dataSnapshot")
                player1.text = dataSnapshot.child("player1").getValue(String::class.java) ?: ""
                player2.text = dataSnapshot.child("player2").getValue(String::class.java) ?: ""
                set1p1.text = dataSnapshot.child("set1p1").getValue(String::class.java) ?: ""
                set2p1.text = dataSnapshot.child("set2p1").getValue(String::class.java) ?: ""
                set3p1.text = dataSnapshot.child("set3p1").getValue(String::class.java) ?: ""
                set1p2.text = dataSnapshot.child("set1p2").getValue(String::class.java) ?: ""
                set2p2.text = dataSnapshot.child("set2p2").getValue(String::class.java) ?: ""
                set3p2.text = dataSnapshot.child("set3p2").getValue(String::class.java) ?: ""
                pkt1.text = dataSnapshot.child("pkt1").getValue(String::class.java) ?: ""
                pkt2.text = dataSnapshot.child("pkt2").getValue(String::class.java) ?: ""

                Log.d("FirebaseData", "Player 1: ${player1.text}, Player 2: ${player2.text}")

                // Ustawienie grafiki lauru lub serwisu
                if(dataSnapshot.child("winner").exists()) {
                    val winner = dataSnapshot.child("winner").getValue(String::class.java)
                    val goldenLaurel =
                        getGoldenDrawable(applicationContext, R.drawable.icon_laurel3)
                    if (winner == player1.text.toString()) {
                        serve1.visibility = View.VISIBLE
                        serve2.visibility = View.INVISIBLE
                        serve1.setCompoundDrawablesWithIntrinsicBounds(
                            goldenLaurel,
                            null,
                            null,
                            null
                        )
                    } else {
                        serve1.visibility = View.INVISIBLE
                        serve2.visibility = View.VISIBLE
                        serve2.setCompoundDrawablesWithIntrinsicBounds(
                            goldenLaurel,
                            null,
                            null,
                            null
                        )
                    }
                }
                else{
                    // Pobranie wartości "LastServePlayer" z bazy danych
                    val lastserve = dataSnapshot.child("LastServePlayer").getValue(String::class.java)
                    serve1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ball, 0, 0, 0)
                    serve2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ball, 0, 0, 0)
                    // Ustawienie wartości w TextView
                    if(lastserve==player1.text.toString()){
                        serve1.visibility = View.VISIBLE
                        serve2.visibility = View.INVISIBLE
                    }
                    else{
                        serve1.visibility = View.INVISIBLE
                        serve2.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun getMatchIdByDate(userId: String, targetDateMillis: Long, onResult: (String?) -> Unit) {
        val database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(userId).child("Matches")

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (matchSnapshot in snapshot.children) {
                        val dataValue = matchSnapshot.child("data").getValue(Long::class.java)
                        if (dataValue == targetDateMillis) {
                            onResult(matchSnapshot.key) // Zwróć ID meczu
                            return
                        }
                    }
                    onResult(null) // Nie znaleziono meczu
                } else {
                    onResult(null) // Brak wpisów w bazie danych
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error reading data: ${error.message}")
                onResult(null)
            }
        })
    }
}