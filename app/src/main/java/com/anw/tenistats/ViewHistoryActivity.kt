package com.anw.tenistats

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    private var matchId = ""
    private lateinit var pl1: String

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
                        if (set2p1.text != "") {
                            buttonAll.setBackgroundResource(R.drawable.rec_btn_not_selected)
                            buttonSet1.setBackgroundResource(R.drawable.rec_btn_not_selected)
                            buttonSet2.setBackgroundResource(R.drawable.rectangle_button) // resetowanie tła
                            buttonSet3.setBackgroundResource(R.drawable.rec_btn_not_selected)
                            // Wywołanie funkcji fetchMatchPoints tylko dla seta 2
                            fetchMatchPoints(matchId, "set 2")
                        }else{
                            Toast.makeText(this@ViewHistoryActivity, "Set2 does not exist", Toast.LENGTH_SHORT).show()
                        }
                    }
                    //zrobilam zamiast ukrywania przyciku spradzenie czy dany set istnieje i zmienilam przycisk danego seta na nieaktywny ~u
                    // Ukryj lub pokaż przycisk dla trzeciego seta w zależności od jego istnienia
                    //if (thirdSetExists) {
                      //  buttonSet3.visibility = View.VISIBLE
                        buttonSet3.setOnClickListener {
                            if (set3p1.text != "") {
                                buttonAll.setBackgroundResource(R.drawable.rec_btn_not_selected)
                                buttonSet1.setBackgroundResource(R.drawable.rec_btn_not_selected)
                                buttonSet2.setBackgroundResource(R.drawable.rec_btn_not_selected) // resetowanie tła
                                buttonSet3.setBackgroundResource(R.drawable.rectangle_button)
                                // Wywołanie funkcji fetchMatchPoints tylko dla seta 3
                                fetchMatchPoints(matchId, "set 3")
                            }else{
                                Toast.makeText(this@ViewHistoryActivity, "Set3 does not exist", Toast.LENGTH_SHORT).show()
                            }
                        }
                    //} else {
                        //textViewSet3.visibility = View.GONE
                      //  buttonSet3.isClickable=false
                    //}

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
        val playerReference = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString()).child("Matches").child(matchId)

        var player1: String? = null
        var player2: String? = null

        playerReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(player1Snapshot: DataSnapshot) {
                player1 = player1Snapshot.child("player1").getValue(String::class.java)
                player2 = player1Snapshot.child("player2").getValue(String::class.java)

                database.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val pointsList = mutableListOf<String>()
                        var currentSet = 0

                        for (setSnapshot in dataSnapshot.children) {
                            if (setSnapshot.hasChildren()) {
                                currentSet++
                                pointsList.add("Set $currentSet")

                                for (gameSnapshot in setSnapshot.children) {
                                    val set1P1 = gameSnapshot.child("score").child("player1set1").getValue(String::class.java)
                                    val set1P2 = gameSnapshot.child("score").child("player2set1").getValue(String::class.java)
                                    val set2P1 = gameSnapshot.child("score").child("player1set2").getValue(String::class.java)
                                    val set2P2 = gameSnapshot.child("score").child("player2set2").getValue(String::class.java)
                                    val set3P1 = gameSnapshot.child("score").child("player1set3").getValue(String::class.java)
                                    val set3P2 = gameSnapshot.child("score").child("player2set3").getValue(String::class.java)
                                    when (currentSet) {
                                        1 -> {
                                            pointsList.add("$set1P1 : $set1P2")
                                        }
                                        2 -> {
                                            pointsList.add("$set1P1 : $set1P2 | $set2P1 : $set2P2")
                                        }
                                        else -> {
                                            pointsList.add("$set1P1 : $set1P2 | $set2P1 : $set2P2 | $set3P1 : $set3P2")
                                        }
                                    }

                                    if (gameSnapshot.hasChildren()) {
                                        for (pointSnapshot in gameSnapshot.children) {
                                            val co = pointSnapshot.child("co").getValue(String::class.java)
                                            val czym = pointSnapshot.child("czym").getValue(String::class.java)
                                            val gdzie = pointSnapshot.child("gdzie").getValue(String::class.java)
                                            val player = pointSnapshot.child("kto").getValue(String::class.java)
                                            val score1 = pointSnapshot.child("pkt1").getValue(String::class.java)
                                            val score2 = pointSnapshot.child("pkt2").getValue(String::class.java)

                                            /*val inName=firstLetters(player)*/
                                            val pointString = if (player == player1) {
                                                "$score1:$score2 ◁ $co $czym $gdzie" // symbol X dla player1
                                            } else {
                                                "$score1:$score2 ▶ $co $czym $gdzie" // symbol O dla player2
                                            }

                                            if (score1 != null && score2 != null && player != null && co != null && czym != null && gdzie != null) {
                                                pointsList.add(pointString)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        displayMatchPoints(pointsList, "◁", "▶")
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e(TAG, "Error fetching match points: ${databaseError.message}")
                    }
                })
            }

            override fun onCancelled(player1DatabaseError: DatabaseError) {
                Log.e(TAG, "Error fetching player1 data: ${player1DatabaseError.message}")
            }
        })
    }

    private fun fetchMatchPoints(matchId: String, set: String) {
        val user = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString()).child("Matches").child(matchId).child(set)
        val Player = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString()).child("Matches").child(matchId)

        var player1: String? = null
        var player2: String? = null

        Player.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(player1Snapshot: DataSnapshot) {
                player1 = player1Snapshot.child("player1").getValue(String::class.java)
                player2 = player1Snapshot.child("player2").getValue(String::class.java)
                database.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val pointsList = mutableListOf<String>()
                        var currentGame = 0

                        for (gameSnapshot in dataSnapshot.children) {
                            val set1P1 = gameSnapshot.child("score").child("player1set1").getValue(String::class.java)
                            val set1P2 = gameSnapshot.child("score").child("player2set1").getValue(String::class.java)
                            val set2P1 = gameSnapshot.child("score").child("player1set2").getValue(String::class.java)
                            val set2P2 = gameSnapshot.child("score").child("player2set2").getValue(String::class.java)
                            val set3P1 = gameSnapshot.child("score").child("player1set3").getValue(String::class.java)
                            val set3P2 = gameSnapshot.child("score").child("player2set3").getValue(String::class.java)
                            if(set2P1=="") {
                                pointsList.add("$set1P1 : $set1P2")
                            }
                            else if(set3P1=="") {
                                pointsList.add("$set1P1 : $set1P2 | $set2P1 : $set2P2")
                            }
                            else{
                                pointsList.add("$set1P1 : $set1P2 | $set2P1 : $set2P2 | $set3P1 : $set3P2")
                            }

                            if (gameSnapshot.hasChildren()) {
                                for (pointSnapshot in gameSnapshot.children) {
                                    val co = pointSnapshot.child("co").getValue(String::class.java)
                                    val czym = pointSnapshot.child("czym").getValue(String::class.java)
                                    val gdzie = pointSnapshot.child("gdzie").getValue(String::class.java)
                                    val player = pointSnapshot.child("kto").getValue(String::class.java)
                                    val score1 = pointSnapshot.child("pkt1").getValue(String::class.java)
                                    val score2 = pointSnapshot.child("pkt2").getValue(String::class.java)

                                    /*val inName=firstLetters(player)*/
                                    val pointString = if (player == player1) {
                                        "$score1:$score2 ◁ $co $czym $gdzie" // symbol X dla player1
                                    } else {
                                        "$score1:$score2 ▶ $co $czym $gdzie" // symbol O dla player2
                                    }


                                    if (score1 != null && score2 != null && player != null && co != null && czym != null && gdzie != null) {
                                        pointsList.add(pointString)
                                    }
                                }
                            }
                        }
                        displayMatchPoints(pointsList, "◁", "▶")
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e(TAG, "Error fetching match points: ${databaseError.message}")
                    }
                })
            }

            override fun onCancelled(player1DatabaseError: DatabaseError) {
                Log.e(TAG, "Error fetching player1 data: ${player1DatabaseError.message}")
            }
        })
    }

    private fun displayMatchPoints(pointsList: List<String>, player1Name: String?, player2Name: String?) {
        val listView = findViewById<ListView>(R.id.historyList)
        val adapter = CustomArrayAdapter(this, pointsList, player1Name, player2Name)
        listView.adapter = adapter
    }

    fun firstLetters(input: String?): String {
        var result = ""

        // Dzielenie ciągu wejściowego na słowa po spacji
        val words = input?.split(" ")

        // Przechodzenie przez każde słowo w ciągu wejściowym
        if (words != null) {
            for (word in words) {
                // Jeśli słowo nie jest puste
                if (word.isNotEmpty()) {
                    // Dodaj pierwszą literę słowa do wyniku
                    result += word[0]

                    // Sprawdź, czy słowo zawiera liczby
                    val numbers = word.filter { it.isDigit() }
                    if (numbers.isNotEmpty()) {
                        result += numbers // Dodaj liczby do wyniku
                    }
                }
            }
        }

        // Zwróć wynik
        return result
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
            pl1 = player1Value.toString()
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
                val goldenLaurel = getGoldenDrawable(applicationContext, R.drawable.icon_laurel3)
                serve1.setCompoundDrawablesWithIntrinsicBounds(goldenLaurel, null, null, null)
                serve2.setCompoundDrawablesWithIntrinsicBounds(goldenLaurel, null, null, null)

                // Ustawienie wartości w TextView
                if(winner==pl1){
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
                    if(lastserve==pl1){
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

