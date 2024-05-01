package com.anw.tenistats

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.anw.tenistats.ui.theme.NavigationDrawerHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewHistoryActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
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
        val backButton = findViewById<ImageButton>(R.id.buttonReturnUndo)
        backButton.visibility = View.GONE

        val userEmail = firebaseAuth.currentUser?.email.toString()
        val userEmailView = headerView.findViewById<TextView>(R.id.textViewUserEmail)
        if(userEmail.isNotEmpty()) {
            userEmailView.text = userEmail
        }else {
            userEmailView.text = resources.getString(R.string.user_email)
        }
        //MENU

        // Odbierz datę meczu w formacie milisekund z poprzedniej aktywności
        val matchDateInMillis = intent.getLongExtra("matchDateInMillis", 0L)

        // Pobierz mecz na podstawie daty z bazy danych
        fetchMatchByDate(matchDateInMillis)

        findViewById<Button>(R.id.statistics).setOnClickListener {
            Intent(this,ViewStatsActivity::class.java).also{
                it.putExtra("matchID",matchId)
                it.putExtra("matchDateInMillis",matchDateInMillis)
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
                    /*val resumeOrStatsDialog = ResumeOrStatsDialogActivity(this@ViewHistoryActivity,true)
                    resumeOrStatsDialog.show(
                        matchId
                    )*/

                    // Sprawdzenie czy istnieje trzeci set
                    val thirdSetExists = matchSnapshot.child("set 3").exists()

                    // Pobierz punkty dla tego meczu
                    fetchMatchPoints(matchId) // Wywołaj fetchMatchPoints() po przypisaniu wartości do matchId

                    val buttonSet1 = findViewById<Button>(R.id.buttonSet1His)
                    val buttonSet2 = findViewById<Button>(R.id.buttonSet2His)
                    val buttonSet3 = findViewById<Button>(R.id.buttonSet3His)
                    val buttonAll = findViewById<Button>(R.id.buttonAllHis)

                    buttonAll.setOnClickListener {
                        buttonAll.setBackgroundResource(R.drawable.rectangle_button)
                        buttonSet1.setBackgroundResource(R.drawable.rec_btn_not_selected)
                        buttonSet2.setBackgroundResource(R.drawable.rec_btn_not_selected)// resetowanie tła
                        buttonSet3.setBackgroundResource(R.drawable.rec_btn_not_selected)
                        fetchMatchPoints(matchId)

                    }
                    buttonSet1.setOnClickListener {
                        buttonAll.setBackgroundResource(R.drawable.rec_btn_not_selected)
                        buttonSet1.setBackgroundResource(R.drawable.rectangle_button)
                        buttonSet2.setBackgroundResource(R.drawable.rec_btn_not_selected) // resetowanie tła
                        buttonSet3.setBackgroundResource(R.drawable.rec_btn_not_selected)
                        // Wywołanie funkcji fetchMatchPoints tylko dla seta 1
                        fetchMatchPoints(matchId, "set 1")
                    }

                    buttonSet2.setOnClickListener {
                        buttonAll.setBackgroundResource(R.drawable.rec_btn_not_selected)
                        buttonSet1.setBackgroundResource(R.drawable.rec_btn_not_selected)
                        buttonSet2.setBackgroundResource(R.drawable.rectangle_button) // resetowanie tła
                        buttonSet3.setBackgroundResource(R.drawable.rec_btn_not_selected)
                        // Wywołanie funkcji fetchMatchPoints tylko dla seta 2
                        fetchMatchPoints(matchId, "set 2")
                    }
                    // Ukryj lub pokaż przycisk dla trzeciego seta w zależności od jego istnienia
                    if (thirdSetExists) {
                        buttonSet3.visibility = View.VISIBLE
                        buttonSet3.setOnClickListener {
                            buttonAll.setBackgroundResource(R.drawable.rec_btn_not_selected)
                            buttonSet1.setBackgroundResource(R.drawable.rec_btn_not_selected)
                            buttonSet2.setBackgroundResource(R.drawable.rec_btn_not_selected) // resetowanie tła
                            buttonSet3.setBackgroundResource(R.drawable.rectangle_button)
                            // Wywołanie funkcji fetchMatchPoints tylko dla seta 3
                            fetchMatchPoints(matchId, "set 3")
                        }
                    } else {
                        //textViewSet3.visibility = View.GONE
                        buttonSet3.isClickable=false
                    }
                    val player1 = findViewById<TextView>(R.id.textviewPlayer1His)
                    val player2 = findViewById<TextView>(R.id.textviewPlayer2His)
                    val serve1 = findViewById<TextView>(R.id.textViewServe1His)
                    val serve2 = findViewById<TextView>(R.id.textViewServe2His)
                    val set1p1 = findViewById<TextView>(R.id.textViewP1Set1His)
                    val set2p1 = findViewById<TextView>(R.id.textViewP1Set2His)
                    val set3p1 = findViewById<TextView>(R.id.textViewP1Set3His)
                    val set1p2 = findViewById<TextView>(R.id.textViewP2Set1His)
                    val set2p2 = findViewById<TextView>(R.id.textViewP2Set2His)
                    val set3p2 = findViewById<TextView>(R.id.textViewP2Set3His)
                    val pkt1 = findViewById<TextView>(R.id.textViewPlayer1PktHis)
                    val pkt2 = findViewById<TextView>(R.id.textViewPlayer2PktHis)
                    setscore(player1,player2,serve1,serve2,set1p1,set2p1,set3p1,set1p2,set2p2,set3p2,pkt1,pkt2)
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
                        val setNr= "Set $currentSet"
                        pointsList.add(setNr)
                        var currentGame = 0
                        for (gameSnapshot in setSnapshot.children) {
                            currentGame++
                            val gameNr= "Game $currentGame"
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

    fun setscore(player1: TextView,player2: TextView,serve1: TextView,serve2: TextView,set1p1: TextView,set2p1: TextView,set3p1: TextView,set1p2: TextView,set2p2: TextView,set3p2: TextView,pkt1: TextView,pkt2: TextView)
    {
        val user = FirebaseAuth.getInstance().currentUser?.uid
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString()).child("Matches").child(matchId)

        database.child("player1").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val player1Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            player1.text = player1Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }

        database.child("player2").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val player2Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            player2.text = player2Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }

        database.child("set1p1").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val set1p1Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            set1p1.text = set1p1Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("set2p1").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val set2p1Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            set2p1.text = set2p1Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("set3p1").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val set3p1Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            set3p1.text = set3p1Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("set1p2").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val set1p2Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            set1p2.text = set1p2Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("set2p2").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val set2p2Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            set2p2.text = set2p2Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("set3p2").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val set3p2Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            set3p2.text = set3p2Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("pkt1").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val pkt1Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            pkt1.text = pkt1Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("pkt2").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val pkt2Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            pkt2.text = pkt2Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("winner").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            if(dataSnapshot.exists()){
                // Pobranie wartości "player1" z bazy danych
                val winner = dataSnapshot.getValue(String::class.java)
                serve1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_resume, 0, 0, 0)
                serve2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_resume, 0, 0, 0)
                // Ustawienie wartości w TextView
                if(winner==player1.text){
                    serve1.visibility = View.VISIBLE
                    serve2.visibility = View.INVISIBLE
                }
                else{
                    serve1.visibility = View.INVISIBLE
                    serve2.visibility = View.VISIBLE
                }
            }
            else{
                database.child("LastServePlayer").get().addOnSuccessListener { dataSnapshot ->
                    // Pobranie wartości "player1" z bazy danych
                    val lastserve = dataSnapshot.getValue(String::class.java)
                    serve1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ball, 0, 0, 0)
                    serve2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ball, 0, 0, 0)
                    // Ustawienie wartości w TextView
                    if(lastserve==player1.text){
                        serve1.visibility = View.VISIBLE
                        serve2.visibility = View.INVISIBLE
                    }
                    else{
                        serve1.visibility = View.INVISIBLE
                        serve2.visibility = View.VISIBLE
                    }
                }.addOnFailureListener { exception ->
                    // Obsługa błędów
                }
            }
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
    }
}
