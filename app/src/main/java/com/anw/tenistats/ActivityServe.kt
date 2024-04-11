package com.anw.tenistats

import android.content.Intent
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ActivityServe : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_serve)
        //link do menu
        findViewById<Button>(R.id.buttonMenu).setOnClickListener {
        val intent = Intent(this, ActivityMenu::class.java)
        startActivity(intent)
        }
        //link do poprzedniego activity (StartNew)
        findViewById<ImageButton>(R.id.imageButtonBack).setOnClickListener {
            val intent = Intent(this, StartNewActivity::class.java)
            startActivity(intent)
        }
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
        val app = application as Stats
        app.player1 = findViewById<Button>(R.id.buttonPlayer1).text.toString()
        app.player2 = findViewById<Button>(R.id.buttonPlayer2).text.toString()

        //kliknięcie na gracza, który serwuje jako pierwsze
        //& przekierowanie do kolejnej aktywności
        findViewById<Button>(R.id.buttonPlayer1).setOnClickListener{
            app.serve1="1"
            callActivity()
        }
        findViewById<Button>(R.id.buttonPlayer2).setOnClickListener{
            app.serve2="1"
            callActivity()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    //przesłanie informacji o garczach do następnego activity (StartPoint)
    private fun callActivity() {
        val buttonPlayer1=findViewById<Button>(R.id.buttonPlayer1)
        val player1=buttonPlayer1.text.toString()
        val buttonPlayer2=findViewById<Button>(R.id.buttonPlayer2)
        val player2=buttonPlayer2.text.toString()

        val intent=Intent(this,ActivityStartPoint::class.java).also{
            it.putExtra("DanePlayer1",player1)
            it.putExtra("DanePlayer2",player2)
            startActivity(it)
        }
    }
}