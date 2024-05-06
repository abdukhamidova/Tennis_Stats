package com.anw.tenistats

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
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

class EndOfMatchActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_end_of_match)
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
            startActivity(Intent(this,ActivityMenu::class.java))
        }

        val userEmail = firebaseAuth.currentUser?.email.toString()
        val userEmailView = headerView.findViewById<TextView>(R.id.textViewUserEmail)
        if(userEmail.isNotEmpty()) {
            userEmailView.text = userEmail
        }else {
            userEmailView.text = resources.getString(R.string.user_email)
        }
        //MENU

        val app = application as Stats
        val serve1 = findViewById<TextView>(R.id.textViewServe1EOM)
        val serve2 = findViewById<TextView>(R.id.textViewServe2EOM)
        val player1 = findViewById<TextView>(R.id.textviewPlayer1)
        val player2 = findViewById<TextView>(R.id.textviewPlayer2)
        val set1p1 = findViewById<TextView>(R.id.textViewP1Set1EOM)
        val set2p1 = findViewById<TextView>(R.id.textViewP1Set2EOM)
        val set3p1 = findViewById<TextView>(R.id.textViewP1Set3EOM)
        val set1p2 = findViewById<TextView>(R.id.textViewP2Set1EOM)
        val set2p2 = findViewById<TextView>(R.id.textViewP2Set2EOM)
        val set3p2 = findViewById<TextView>(R.id.textViewP2Set3EOM)
        fillUpScoreInActivityEnd(app,player1, player2, serve1, serve2, set1p1, set1p2, set2p1, set2p2, set3p1, set3p2)

        val user = firebaseAuth.currentUser?.uid
        val matchId = intent.getStringExtra("matchID")
        val winner: String
        //sprawdzenie kto wygral
        winner = if(serve1.text==""){ //wygral player2
            player2.text.toString()
        } //wygral player2
        else{
            player1.text.toString()
        }

        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(user.toString()).child("Matches").child(matchId.toString())
        database.child("servePlayer").setValue("")
        database.child("winner").setValue(winner)
        database.child("set1p1").setValue((set1p1.text.toString()))
        database.child("set2p1").setValue((set2p1.text.toString()))
        database.child("set3p1").setValue((set3p1.text.toString()))
        database.child("set1p2").setValue((set1p2.text.toString()))
        database.child("set2p2").setValue((set2p2.text.toString()))
        database.child("set3p2").setValue((set3p2.text.toString()))
        database.child("pkt1").setValue("")
        database.child("pkt2").setValue("")

        app.isEnd=false

        clear(app)

        findViewById<Button>(R.id.buttonViewStatsEOF).setOnClickListener {
            database.child("data").get().addOnSuccessListener { dataSnapshot ->
                // Pobranie wartości "player1" z bazy danych
                val data = dataSnapshot.getValue(Long::class.java)
                Intent(this,ViewHistoryActivity::class.java).also{
                    it.putExtra("matchDateInMillis",data)
                    startActivity(it)
                    finish()
                }
            }.addOnFailureListener { exception ->
                // Obsługa błędów
            }
        }
    }
}

fun fillUpScoreInActivityEnd(app: Stats,player1: TextView, player2: TextView, serve1: TextView, serve2: TextView,set1p1: TextView, set1p2: TextView, set2p1: TextView, set2p2: TextView, set3p1: TextView, set3p2: TextView) {
    player1.text = app.player1
    player2.text = app.player2
    serve1.text = app.serve1
    serve2.text = app.serve2
    set1p1.text = app.set1p1
    set1p2.text = app.set1p2
    set2p1.text = app.set2p1
    set2p2.text = app.set2p2
    set3p1.text = app.set3p1
    set3p2.text = app.set3p2
}

fun clear(app: Stats){
    app.serve1 = ""
    app.serve2 = ""
    app.set1p1 = "0"
    app.set1p2 = "0"
    app.set2p1 = ""
    app.set2p2 = ""
    app.set3p1 = ""
    app.set3p2 = ""
}