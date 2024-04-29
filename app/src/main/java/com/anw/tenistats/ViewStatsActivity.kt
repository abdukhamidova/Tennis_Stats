package com.anw.tenistats

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class ViewStatsActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_stats)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val app = application as Stats
        //pola w tabeli wyniku
        val player1 = findViewById<TextView>(R.id.textviewPlayer1Stats)
        val player2 = findViewById<TextView>(R.id.textviewPlayer2Stats)
        val serve1 = findViewById<TextView>(R.id.textViewServe1Stats)
        val serve2 = findViewById<TextView>(R.id.textViewServe2Stats)
        val set1p1 = findViewById<TextView>(R.id.textViewP1Set1Stats)
        val set2p1 = findViewById<TextView>(R.id.textViewP1Set2Stats)
        val set3p1 = findViewById<TextView>(R.id.textViewP1Set3Stats)
        val set1p2 = findViewById<TextView>(R.id.textViewP2Set1Stats)
        val set2p2 = findViewById<TextView>(R.id.textViewP2Set2Stats)
        val set3p2 = findViewById<TextView>(R.id.textViewP2Set3Stats)

        firebaseAuth=FirebaseAuth.getInstance()
        val matchID = intent.getStringExtra("matchID").toString()
        val user = firebaseAuth.currentUser?.uid
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString()).child("Matches").child(matchID)

        //ustawienie wyniku w tabeli
        setscore(player1,player2,serve1,serve2,set1p1,set2p1,set3p1,set1p2,set2p2,set3p2)

        //pola nad statystykami
        val player1name = findViewById<TextView>(R.id.player1name)
        val player2name = findViewById<TextView>(R.id.player2name)
        database.child("player1").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val player1Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            player1name.text = player1Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }

        database.child("player2").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val player2Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            player2name.text = player2Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }

        findViewById<Button>(R.id.buttonAllStats).setOnClickListener {
            //zmiana kolorow textViews ALL, SET1,SET2,SET3
            findViewById<Button>(R.id.buttonAllStats).setBackgroundResource(R.drawable.rectangle_button)
            findViewById<Button>(R.id.buttonSet1Stats).setBackgroundResource(R.drawable.rec_btn_not_selected)
            findViewById<Button>(R.id.buttonSet2Stats).setBackgroundResource(R.drawable.rec_btn_not_selected)
            findViewById<Button>(R.id.buttonSet3Stats).setBackgroundResource(R.drawable.rec_btn_not_selected)
            //ustawienie statystyk ALL
            setAppValues(app,0,player1name,player2name)
            setTableValue(app)
        }
        findViewById<Button>(R.id.buttonSet1Stats).setOnClickListener {
            //zmiana kolorow textViews ALL, SET1,SET2,SET3
            findViewById<Button>(R.id.buttonAllStats).setBackgroundResource(R.drawable.rec_btn_not_selected)
            findViewById<Button>(R.id.buttonSet1Stats).setBackgroundResource(R.drawable.rectangle_button)
            findViewById<Button>(R.id.buttonSet2Stats).setBackgroundResource(R.drawable.rec_btn_not_selected)
            findViewById<Button>(R.id.buttonSet3Stats).setBackgroundResource(R.drawable.rec_btn_not_selected)
            //ustawienie statystyk SET1
            setAppValues(app,1,player1name,player2name)
            setTableValue(app)
        }
        findViewById<Button>(R.id.buttonSet2Stats).setOnClickListener {
            if (set2p1.text != ""){
                //zmiana kolorow textViews ALL, SET1,SET2,SET3
                findViewById<Button>(R.id.buttonAllStats).setBackgroundResource(R.drawable.rec_btn_not_selected)
                findViewById<Button>(R.id.buttonSet1Stats).setBackgroundResource(R.drawable.rec_btn_not_selected)
                findViewById<Button>(R.id.buttonSet2Stats).setBackgroundResource(R.drawable.rectangle_button)
                findViewById<Button>(R.id.buttonSet3Stats).setBackgroundResource(R.drawable.rec_btn_not_selected)
                //ustawienie statystyk SET2
                setAppValues(app, 2, player1name, player2name)
                setTableValue(app)
            }
        }
        findViewById<Button>(R.id.buttonSet3Stats).setOnClickListener {
            if(set3p1.text!=""){
                //zmiana kolorow textViews ALL, SET1,SET2,SET3
                findViewById<Button>(R.id.buttonAllStats).setBackgroundResource(R.drawable.rec_btn_not_selected)
                findViewById<Button>(R.id.buttonSet1Stats).setBackgroundResource(R.drawable.rec_btn_not_selected)
                findViewById<Button>(R.id.buttonSet2Stats).setBackgroundResource(R.drawable.rec_btn_not_selected)
                findViewById<Button>(R.id.buttonSet3Stats).setBackgroundResource(R.drawable.rectangle_button)
                //ustawienie statystyk SET3
                setAppValues(app,3,player1name,player2name)
                setTableValue(app)
            }
        }
        //zmiana aktywnosci na History
        findViewById<Button>(R.id.buttonHistoryStats).setOnClickListener {
            val intent= Intent(this,ViewHistoryActivity::class.java).also{
                startActivity(it)
            }
        }
    }
    fun setscore(player1: TextView,player2: TextView,serve1: TextView,serve2: TextView,set1p1: TextView,set2p1: TextView,set3p1: TextView,set1p2: TextView,set2p2: TextView,set3p2: TextView)
    {
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
    }

    private fun countForPlayer(setNumber: Int, playerName: String, shotName: String, placeName: String, sideName: String,callback: (Int) -> Unit) {
        //statystyki dla pojedynczych setow
        if(setNumber!=0) {
            database.child("set $setNumber")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var result = 0
                        for (gameSnapshot in dataSnapshot.children) {
                            if (gameSnapshot.hasChildren()) {
                                for (pointSnapshot in gameSnapshot.children) {
                                    // pobranie atrybutów punktu
                                    val co = pointSnapshot.child("co").getValue(String::class.java)
                                    val czym =
                                        pointSnapshot.child("czym").getValue(String::class.java)
                                    val gdzie =
                                        pointSnapshot.child("gdzie").getValue(String::class.java)
                                    val player =
                                        pointSnapshot.child("kto").getValue(String::class.java)
                                    if (player == playerName && co == shotName && gdzie == placeName && czym == sideName) {
                                        result++
                                    }
                                }
                            }
                        }
                        callback(result)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Obsługa błędu pobierania danych
                        Log.e(
                            ContentValues.TAG,
                            "Error fetching match points: ${databaseError.message}"
                        )
                    }
                })
        }
        //statystyki dla ALL
        else{
            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var result = 0
                    for (setSnapshot in dataSnapshot.children) {
                        if (setSnapshot.hasChildren()) {
                            for (gameSnapshot in setSnapshot.children) {
                                if (gameSnapshot.hasChildren()) {
                                    for (pointSnapshot in gameSnapshot.children) {
                                        //pobranie atrybutów punktu
                                        val co =
                                            pointSnapshot.child("co").getValue(String::class.java)
                                        val czym =
                                            pointSnapshot.child("czym").getValue(String::class.java)
                                        val gdzie =
                                            pointSnapshot.child("gdzie")
                                                .getValue(String::class.java)
                                        val player =
                                            pointSnapshot.child("kto").getValue(String::class.java)
                                        if (player == playerName && co == shotName && gdzie == placeName && czym == sideName) {
                                            result++
                                        }
                                    }
                                }
                            }
                        }
                    }
                        callback(result)
                }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Obsługa błędu pobierania danych
                        Log.e(
                            ContentValues.TAG,
                            "Error fetching match points: ${databaseError.message}"
                        )
                    }
                })
        }
    }

    private fun countServeForPlayer(setNumber: Int, playerName: String, serveNumber: Int,callback: (Int) -> Unit) {
        //statystyki dla pojedynczych setow
        if(setNumber!=0) {
            database.child("set $setNumber")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var result = 0
                        for (gameSnapshot in dataSnapshot.children) {
                            if (gameSnapshot.hasChildren()) {
                                for (pointSnapshot in gameSnapshot.children) {
                                    // pobranie atrybutów punktu
                                    val servePlayer = pointSnapshot.child("servePlayer").getValue(String::class.java)
                                    val serwis = pointSnapshot.child("serwis").getValue(Int::class.java)
                                    if (servePlayer == playerName && serwis == serveNumber) {
                                        result++
                                    }
                                }
                            }
                        }
                        callback(result)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Obsługa błędu pobierania danych
                        Log.e(
                            ContentValues.TAG,
                            "Error fetching match points: ${databaseError.message}"
                        )
                    }
                })
        }
        //statystyki dla ALL
        else{
            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var result = 0
                    for (setSnapshot in dataSnapshot.children) {
                        if (setSnapshot.hasChildren()) {
                            for (gameSnapshot in setSnapshot.children) {
                                if (gameSnapshot.hasChildren()) {
                                    for (pointSnapshot in gameSnapshot.children) {
                                        // pobranie atrybutów punktu
                                        val servePlayer = pointSnapshot.child("servePlayer").getValue(String::class.java)
                                        val serwis = pointSnapshot.child("serwis").getValue(Int::class.java)
                                        if (servePlayer == playerName && serwis == serveNumber) {
                                            result++
                                        }
                                    }
                                }
                            }
                        }
                    }
                    callback(result)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Obsługa błędu pobierania danych
                    Log.e(
                        ContentValues.TAG,
                        "Error fetching match points: ${databaseError.message}"
                    )
                }
            })
        }
    }

    fun setAppValues(app: Stats,setNumber: Int,player1name: TextView,player2name: TextView){
        countForPlayer(setNumber, player1name.text.toString(), "Ace","","") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.ace1 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Ace","","") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.ace2 = result
            app.totalpoints2+=result
        }

        countForPlayer(setNumber, player1name.text.toString(), "Double Fault","","") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.doublefault1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Double Fault","","") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.doublefault2 = result
            app.totalpoints1+=result
        }

        countServeForPlayer(setNumber,player1name.text.toString(),1){ result ->
            app.firstservein1=result
        }
        countServeForPlayer(setNumber,player2name.text.toString(),1){ result ->
            app.firstservein2=result
        }

        countServeForPlayer(setNumber,player1name.text.toString(),2){ result ->
            app.secondservein1=result
        }
        countServeForPlayer(setNumber,player2name.text.toString(),2){ result ->
            app.secondservein2=result
        }

        app.secondserve1=app.secondservein1+app.doublefault1
        app.secondserve2=app.secondservein2+app.doublefault2

        app.firstserve1=app.firstservein1+app.secondserve1
        app.firstserve2=app.firstservein2+app.secondserve2

        countForPlayer(setNumber, player1name.text.toString(), "Winner","Return","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.returnwinnerFH1 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player1name.text.toString(), "Winner","Return","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.returnwinnerBH1 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Winner","Return","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.returnwinnerFH2 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Winner","Return","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.returnwinnerFH2 = result
            app.totalpoints2+=result
        }

        countForPlayer(setNumber, player1name.text.toString(), "Error","Return","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.returnerrorFH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player1name.text.toString(), "Error","Return","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.returnerrorBH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Error","Return","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.returnerrorFH2 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Error","Return","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.returnerrorBH2 = result
            app.totalpoints1+=result
        }

        countForPlayer(setNumber, player1name.text.toString(), "Winner","Ground","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnergroundFH1 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player1name.text.toString(), "Winner","Ground","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnergroundBH1 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Winner","Ground","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnergroundFH2 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Winner","Ground","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnergroundBH2 = result
            app.totalpoints2+=result
        }

        countForPlayer(setNumber, player1name.text.toString(), "Winner","Slice","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnersliceFH1 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player1name.text.toString(), "Winner","Slice","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnersliceBH1 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Winner","Slice","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnersliceFH2 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Winner","Slice","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnersliceBH2 = result
            app.totalpoints2+=result
        }

        countForPlayer(setNumber, player1name.text.toString(), "Winner","Smash","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnersmashFH1 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player1name.text.toString(), "Winner","Smash","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnersmashBH1 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Winner","Smash","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnersmashFH2 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Winner","Smash","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnersmashBH2 = result
            app.totalpoints2+=result
        }

        countForPlayer(setNumber, player1name.text.toString(), "Winner","Volley","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnervolleyFH1 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player1name.text.toString(), "Winner","Volley","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnervolleyBH1 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Winner","Volley","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnervolleyFH2 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Winner","Volley","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnervolleyBH2 = result
            app.totalpoints2+=result
        }

        countForPlayer(setNumber, player1name.text.toString(), "Winner","Dropshot","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnerdropshotFH1 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player1name.text.toString(), "Winner","Dropshot","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnerdropshotBH1 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Winner","Dropshot","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnerdropshotFH2 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Winner","Dropshot","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnerdropshotBH2 = result
            app.totalpoints2+=result
        }

        countForPlayer(setNumber, player1name.text.toString(), "Winner","Lob","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnerlobFH1 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player1name.text.toString(), "Winner","Lob","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnerlobBH1 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Winner","Lob","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnerlobFH2 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Winner","Lob","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.winnerlobBH2 = result
            app.totalpoints2+=result
        }

        countForPlayer(setNumber, player1name.text.toString(), "Forced Error","Ground","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrorgroundFH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player1name.text.toString(), "Forced Error","Ground","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrorgroundBH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Forced Error","Ground","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrorgroundFH2 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Forced Error","Ground","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrorgroundBH2 = result
            app.totalpoints1+=result
        }

        countForPlayer(setNumber, player1name.text.toString(), "Forced Error","Slice","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrorsliceFH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player1name.text.toString(), "Forced Error","Slice","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrorsliceBH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Forced Error","Slice","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrorsliceFH2 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Forced Error","Slice","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrorsliceBH2 = result
            app.totalpoints1+=result
        }

        countForPlayer(setNumber, player1name.text.toString(), "Forced Error","Smash","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrorsmashFH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player1name.text.toString(), "Forced Error","Smash","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrorsmashBH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Forced Error","Smash","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrorsmashFH2 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Forced Error","Smash","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrorsmashBH2 = result
            app.totalpoints1+=result
        }

        countForPlayer(setNumber, player1name.text.toString(), "Forced Error","Volley","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrorvolleyFH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player1name.text.toString(), "Forced Error","Volley","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrorvolleyBH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Forced Error","Volley","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrorvolleyFH2 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Forced Error","Volley","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrorvolleyBH2 = result
            app.totalpoints1+=result
        }

        countForPlayer(setNumber, player1name.text.toString(), "Forced Error","Dropshot","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrordropshotFH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player1name.text.toString(), "Forced Error","Dropshot","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrordropshotBH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Forced Error","Dropshot","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrordropshotFH2 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Forced Error","Dropshot","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrordropshotBH2 = result
            app.totalpoints1+=result
        }

        countForPlayer(setNumber, player1name.text.toString(), "Forced Error","Lob","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrorlobFH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player1name.text.toString(), "Forced Error","Lob","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrorlobBH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Forced Error","Lob","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrorlobFH2 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Forced Error","Lob","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.forcederrorlobBH2 = result
            app.totalpoints1+=result
        }

        countForPlayer(setNumber, player1name.text.toString(), "Unforced Error","Ground","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrorgroundFH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player1name.text.toString(), "Unforced Error","Ground","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrorgroundBH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Unforced Error","Ground","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrorgroundFH2 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Unforced Error","Ground","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrorgroundBH2 = result
            app.totalpoints1+=result
        }

        countForPlayer(setNumber, player1name.text.toString(), "Unforced Error","Slice","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrorsliceFH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player1name.text.toString(), "Unforced Error","Slice","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrorsliceBH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Unforced Error","Slice","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrorsliceFH2 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Unforced Error","Slice","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrorsliceBH2 = result
            app.totalpoints1+=result
        }

        countForPlayer(setNumber, player1name.text.toString(), "Unforced Error","Smash","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrorsmashFH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player1name.text.toString(), "Unforced Error","Smash","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrorsmashBH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Unforced Error","Smash","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrorsmashFH2 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Unforced Error","Smash","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrorsmashBH2 = result
            app.totalpoints1+=result
        }

        countForPlayer(setNumber, player1name.text.toString(), "Unforced Error","Volley","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrorvolleyFH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player1name.text.toString(), "Unforced Error","Volley","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrorvolleyBH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Unforced Error","Volley","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrorvolleyFH2 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Unforced Error","Volley","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrorvolleyBH2 = result
            app.totalpoints1+=result
        }

        countForPlayer(setNumber, player1name.text.toString(), "Unforced Error","Dropshot","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrordropshotFH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player1name.text.toString(), "Unforced Error","Dropshot","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrordropshotBH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Unforced Error","Dropshot","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrordropshotFH2 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Unforced Error","Dropshot","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrordropshotBH2 = result
            app.totalpoints1+=result
        }

        countForPlayer(setNumber, player1name.text.toString(), "Unforced Error","Lob","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrorlobFH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player1name.text.toString(), "Unforced Error","Lob","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrorlobBH1 = result
            app.totalpoints2+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Unforced Error","Lob","Forehand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrorlobFH2 = result
            app.totalpoints1+=result
        }
        countForPlayer(setNumber, player2name.text.toString(), "Unforced Error","Lob","Backhand") { result ->
            // Tutaj możesz obsłużyć wynik countAcesForPlayer
            app.unforcederrorlobBH2 = result
            app.totalpoints1+=result
        }
    }

    fun setTableValue(app: Stats){
        val scale = resources.displayMetrics.density
        findViewById<TextView>(R.id.totalPlayer1).text = app.totalpoints1.toString()
        findViewById<TextView>(R.id.totalPlayer2).text = app.totalpoints2.toString()
        //kod do ustawienia wykresow totalpoints
        if(app.totalpoints1>app.totalpoints2){
            findViewById<View>(R.id.graphTotalPlayer1).layoutParams.width=(100 * scale + 0.5f).toInt()
            val wyn = (app.totalpoints2.toDouble() * 100 /app.totalpoints1.toDouble()).toInt()
            findViewById<View>(R.id.graphTotalPlayer2).layoutParams.width=(wyn * scale + 0.5f).toInt()
            /*findViewById<View>(R.id.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
        }
        else{
            findViewById<View>(R.id.graphTotalPlayer2).layoutParams.width=(100 * scale + 0.5f).toInt()
            val wyn = (app.totalpoints1.toDouble() * 100 / app.totalpoints2.toDouble()).toInt()
            findViewById<View>(R.id.graphTotalPlayer1).layoutParams.width=(wyn * scale + 0.5f).toInt()
            /*findViewById<View>(R.id.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
        }

        app.totalpoints1 = 0
        app.totalpoints2 = 0

        findViewById<TextView>(R.id.acePlayer1).text = app.ace1.toString()
        findViewById<TextView>(R.id.acePlayer2).text = app.ace2.toString()
        if(app.ace1==app.ace2 && app.ace1==0){
            findViewById<View>(R.id.graphAcePlayer1).layoutParams.width=0
            findViewById<View>(R.id.graphAcePlayer2).layoutParams.width=0
        }
        else if(app.ace1>app.ace2){
            findViewById<View>(R.id.graphAcePlayer1).layoutParams.width=(100 * scale + 0.5f).toInt()
            val wyn = (app.ace2.toDouble() * 100 /app.ace1.toDouble()).toInt()
            findViewById<View>(R.id.graphAcePlayer2).layoutParams.width=(wyn * scale + 0.5f).toInt()
            /*findViewById<View>(R.id.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
        }
        else{
            findViewById<View>(R.id.graphAcePlayer2).layoutParams.width=(100 * scale + 0.5f).toInt()
            val wyn = (app.ace1.toDouble() * 100 / app.ace2.toDouble()).toInt()
            findViewById<View>(R.id.graphAcePlayer1).layoutParams.width=(wyn * scale + 0.5f).toInt()
            /*findViewById<View>(R.id.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
        }

        findViewById<TextView>(R.id.doublePlayer1).text = app.doublefault1.toString()
        findViewById<TextView>(R.id.doublePlayer2).text = app.doublefault2.toString()
        if(app.doublefault1==app.doublefault2 && app.doublefault1==0){
            findViewById<View>(R.id.graphDoubleFaultPlayer1).layoutParams.width=0
            findViewById<View>(R.id.graphDoubleFaultPlayer2).layoutParams.width=0
        }
        else if(app.doublefault1>app.doublefault2){
            findViewById<View>(R.id.graphDoubleFaultPlayer1).layoutParams.width=(100 * scale + 0.5f).toInt()
            val wyn = (app.doublefault2.toDouble() * 100 / app.doublefault1.toDouble()).toInt()
            findViewById<View>(R.id.graphDoubleFaultPlayer2).layoutParams.width=(wyn * scale + 0.5f).toInt()
            /*findViewById<View>(R.id.graphDoubleFaultPlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
        }
        else{
            findViewById<View>(R.id.graphDoubleFaultPlayer2).layoutParams.width=(100 * scale + 0.5f).toInt()
            val wyn = (app.doublefault1.toDouble() * 100 / app.doublefault2.toDouble()).toInt()
            findViewById<View>(R.id.graphDoubleFaultPlayer1).layoutParams.width=(wyn * scale + 0.5f).toInt()
            /*findViewById<View>(R.id.graphDoubleFaultPlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
        }

        val firstServeIn1 = app.firstservein1
        val allFirstServe1 = app.firstserve1
        val firstServePercentage1 = (firstServeIn1.toDouble() / allFirstServe1.toDouble() * 100).toInt()
        findViewById<TextView>(R.id.firstinPlayer1).text = "$firstServeIn1/$allFirstServe1 ($firstServePercentage1%)"

        val firstServeIn2 = app.firstservein2
        val allFirstServe2 = app.firstserve2
        val firstServePercentage2 = (firstServeIn2.toDouble() / allFirstServe2.toDouble() * 100).toInt()
        findViewById<TextView>(R.id.firstinPlayer2).text = "$firstServeIn2/$allFirstServe2 ($firstServePercentage2%)"

        findViewById<View>(R.id.graphFirstPlayer1).layoutParams.width=(firstServePercentage1 * scale + 0.5f).toInt()
        findViewById<View>(R.id.graphFirstPlayer2).layoutParams.width=(firstServePercentage2 * scale + 0.5f).toInt()

        val secondServeIn1 = app.secondservein1
        val allSecondServe1 = app.secondserve1
        val secondServePercentage1 = (secondServeIn1.toDouble() / allSecondServe1.toDouble() * 100).toInt()
        findViewById<TextView>(R.id.secondinPlayer1).text = "$secondServeIn1/$allSecondServe1 ($secondServePercentage1%)"

        val secondServeIn2 = app.secondservein2
        val allSecondServe2 = app.secondserve2
        val secondServePercentage2 = (secondServeIn2.toDouble() / allSecondServe2.toDouble() * 100).toInt()
        findViewById<TextView>(R.id.secondinPlayer2).text = "$secondServeIn2/$allSecondServe2 ($secondServePercentage2%)"

        findViewById<View>(R.id.graphSecondPlayer1).layoutParams.width=(secondServePercentage1 * scale + 0.5f).toInt()
        findViewById<View>(R.id.graphSecondPlayer2).layoutParams.width=(secondServePercentage2 * scale + 0.5f).toInt()

        findViewById<TextView>(R.id.returnWfhPlayer1).text = app.returnwinnerFH1.toString()
        findViewById<TextView>(R.id.returnWfhPlayer2).text = app.returnwinnerFH2.toString()
        findViewById<TextView>(R.id.returnWbhPlayer1).text = app.returnwinnerBH1.toString()
        findViewById<TextView>(R.id.returnWbhPlayer2).text = app.returnwinnerBH2.toString()

        findViewById<TextView>(R.id.returnEfhPlayer1).text = app.returnerrorFH1.toString()
        findViewById<TextView>(R.id.returnEfhPlayer2).text = app.returnerrorFH2.toString()
        findViewById<TextView>(R.id.returnEbhPlayer1).text = app.returnerrorBH1.toString()
        findViewById<TextView>(R.id.returnEbhPlayer2).text = app.returnerrorBH2.toString()

        findViewById<TextView>(R.id.winnerGroundfhPlayer1).text = app.winnergroundFH1.toString()
        findViewById<TextView>(R.id.winnerGroundfhPlayer2).text = app.winnergroundFH2.toString()
        findViewById<TextView>(R.id.winnerGroundbhPlayer1).text = app.winnergroundBH1.toString()
        findViewById<TextView>(R.id.winnerGroundbhPlayer2).text = app.winnergroundBH2.toString()

        findViewById<TextView>(R.id.winnerSlicefhPlayer1).text = app.winnersliceFH1.toString()
        findViewById<TextView>(R.id.winnerSlicefhPlayer2).text = app.winnersliceFH2.toString()
        findViewById<TextView>(R.id.winnerSlicebhPlayer1).text = app.winnersliceBH1.toString()
        findViewById<TextView>(R.id.winnerSlicebhPlayer2).text = app.winnersliceBH2.toString()

        findViewById<TextView>(R.id.winnerSmashfhPlayer1).text = app.winnersmashFH1.toString()
        findViewById<TextView>(R.id.winnerSmashfhPlayer2).text = app.winnersmashFH2.toString()
        findViewById<TextView>(R.id.winnerSmashbhPlayer1).text = app.winnersmashBH1.toString()
        findViewById<TextView>(R.id.winnerSmashbhPlayer2).text = app.winnersmashBH2.toString()

        findViewById<TextView>(R.id.winnerVolleyfhPlayer1).text = app.winnervolleyFH1.toString()
        findViewById<TextView>(R.id.winnerVolleyfhPlayer2).text = app.winnervolleyFH2.toString()
        findViewById<TextView>(R.id.winnerVolleybhPlayer1).text = app.winnervolleyBH1.toString()
        findViewById<TextView>(R.id.winnerVolleybhPlayer2).text = app.winnervolleyBH2.toString()

        findViewById<TextView>(R.id.winnerDropshotfhPlayer1).text = app.winnerdropshotFH1.toString()
        findViewById<TextView>(R.id.winnerDropshotfhPlayer2).text = app.winnerdropshotFH2.toString()
        findViewById<TextView>(R.id.winnerDropshotbhPlayer1).text = app.winnerdropshotBH1.toString()
        findViewById<TextView>(R.id.winnerDropshotbhPlayer2).text = app.winnerdropshotBH2.toString()

        findViewById<TextView>(R.id.winnerLobfhPlayer1).text = app.winnerlobFH1.toString()
        findViewById<TextView>(R.id.winnerLobfhPlayer2).text = app.winnerlobFH2.toString()
        findViewById<TextView>(R.id.winnerLobbhPlayer1).text = app.winnerlobBH1.toString()
        findViewById<TextView>(R.id.winnerLobbhPlayer2).text = app.winnerlobBH2.toString()

        findViewById<TextView>(R.id.forcedErrorGroundfhPlayer1).text = app.forcederrorgroundFH1.toString()
        findViewById<TextView>(R.id.forcedErrorGroundfhPlayer2).text = app.forcederrorgroundFH2.toString()
        findViewById<TextView>(R.id.forcedErrorGroundbhPlayer1).text = app.forcederrorgroundBH1.toString()
        findViewById<TextView>(R.id.forcedErrorGroundbhPlayer2).text = app.forcederrorgroundBH2.toString()

        findViewById<TextView>(R.id.forcedErrorSlicefhPlayer1).text = app.forcederrorsliceFH1.toString()
        findViewById<TextView>(R.id.forcedErrorSlicefhPlayer2).text = app.forcederrorsliceFH2.toString()
        findViewById<TextView>(R.id.forcedErrorSlicebhPlayer1).text = app.forcederrorsliceBH1.toString()
        findViewById<TextView>(R.id.forcedErrorSlicebhPlayer2).text = app.forcederrorsliceBH2.toString()

        findViewById<TextView>(R.id.forcedErrorSmashfhPlayer1).text = app.forcederrorsmashFH1.toString()
        findViewById<TextView>(R.id.forcedErrorSmashfhPlayer2).text = app.forcederrorsmashFH2.toString()
        findViewById<TextView>(R.id.forcedErrorSmashbhPlayer1).text = app.forcederrorsmashBH1.toString()
        findViewById<TextView>(R.id.forcedErrorSmashbhPlayer2).text = app.forcederrorsmashBH2.toString()

        findViewById<TextView>(R.id.forcedErrorVolleyfhPlayer1).text = app.forcederrorvolleyFH1.toString()
        findViewById<TextView>(R.id.forcedErrorVolleyfhPlayer2).text = app.forcederrorvolleyFH2.toString()
        findViewById<TextView>(R.id.forcedErrorVolleybhPlayer1).text = app.forcederrorvolleyBH1.toString()
        findViewById<TextView>(R.id.forcedErrorVolleybhPlayer2).text = app.forcederrorvolleyBH2.toString()

        findViewById<TextView>(R.id.forcedErrorDropshotfhPlayer1).text = app.forcederrordropshotFH1.toString()
        findViewById<TextView>(R.id.forcedErrorDropshotfhPlayer2).text = app.forcederrordropshotFH2.toString()
        findViewById<TextView>(R.id.forcedErrorDropshotbhPlayer1).text = app.forcederrordropshotBH1.toString()
        findViewById<TextView>(R.id.forcedErrorDropshotbhPlayer2).text = app.forcederrordropshotBH2.toString()

        findViewById<TextView>(R.id.forcedErrorLobfhPlayer1).text = app.forcederrorlobFH1.toString()
        findViewById<TextView>(R.id.forcedErrorLobfhPlayer2).text = app.forcederrorlobFH2.toString()
        findViewById<TextView>(R.id.forcedErrorLobbhPlayer1).text = app.forcederrorlobBH1.toString()
        findViewById<TextView>(R.id.forcedErrorLobbhPlayer2).text = app.forcederrorlobBH2.toString()

        findViewById<TextView>(R.id.unforcedErrorGroundfhPlayer1).text = app.unforcederrorgroundFH1.toString()
        findViewById<TextView>(R.id.unforcedErrorGroundfhPlayer2).text = app.unforcederrorgroundFH2.toString()
        findViewById<TextView>(R.id.unforcedErrorGroundbhPlayer1).text = app.unforcederrorgroundBH1.toString()
        findViewById<TextView>(R.id.unforcedErrorGroundbhPlayer2).text = app.unforcederrorgroundBH2.toString()

        findViewById<TextView>(R.id.unforcedErrorSlicefhPlayer1).text = app.unforcederrorsliceFH1.toString()
        findViewById<TextView>(R.id.unforcedErrorSlicefhPlayer2).text = app.unforcederrorsliceFH2.toString()
        findViewById<TextView>(R.id.unforcedErrorSlicebhPlayer1).text = app.unforcederrorsliceBH1.toString()
        findViewById<TextView>(R.id.unforcedErrorSlicebhPlayer2).text = app.unforcederrorsliceBH2.toString()

        findViewById<TextView>(R.id.unforcedErrorSmashfhPlayer1).text = app.unforcederrorsmashFH1.toString()
        findViewById<TextView>(R.id.unforcedErrorSmashfhPlayer2).text = app.unforcederrorsmashFH2.toString()
        findViewById<TextView>(R.id.unforcedErrorSmashbhPlayer1).text = app.unforcederrorsmashBH1.toString()
        findViewById<TextView>(R.id.unforcedErrorSmashbhPlayer2).text = app.unforcederrorsmashBH2.toString()

        findViewById<TextView>(R.id.unforcedErrorVolleyfhPlayer1).text = app.unforcederrorvolleyFH1.toString()
        findViewById<TextView>(R.id.unforcedErrorVolleyfhPlayer2).text = app.unforcederrorvolleyFH2.toString()
        findViewById<TextView>(R.id.unforcedErrorVolleybhPlayer1).text = app.unforcederrorvolleyBH1.toString()
        findViewById<TextView>(R.id.unforcedErrorVolleybhPlayer2).text = app.unforcederrorvolleyBH2.toString()

        findViewById<TextView>(R.id.unforcedErrorDropshotfhPlayer1).text = app.unforcederrordropshotFH1.toString()
        findViewById<TextView>(R.id.unforcedErrorDropshotfhPlayer2).text = app.unforcederrordropshotFH2.toString()
        findViewById<TextView>(R.id.unforcedErrorDropshotbhPlayer1).text = app.unforcederrordropshotBH1.toString()
        findViewById<TextView>(R.id.unforcedErrorDropshotbhPlayer2).text = app.unforcederrordropshotBH2.toString()

        findViewById<TextView>(R.id.unforcedErrorLobfhPlayer1).text = app.unforcederrorlobFH1.toString()
        findViewById<TextView>(R.id.unforcedErrorLobfhPlayer2).text = app.unforcederrorlobFH2.toString()
        findViewById<TextView>(R.id.unforcedErrorLobbhPlayer1).text = app.unforcederrorlobBH1.toString()
        findViewById<TextView>(R.id.unforcedErrorLobbhPlayer2).text = app.unforcederrorlobBH2.toString()
    }
}