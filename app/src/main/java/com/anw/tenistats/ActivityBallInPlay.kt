package com.anw.tenistats

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ActivityBallInPlay : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var database: DatabaseReference
    var matchId: String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ball_in_play)
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
        navigationDrawerHelper.setupNavigationDrawer(drawerLayout, navigationView, firebaseAuth, true)
        val backButton = findViewById<ImageButton>(R.id.buttonUndo)
        backButton.setOnClickListener{
            Toast.makeText(this,"Point cleared", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this,ActivityStartPoint::class.java))
            finish()
        }

        val userEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        val userEmailView = headerView.findViewById<TextView>(R.id.textViewUserEmail)
        if(userEmail.isNotEmpty()) {
            userEmailView.text = userEmail
        }else {
            userEmailView.text = resources.getString(R.string.user_email)
        }
        //MENU

        val app = application as Stats
        val serve1 = findViewById<TextView>(R.id.textViewServe1BIP)
        val serve2 = findViewById<TextView>(R.id.textViewServe2BIP)
        val pkt1 = findViewById<TextView>(R.id.textViewPlayer1PktBIP)
        val set1p1 = findViewById<TextView>(R.id.textViewPlayer1Set1BIP)
        val set2p1 = findViewById<TextView>(R.id.textViewPlayer1Set2BIP)
        val set3p1 = findViewById<TextView>(R.id.textViewPlayer1Set3BIP)
        val pkt2 = findViewById<TextView>(R.id.textViewPlayer2PktBIP)
        val set1p2 = findViewById<TextView>(R.id.textViewPlayer2Set1BIP)
        val set2p2 = findViewById<TextView>(R.id.textViewPlayer2Set2BIP)
        val set3p2 = findViewById<TextView>(R.id.textViewPlayer2Set3BIP)

        val player1 = findViewById<TextView>(R.id.textviewPlayer1)
        val player2 = findViewById<TextView>(R.id.textviewPlayer2)

        val user = firebaseAuth.currentUser?.uid

        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(user.toString())
        database.child("Current match").get().addOnSuccessListener {dataSnapshot ->
            matchId = dataSnapshot.getValue(String::class.java)
            database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(user.toString()).child("Matches").child(matchId.toString())

            //ustawienie wyniku w tabeli
            setscore(player1,player2,serve1,serve2,set1p1,set2p1,set3p1,set1p2,set2p2,set3p2,pkt1,pkt2)
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to read Current Match ID", Toast.LENGTH_SHORT).show()
        }

        //findViewById<TextView>(R.id.textPL1).text = player1.text
        //findViewById<TextView>(R.id.textPL2).text = player2.text

        fillUpScoreInActivity(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2) //ustawienie poczatkowe wyniku

        findViewById<Button>(R.id.buttonWinner1).setOnClickListener {
            Intent(this,DetailsActivity::class.java).also{
                it.putExtra("Pkt1",intent.getStringExtra("Pkt1"))
                it.putExtra("Pkt2",intent.getStringExtra("Pkt2"))
                it.putExtra("matchID",intent.getStringExtra("matchID"))
                it.putExtra("gameID",intent.getStringExtra("gameID"))
                it.putExtra("setID",intent.getStringExtra("setID"))
                it.putExtra("Kto",player1.text)
                it.putExtra("Co","Winner")
                startActivity(it)
                finish()
            }
        }

        findViewById<Button>(R.id.buttonWinner2).setOnClickListener {
            Intent(this,DetailsActivity::class.java).also{
                it.putExtra("Pkt1",intent.getStringExtra("Pkt1"))
                it.putExtra("Pkt2",intent.getStringExtra("Pkt2"))
                it.putExtra("matchID",intent.getStringExtra("matchID"))
                it.putExtra("gameID",intent.getStringExtra("gameID"))
                it.putExtra("setID",intent.getStringExtra("setID"))
                it.putExtra("Kto",player2.text)
                it.putExtra("Co","Winner")
                startActivity(it)
                finish()
            }
        }

        findViewById<Button>(R.id.buttonForcedError1).setOnClickListener {
            Intent(this,DetailsActivity::class.java).also{
                it.putExtra("Pkt1",intent.getStringExtra("Pkt1"))
                it.putExtra("Pkt2",intent.getStringExtra("Pkt2"))
                it.putExtra("matchID",intent.getStringExtra("matchID"))
                it.putExtra("gameID",intent.getStringExtra("gameID"))
                it.putExtra("setID",intent.getStringExtra("setID"))
                it.putExtra("Kto",player1.text)
                it.putExtra("Co","Forced Error")
                startActivity(it)
                finish()
            }
        }

        findViewById<Button>(R.id.buttonForcedError2).setOnClickListener {
            Intent(this,DetailsActivity::class.java).also{
                it.putExtra("Pkt1",intent.getStringExtra("Pkt1"))
                it.putExtra("Pkt2",intent.getStringExtra("Pkt2"))
                it.putExtra("matchID",intent.getStringExtra("matchID"))
                it.putExtra("gameID",intent.getStringExtra("gameID"))
                it.putExtra("setID",intent.getStringExtra("setID"))
                it.putExtra("Kto",player2.text)
                it.putExtra("Co","Forced Error")
                startActivity(it)
                finish()
            }
        }

        findViewById<Button>(R.id.buttonUnforcedError1).setOnClickListener {
            Intent(this,DetailsActivity::class.java).also{
                it.putExtra("Pkt1",intent.getStringExtra("Pkt1"))
                it.putExtra("Pkt2",intent.getStringExtra("Pkt2"))
                it.putExtra("matchID",intent.getStringExtra("matchID"))
                it.putExtra("gameID",intent.getStringExtra("gameID"))
                it.putExtra("setID",intent.getStringExtra("setID"))
                it.putExtra("Kto",player1.text)
                it.putExtra("Co","Unforced Error")
                startActivity(it)
                finish()
            }
        }

        findViewById<Button>(R.id.buttonUnforcedError2).setOnClickListener {
            Intent(this,DetailsActivity::class.java).also{
                it.putExtra("Pkt1",intent.getStringExtra("Pkt1"))
                it.putExtra("Pkt2",intent.getStringExtra("Pkt2"))
                it.putExtra("matchID",intent.getStringExtra("matchID"))
                it.putExtra("gameID",intent.getStringExtra("gameID"))
                it.putExtra("setID",intent.getStringExtra("setID"))
                it.putExtra("Kto",player2.text)
                it.putExtra("Co","Unforced Error")
                startActivity(it)
                finish()
            }
        }
    }
    fun setscore(player1: TextView,player2: TextView,serve1: TextView,serve2: TextView,set1p1: TextView,set2p1: TextView,set3p1: TextView,set1p2: TextView,set2p2: TextView,set3p2: TextView,pkt1:TextView,pkt2:TextView)
    {
        database.child("player1").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val player1Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            player1.text = player1Value.toString()
            findViewById<TextView>(R.id.textPL1).text = player1Value.toString()
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }

        database.child("player2").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val player2Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            player2.text = player2Value.toString()
            findViewById<TextView>(R.id.textPL2).text = player2Value.toString()
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
                serve1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_laurel3, 0, 0, 0)
                serve2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_laurel3, 0, 0, 0)
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