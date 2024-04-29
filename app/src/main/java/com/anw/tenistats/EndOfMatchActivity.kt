package com.anw.tenistats

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EndOfMatchActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_end_of_match)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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

        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        var matchId = intent.getStringExtra("matchID")
        var winner: String ?=null
        //sprawdzenie kto wygral
        if(serve1.text==""){ //wygral player2
            winner=player2.text.toString()
        } //wygral player2
        else{
            winner=player1.text.toString()
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

        app.isEnd=false

        clear(app)
        findViewById<Button>(R.id.buttonMenuEnd).setOnClickListener {
            startActivity(Intent(this,ActivityMenu::class.java))
        }
    }
}

fun fillUpScoreInActivityEnd(app: Stats,player1: TextView, player2: TextView, serve1: TextView, serve2: TextView,set1p1: TextView, set1p2: TextView, set2p1: TextView, set2p2: TextView, set3p1: TextView, set3p2: TextView):Unit {
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

fun clear(app: Stats):Unit{
    app.serve1 = ""
    app.serve2 = ""
    app.set1p1 = "0"
    app.set1p2 = "0"
    app.set2p1 = ""
    app.set2p2 = ""
    app.set3p1 = ""
    app.set3p2 = ""
}