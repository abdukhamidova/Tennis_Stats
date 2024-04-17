package com.anw.tenistats

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.anw.tenistats.com.anw.tenistats.AddPointDialog

class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //ustawienie textView kto i co
        findViewById<TextView>(R.id.textPlayerName).apply {
            text = intent.getStringExtra("Kto")
        }
        findViewById<TextView>(R.id.textShot).apply {
            text = intent.getStringExtra("Co")
        }

        val app = application as Stats
        val serve1 = findViewById<TextView>(R.id.textViewServe1Details)
        val serve2 = findViewById<TextView>(R.id.textViewServe2Details)
        val pkt1 = findViewById<TextView>(R.id.textviewPlayer1PktDetails)
        val set1p1 = findViewById<TextView>(R.id.textviewPlayer1Set1Details)
        val set2p1 = findViewById<TextView>(R.id.textviewPlayer1Set2Details)
        val set3p1 = findViewById<TextView>(R.id.textviewPlayer1Set3Details)
        val pkt2 = findViewById<TextView>(R.id.textviewPlayer2PktDetails)
        val set1p2 = findViewById<TextView>(R.id.textviewPlayer2Set1Details)
        val set2p2 = findViewById<TextView>(R.id.textviewPlayer2Set2Details)
        val set3p2 = findViewById<TextView>(R.id.textviewPlayer2Set3Details)
        val addPointDialog = AddPointDialog(this,false)
        fillUpScoreInActivity(app,findViewById<TextView>(R.id.textviewPlayer1Details),findViewById<TextView>(R.id.textviewPlayer2Details),serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)

        val player1 = findViewById<TextView>(R.id.textviewPlayer1Details)
        val player2 = findViewById<TextView>(R.id.textviewPlayer2Details)
        //spr kto serwuje
        var czyPlayer1: Boolean = false
        if(player1.text==intent.getStringExtra("Kto")){
            czyPlayer1 = true
        }

        //button Menu
        findViewById<Button>(R.id.buttonMenuDetails).setOnClickListener {
            startActivity(Intent(this,ActivityMenu::class.java))
        }
        //link do poprzedniego activity (BallInPlay)
        /*findViewById<ImageButton>(R.id.imageButtonBack).setOnClickListener {
            val player1=findViewById<TextView>(R.id.textviewPlayer1).text.toString()
            val player2=findViewById<TextView>(R.id.textviewPlayer2).text.toString()

            val intent=Intent(this,ActivityBallInPlay::class.java).also{
                it.putExtra("DanePlayer1",player1)
                it.putExtra("DanePlayer2",player2)
                startActivity(it)
            }
        }*/

        findViewById<Button>(R.id.buttonGround).setOnClickListener {
            if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
            {
                addPointDialog.show(
                    intent.getStringExtra("Pkt1").toString(),
                    intent.getStringExtra("Pkt2").toString(),
                    intent.getStringExtra("Kto").toString(),
                    intent.getStringExtra("Co").toString(),
                    "Ground",
                    "Forehand",
                    intent.getStringExtra("matchID").toString(),
                    intent.getStringExtra("gameID").toString(),
                    intent.getStringExtra("setID").toString())
            }
            else{
                addPointDialog.show(
                    intent.getStringExtra("Pkt1").toString(),
                    intent.getStringExtra("Pkt2").toString(),
                    intent.getStringExtra("Kto").toString(),
                    intent.getStringExtra("Co").toString(),
                    "Ground",
                    "Backhand",
                    intent.getStringExtra("matchID").toString(),
                    intent.getStringExtra("gameID").toString(),
                    intent.getStringExtra("setID").toString())
            }
            if(intent.getStringExtra("Co")=="Winner") {
                if(czyPlayer1) {
                    score(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                }
                else{
                    score(app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                }
            }//przyznanie punktu
            else{
                if(czyPlayer1) {
                    score(app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                }
                else{
                    score(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                }
            }
        }

        findViewById<Button>(R.id.buttonVolley).setOnClickListener {
            if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
            {
                addPointDialog.show(
                    intent.getStringExtra("Pkt1").toString(),
                    intent.getStringExtra("Pkt2").toString(),
                    intent.getStringExtra("Kto").toString(),
                    intent.getStringExtra("Co").toString(),
                    "Volley",
                    "Forehand",
                    intent.getStringExtra("matchID").toString(),
                    intent.getStringExtra("gameID").toString(),
                    intent.getStringExtra("setID").toString())
            }
            else{
                addPointDialog.show(
                    intent.getStringExtra("Pkt1").toString(),
                    intent.getStringExtra("Pkt2").toString(),
                    intent.getStringExtra("Kto").toString(),
                    intent.getStringExtra("Co").toString(),
                    "Volley",
                    "Backhand",
                    intent.getStringExtra("matchID").toString(),
                    intent.getStringExtra("gameID").toString(),
                    intent.getStringExtra("setID").toString())
            }
            if(intent.getStringExtra("Co")=="Winner") {
                if(czyPlayer1) {
                    score(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                }
                else{
                    score(app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                }
            }//przyznanie punktu
            else{
                if(czyPlayer1) {
                    score(app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                }
                else{
                    score(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                }
            }
        }

        findViewById<Button>(R.id.buttonLob).setOnClickListener {
            if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
            {
                addPointDialog.show(
                    intent.getStringExtra("Pkt1").toString(),
                    intent.getStringExtra("Pkt2").toString(),
                    intent.getStringExtra("Kto").toString(),
                    intent.getStringExtra("Co").toString(),
                    "Lob",
                    "Forehand",
                    intent.getStringExtra("matchID").toString(),
                    intent.getStringExtra("gameID").toString(),
                    intent.getStringExtra("setID").toString())
            }
            else{
                addPointDialog.show(
                    intent.getStringExtra("Pkt1").toString(),
                    intent.getStringExtra("Pkt2").toString(),
                    intent.getStringExtra("Kto").toString(),
                    intent.getStringExtra("Co").toString(),
                    "Lob",
                    "Backhand",
                    intent.getStringExtra("matchID").toString(),
                    intent.getStringExtra("gameID").toString(),
                    intent.getStringExtra("setID").toString())
            }
            if(intent.getStringExtra("Co")=="Winner") {
                if(czyPlayer1) {
                    score(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                }
                else{
                    score(app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                }
            }//przyznanie punktu
            else{
                if(czyPlayer1) {
                    score(app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                }
                else{
                    score(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                }
            }
        }

        findViewById<Button>(R.id.buttonSlice).setOnClickListener {
            if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
            {
                addPointDialog.show(
                    intent.getStringExtra("Pkt1").toString(),
                    intent.getStringExtra("Pkt2").toString(),
                    intent.getStringExtra("Kto").toString(),
                    intent.getStringExtra("Co").toString(),
                    "Slice",
                    "Forehand",
                    intent.getStringExtra("matchID").toString(),
                    intent.getStringExtra("gameID").toString(),
                    intent.getStringExtra("setID").toString())
            }
            else{
                addPointDialog.show(
                    intent.getStringExtra("Pkt1").toString(),
                    intent.getStringExtra("Pkt2").toString(),
                    intent.getStringExtra("Kto").toString(),
                    intent.getStringExtra("Co").toString(),
                    "Slice",
                    "Backhand",
                    intent.getStringExtra("matchID").toString(),
                    intent.getStringExtra("gameID").toString(),
                    intent.getStringExtra("setID").toString())
            }
            if(intent.getStringExtra("Co")=="Winner") {
                if(czyPlayer1) {
                    score(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                }
                else{
                    score(app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                }
            }//przyznanie punktu
            else{
                if(czyPlayer1) {
                    score(app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                }
                else{
                    score(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                }
            }
        }

        findViewById<Button>(R.id.buttonSmash).setOnClickListener {
            if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
            {
                addPointDialog.show(
                    intent.getStringExtra("Pkt1").toString(),
                    intent.getStringExtra("Pkt2").toString(),
                    intent.getStringExtra("Kto").toString(),
                    intent.getStringExtra("Co").toString(),
                    "Smash",
                    "Forehand",
                    intent.getStringExtra("matchID").toString(),
                    intent.getStringExtra("gameID").toString(),
                    intent.getStringExtra("setID").toString())
            }
            else{
                addPointDialog.show(
                    intent.getStringExtra("Pkt1").toString(),
                    intent.getStringExtra("Pkt2").toString(),
                    intent.getStringExtra("Kto").toString(),
                    intent.getStringExtra("Co").toString(),
                    "Smash",
                    "Backhand",
                    intent.getStringExtra("matchID").toString(),
                    intent.getStringExtra("gameID").toString(),
                    intent.getStringExtra("setID").toString())
            }
            if(intent.getStringExtra("Co")=="Winner") {
                if(czyPlayer1) {
                    score(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                }
                else{
                    score(app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                }
            }//przyznanie punktu
            else{
                if(czyPlayer1) {
                    score(app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                }
                else{
                    score(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                }
            }
        }

        findViewById<Button>(R.id.buttonDropshot).setOnClickListener {
            if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
            {
                addPointDialog.show(
                    intent.getStringExtra("Pkt1").toString(),
                    intent.getStringExtra("Pkt2").toString(),
                    intent.getStringExtra("Kto").toString(),
                    intent.getStringExtra("Co").toString(),
                    "Dropshot",
                    "Forehand",
                    intent.getStringExtra("matchID").toString(),
                    intent.getStringExtra("gameID").toString(),
                    intent.getStringExtra("setID").toString())
            }
            else{
                addPointDialog.show(
                    intent.getStringExtra("Pkt1").toString(),
                    intent.getStringExtra("Pkt2").toString(),
                    intent.getStringExtra("Kto").toString(),
                    intent.getStringExtra("Co").toString(),
                    "Dropshot",
                    "Backhand",
                    intent.getStringExtra("matchID").toString(),
                    intent.getStringExtra("gameID").toString(),
                    intent.getStringExtra("setID").toString())
            }
            if(intent.getStringExtra("Co")=="Winner") {
                if(czyPlayer1) {
                    score(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                }
                else{
                    score(app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                }
            }//przyznanie punktu
            else{
                if(czyPlayer1) {
                    score(app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                }
                else{
                    score(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                }
            }
        }
    }
}