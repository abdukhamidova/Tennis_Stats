package com.anw.tenistats

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ActivityServe : AppCompatActivity() {
    private var matchId: String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_serve)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val app = application as Stats
        matchId = intent.getStringExtra("matchID")

        //pobranie nazw graczy z activity, skąd nastąpiło przekierowanie
        //& przypisanie do tekstu na buttonach
        val player1 = intent.getStringExtra("DanePlayer1")
        findViewById<Button>(R.id.buttonPlayer1).apply {
            text=player1
        }
        val player2 = intent.getStringExtra("DanePlayer2")
        findViewById<Button>(R.id.buttonPlayer2).apply {
            text = player2
        }

        //dodaje imiona playerow do statystyk ~u
        //val app = application as Stats
        app.player1 = findViewById<Button>(R.id.buttonPlayer1).text.toString()
        app.player2 = findViewById<Button>(R.id.buttonPlayer2).text.toString()

        //kliknięcie na gracza, który serwuje jako pierwsze
        //& przekierowanie do kolejnej aktywności
        findViewById<Button>(R.id.buttonPlayer1).setOnClickListener{
            app.serve1="1" //do statysyk
            app.serve2=""
            callActivity()
        }
        findViewById<Button>(R.id.buttonPlayer2).setOnClickListener{
            app.serve1=""
            app.serve2="1" //do statysyk
            callActivity()
        }

    }

    //przesłanie informacji o garczach do następnego activity (StartPoint)
    private fun callActivity() {
        val buttonPlayer1=findViewById<Button>(R.id.buttonPlayer1)
        val player1=buttonPlayer1.text.toString()
        val buttonPlayer2=findViewById<Button>(R.id.buttonPlayer2)
        val player2=buttonPlayer2.text.toString()

        Intent(this,ActivityStartPoint::class.java).also{
            it.putExtra("DanePlayer1",player1)
            it.putExtra("DanePlayer2",player2)
            it.putExtra("matchID",matchId)
            startActivity(it)
        }
    }
}