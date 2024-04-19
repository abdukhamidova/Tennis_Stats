package com.anw.tenistats

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ActivityBallInPlay : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ball_in_play)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //serve1.visibility = View.VISIBLE

        val app = application as Stats
        val serve1 = findViewById<TextView>(R.id.textViewServe1BIP)
        //val serve1 = binding.textViewServe1BIP
        val serve2 = findViewById<TextView>(R.id.textViewServe2BIP)
        //val serve2 = binding.textViewServe2BIP
        val pkt1 = findViewById<TextView>(R.id.textViewPlayer1PktBIP)
        val set1p1 = findViewById<TextView>(R.id.textViewPlayer1Set1BIP)
        val set2p1 = findViewById<TextView>(R.id.textViewPlayer1Set2BIP)
        val set3p1 = findViewById<TextView>(R.id.textViewPlayer1Set3BIP)
        val pkt2 = findViewById<TextView>(R.id.textViewPlayer2PktBIP)
        val set1p2 = findViewById<TextView>(R.id.textViewPlayer2Set1BIP)
        val set2p2 = findViewById<TextView>(R.id.textViewPlayer2Set2BIP)
        val set3p2 = findViewById<TextView>(R.id.textViewPlayer2Set3BIP)


        fillUpScoreInActivity(app,findViewById<TextView>(R.id.textviewPlayer1),findViewById<TextView>(R.id.textviewPlayer2),serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2) //ustawienie poczatkowe wyniku

        val player1 = findViewById<TextView>(R.id.textviewPlayer1)
        val player2 = findViewById<TextView>(R.id.textviewPlayer2)

        //link do Menu
        findViewById<Button>(R.id.buttonMenuBIP).setOnClickListener {
            startActivity(Intent(this,ActivityMenu::class.java))
        }

        //link do poprzedniego activity (StartPoint)
        /*findViewById<ImageButton>(R.id.imageButtonBack).setOnClickListener {
            val player1=findViewById<TextView>(R.id.textviewPlayer1).text.toString()
            val player2=findViewById<TextView>(R.id.textviewPlayer2).text.toString()

            val intent=Intent(this,ActivityStartPoint::class.java).also{
                it.putExtra("DanePlayer1",player1)
                it.putExtra("DanePlayer2",player2)
                startActivity(it)
            }
        }*/

        findViewById<TextView>(R.id.textPL1).text = player1.text
        findViewById<TextView>(R.id.textPL2).text = player2.text

        findViewById<Button>(R.id.buttonWinner1).setOnClickListener {
            val intent=Intent(this,DetailsActivity::class.java).also{
                it.putExtra("Pkt1",intent.getStringExtra("Pkt1"))
                it.putExtra("Pkt2",intent.getStringExtra("Pkt2"))
                it.putExtra("matchID",intent.getStringExtra("matchID"))
                it.putExtra("gameID",intent.getStringExtra("gameID"))
                it.putExtra("setID",intent.getStringExtra("setID"))
                it.putExtra("Kto",player1.text)
                it.putExtra("Co","Winner")
                startActivity(it)
            }
        }

        findViewById<Button>(R.id.buttonWinner2).setOnClickListener {
            val intent=Intent(this,DetailsActivity::class.java).also{
                it.putExtra("Pkt1",intent.getStringExtra("Pkt1"))
                it.putExtra("Pkt2",intent.getStringExtra("Pkt2"))
                it.putExtra("matchID",intent.getStringExtra("matchID"))
                it.putExtra("gameID",intent.getStringExtra("gameID"))
                it.putExtra("setID",intent.getStringExtra("setID"))
                it.putExtra("Kto",player2.text)
                it.putExtra("Co","Winner")
                startActivity(it)
            }
        }

        findViewById<Button>(R.id.buttonForcedError1).setOnClickListener {
            val intent=Intent(this,DetailsActivity::class.java).also{
                it.putExtra("Pkt1",intent.getStringExtra("Pkt1"))
                it.putExtra("Pkt2",intent.getStringExtra("Pkt2"))
                it.putExtra("matchID",intent.getStringExtra("matchID"))
                it.putExtra("gameID",intent.getStringExtra("gameID"))
                it.putExtra("setID",intent.getStringExtra("setID"))
                it.putExtra("Kto",player1.text)
                it.putExtra("Co","Forced Error")
                startActivity(it)
            }
        }

        findViewById<Button>(R.id.buttonForcedError2).setOnClickListener {
            val intent=Intent(this,DetailsActivity::class.java).also{
                it.putExtra("Pkt1",intent.getStringExtra("Pkt1"))
                it.putExtra("Pkt2",intent.getStringExtra("Pkt2"))
                it.putExtra("matchID",intent.getStringExtra("matchID"))
                it.putExtra("gameID",intent.getStringExtra("gameID"))
                it.putExtra("setID",intent.getStringExtra("setID"))
                it.putExtra("Kto",player2.text)
                it.putExtra("Co","Forced Error")
                startActivity(it)
            }
        }

        findViewById<Button>(R.id.buttonUnforcedError1).setOnClickListener {
            val intent=Intent(this,DetailsActivity::class.java).also{
                it.putExtra("Pkt1",intent.getStringExtra("Pkt1"))
                it.putExtra("Pkt2",intent.getStringExtra("Pkt2"))
                it.putExtra("matchID",intent.getStringExtra("matchID"))
                it.putExtra("gameID",intent.getStringExtra("gameID"))
                it.putExtra("setID",intent.getStringExtra("setID"))
                it.putExtra("Kto",player1.text)
                it.putExtra("Co","Unforced Error")
                startActivity(it)
            }
        }

        findViewById<Button>(R.id.buttonUnforcedError2).setOnClickListener {
            val intent=Intent(this,DetailsActivity::class.java).also{
                it.putExtra("Pkt1",intent.getStringExtra("Pkt1"))
                it.putExtra("Pkt2",intent.getStringExtra("Pkt2"))
                it.putExtra("matchID",intent.getStringExtra("matchID"))
                it.putExtra("gameID",intent.getStringExtra("gameID"))
                it.putExtra("setID",intent.getStringExtra("setID"))
                it.putExtra("Kto",player2.text)
                it.putExtra("Co","Unforced Error")
                startActivity(it)
            }
        }
    }
}