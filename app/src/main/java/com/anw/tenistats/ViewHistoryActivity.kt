package com.anw.tenistats

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewHistoryActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var matchId = "" // Zmienna matchId zamiast stałej

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_history)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Odbierz datę meczu w formacie milisekund z poprzedniej aktywności
        val matchDateInMillis = intent.getLongExtra("matchDateInMillis", 0L)

        // Pobierz mecz na podstawie daty z bazy danych
        fetchMatchByDate(matchDateInMillis)

        findViewById<TextView>(R.id.test).setOnClickListener {
            val intent= Intent(this,ViewStatsActivity::class.java).also{
                it.putExtra("matchID",matchId)
                startActivity(it)
            }
        }


    }

    private fun fetchMatchByDate(matchDateInMillis: Long) {
        val user = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString()).child("Matches")

        // Query w bazie danych Firebase do znalezienia meczu o podanej dacie
        val query = database.orderByChild("data").equalTo(matchDateInMillis.toDouble())

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Pobranie pierwszego pasującego meczu
                    val matchSnapshot = dataSnapshot.children.first()

                    // Pobranie ID meczu
                    matchId = matchSnapshot.key ?: ""

                    // Sprawdzenie czy istnieje trzeci set
                    val thirdSetExists = matchSnapshot.child("set 3").exists()

                    // Pobierz punkty dla tego meczu
                    fetchMatchPoints(matchId) // Wywołaj fetchMatchPoints() po przypisaniu wartości do matchId

                    val textViewSet1 = findViewById<TextView>(R.id.setButton1)
                    val textViewSet2 = findViewById<TextView>(R.id.setButton2)
                    val textViewSet3 = findViewById<TextView>(R.id.setButton3)
                    val textViewAll = findViewById<TextView>(R.id.allButton)

                    textViewAll.setOnClickListener {
                        textViewSet1.setBackgroundResource(0)
                        textViewSet2.setBackgroundResource(0) // resetowanie tła
                        textViewSet3.setBackgroundResource(0)
                        fetchMatchPoints(matchId)

                    }
                    textViewSet1.setOnClickListener {
                        textViewSet1.setBackgroundResource(R.drawable.background_selected_set)
                        textViewSet2.setBackgroundResource(0) // resetowanie tła
                        textViewSet3.setBackgroundResource(0)
                        // Wywołanie funkcji fetchMatchPoints tylko dla seta 1
                        fetchMatchPoints(matchId, "set 1")
                    }

                    textViewSet2.setOnClickListener {
                        textViewSet1.setBackgroundResource(0) // resetowanie tła
                        textViewSet2.setBackgroundResource(R.drawable.background_selected_set)
                        textViewSet3.setBackgroundResource(0)
                        // Wywołanie funkcji fetchMatchPoints tylko dla seta 2
                        fetchMatchPoints(matchId, "set 2")
                    }
                    // Ukryj lub pokaż przycisk dla trzeciego seta w zależności od jego istnienia
                    if (thirdSetExists) {
                        textViewSet3.visibility = View.VISIBLE
                        textViewSet3.setOnClickListener {
                            //textViewSet1.setBackgroundResource(0) // resetowanie tła
                            //textViewSet2.setBackgroundResource(0) // resetowanie tła
                            textViewSet3.setBackgroundResource(R.drawable.background_selected_set)
                            // Wywołanie funkcji fetchMatchPoints tylko dla seta 3
                            fetchMatchPoints(matchId, "set 3")
                        }
                    } else {
                        //textViewSet3.visibility = View.GONE
                        textViewSet3.isClickable=false
                    }
                } else {
                    // Obsługa, gdy dane nie istnieją w bazie danych
                    Toast.makeText(this@ViewHistoryActivity, "Nie znaleziono danych", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Obsługa błędu zapytania do bazy danych
                Toast.makeText(this@ViewHistoryActivity, "Błąd zapytania do bazy danych", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun fetchMatchPoints(matchId: String) {
        val user = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString()).child("Matches").child(matchId)

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val pointsList = mutableListOf<String>()
                var currentSet = 0

                for (setSnapshot in dataSnapshot.children) {


                    if (setSnapshot.hasChildren()) {
                        currentSet++
                        val SetNr= "Set $currentSet"
                        pointsList.add(SetNr)
                        var currentGame = 0
                        for (gameSnapshot in setSnapshot.children) {
                            currentGame++
                            val GameNr= "Game $currentGame"
                            pointsList.add(GameNr)
                            if (gameSnapshot.hasChildren()) {
                                for (pointSnapshot in gameSnapshot.children) {
                                    // Pobranie atrybutów punktu
                                    val co = pointSnapshot.child("co").getValue(String::class.java)
                                    val czym = pointSnapshot.child("czym").getValue(String::class.java)
                                    val gdzie = pointSnapshot.child("gdzie").getValue(String::class.java)
                                    val player = pointSnapshot.child("kto").getValue(String::class.java)
                                    val score1 = pointSnapshot.child("pkt1").getValue(String::class.java)
                                    val score2 = pointSnapshot.child("pkt2").getValue(String::class.java)

                                    // Zbudowanie łańcucha znaków na podstawie atrybutów punktu
                                    val pointString = "$score1:$score2 $player played $co $czym $gdzie"
                                    pointsList.add(pointString)
                                }
                            }
                        }
                    }
                }

                // Po pobraniu wszystkich punktów, wyświetl je na ekranie
                displayMatchPoints(pointsList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Obsługa błędu pobierania danych
                Log.e(TAG, "Error fetching match points: ${databaseError.message}")
            }
        })
    }

    private fun fetchMatchPoints(matchId: String, set: String) {
        val user = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString()).child("Matches").child(matchId).child(set)

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val pointsList = mutableListOf<String>()
                var currentGame = 0

                for (gameSnapshot in dataSnapshot.children) {
                    currentGame++
                    val gameNr = "Game $currentGame"
                    pointsList.add(gameNr)

                    if (gameSnapshot.hasChildren()) {
                        for (pointSnapshot in gameSnapshot.children) {
                            // Pobranie atrybutów punktu
                            val co = pointSnapshot.child("co").getValue(String::class.java)
                            val czym = pointSnapshot.child("czym").getValue(String::class.java)
                            val gdzie = pointSnapshot.child("gdzie").getValue(String::class.java)
                            val player = pointSnapshot.child("kto").getValue(String::class.java)
                            val score1 = pointSnapshot.child("pkt1").getValue(String::class.java)
                            val score2 = pointSnapshot.child("pkt2").getValue(String::class.java)

                            // Zbudowanie łańcucha znaków na podstawie atrybutów punktu
                            val pointString = "$score1:$score2 $player played $co $czym $gdzie"
                            pointsList.add(pointString)
                        }
                    }
                }

                // Po pobraniu wszystkich punktów, wyświetl je na ekranie
                displayMatchPoints(pointsList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Obsługa błędu pobierania danych
                Log.e(TAG, "Error fetching match points: ${databaseError.message}")
            }
        })
    }

    private fun displayMatchPoints(pointsList: List<String>) {
        // Znajdź ListView w układzie i ustaw adapter na nim
        val listView = findViewById<ListView>(R.id.historyList)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, pointsList)
        listView.adapter = adapter
    }
}
