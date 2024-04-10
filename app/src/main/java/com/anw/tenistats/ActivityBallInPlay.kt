package com.anw.tenistats

import android.content.Intent
import android.graphics.Typeface
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

class ActivityBallInPlay : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ball_in_play)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

        fillUpScoreInActivity(app,findViewById<TextView>(R.id.textviewPlayer1),findViewById<TextView>(R.id.textviewPlayer2),serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2) //ustawienie poczatkowe wyniku

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("Mecz").child("row")
        val historia = Row()
        val match = Match(Row(
            serwis = historia.serwis,
            set1 = historia.set1,
            set2 = historia.set2,
            set3 = historia.set3,
            wynik = historia.wynik,
            kto = historia.kto,
            co = historia.co,
            czym = historia.czym,
            gdzie = historia.gdzie
        ))

        val player1 = findViewById<TextView>(R.id.textviewPlayer1)
        val player2 = findViewById<TextView>(R.id.textviewPlayer2)

        findViewById<Button>(R.id.buttonMenuBIP).setOnClickListener {
            startActivity(Intent(this,ActivityMenu::class.java))
        }

        findViewById<TextView>(R.id.textPL1).text = player1.text
        findViewById<TextView>(R.id.textPL2).text = player2.text

        var tekst: String = ""
        findViewById<Button>(R.id.buttonWinner1).setOnClickListener {
            historia.kto.add(player1.text.toString())
            historia.co.add("Winner")
            database.child("kto").push().setValue(player1.text.toString())
            database.child("co").push().setValue("Winner")
            val player = player1
            tekst = "Winner"
            callActivity(tekst,player)
        }

        findViewById<Button>(R.id.buttonWinner2).setOnClickListener {
            historia.kto.add(player2.text.toString())
            historia.co.add("Winner")
            database.child("kto").push().setValue(player2.text.toString())
            database.child("co").push().setValue("Winner")
            val player = player2
            tekst = "Winner"
            callActivity(tekst,player)
        }

        findViewById<Button>(R.id.buttonForcedError1).setOnClickListener {
            historia.kto.add(player1.text.toString())
            historia.co.add("Forced Error")
            database.child("kto").push().setValue(player1.text.toString())
            database.child("co").push().setValue("Forced Error")
            val player = player1
            tekst = "Forced Error"
            callActivity(tekst,player)
        }

        findViewById<Button>(R.id.buttonForcedError2).setOnClickListener {
            historia.kto.add(player2.text.toString())
            historia.co.add("Forced Error")
            database.child("kto").push().setValue(player2.text.toString())
            database.child("co").push().setValue("Forced Error")
            val player = player2
            tekst = "Forced Error"
            callActivity(tekst,player)
        }

        findViewById<Button>(R.id.buttonUnforcedError1).setOnClickListener {
            historia.kto.add(player1.text.toString())
            historia.co.add("Unforced Error")
            database.child("kto").push().setValue(player1.text.toString())
            database.child("co").push().setValue("Unforced Error")
            val player = player1
            tekst = "Unforced Error"
            callActivity(tekst,player)
        }

        findViewById<Button>(R.id.buttonUnforcedError2).setOnClickListener {
            historia.kto.add(player2.text.toString())
            historia.co.add("Unforced Error")
            database.child("kto").push().setValue(player2.text.toString())
            database.child("co").push().setValue("Unforced Error")
            val player = player2
            tekst = "Unforced Error"
            callActivity(tekst,player)
        }
    }

    fun callActivity(tekst: String, player: TextView) {
        val intent= Intent(this,DetailsActivity::class.java).also{
            it.putExtra("Text name",player.text)
            it.putExtra("Text shot",tekst)
            startActivity(it)
        }
    }
}