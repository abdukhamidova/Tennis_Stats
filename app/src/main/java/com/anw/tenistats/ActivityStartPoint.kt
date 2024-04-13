package com.anw.tenistats

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database

class ActivityStartPoint : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start_point)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var matchId = intent.getStringExtra("matchID")

        val app = application as Stats

        //val app = application as Stats
        val player1 = findViewById<TextView>(R.id.textviewPlayer1)
        val player2 = findViewById<TextView>(R.id.textviewPlayer2)
        val serve1 = findViewById<TextView>(R.id.textViewBallPl1)
        val serve2 = findViewById<TextView>(R.id.textViewBallPl2)
        val pkt1 = findViewById<TextView>(R.id.textViewPktPl1)
        val set1p1 = findViewById<TextView>(R.id.textViewSet1Pl1)
        val set2p1 = findViewById<TextView>(R.id.textViewSet2Pl1)
        val set3p1 = findViewById<TextView>(R.id.textViewSet3Pl1)
        val pkt2 = findViewById<TextView>(R.id.textViewPktPl2)
        val set1p2 = findViewById<TextView>(R.id.textViewSet1Pl2)
        val set2p2 = findViewById<TextView>(R.id.textViewSet2Pl2)
        val set3p2 = findViewById<TextView>(R.id.textViewSet3Pl2)

        fillUpScoreInActivity(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)


        findViewById<Button>(R.id.buttonMenuSP).setOnClickListener {
            startActivity(Intent(this,ActivityMenu::class.java))
        }
        //link do poprzedniego activity (Serve)
        /*findViewById<ImageButton>(R.id.imageButtonBack).setOnClickListener {
            val player1=findViewById<TextView>(R.id.textviewPlayer1).text.toString()
            val player2=findViewById<TextView>(R.id.textviewPlayer2).text.toString()

            val intent=Intent(this,ActivityServe::class.java).also{
                it.putExtra("DanePlayer1",player1)
                it.putExtra("DanePlayer2",player2)
                startActivity(it)
            }
        }*/

        var game: Int
        var set = 1
        if(set2p1.text=="") {
            val game1=set1p1.text.toString()
            val game2=set1p2.text.toString()
            game = game1.toInt() + game2.toInt() + 1
        }//jest set 1
        else if(set3p1.text==""){
            set=2
            val game1=set2p1.text.toString()
            val game2=set2p2.text.toString()
            game = game1.toInt() + game2.toInt() + 1
        }//jest set 2
        else{
            set=3
            val game1=set3p1.text.toString()
            val game2=set3p2.text.toString()
            game = game1.toInt() + game2.toInt() + 1
        }//jest set 3

        findViewById<Button>(R.id.buttonAce).setOnClickListener {
            if (serve1.text != "") { //serwuje player 1
                app.totalpoints1++
                if (findViewById<Button>(R.id.buttonFault).text == "Fault") { //trafiony 1 serwis
                    app.firstservein1++
                }
                else if (findViewById<Button>(R.id.buttonFault).text == "Double Fault") {
                    app.secondservein1++
                    findViewById<Button>(R.id.buttonFault).text = "Fault"
                    findViewById<TextView>(R.id.textViewFS).text = "1st Serve"
                }
                app.ace1++
                val intent=Intent(this,AddActivity::class.java).also{
                    it.putExtra("Pkt1",pkt1.text)
                    it.putExtra("Pkt2",pkt2.text)
                    it.putExtra("Kto",player1.text)
                    it.putExtra("Co","Ace")
                    it.putExtra("Gdzie","")
                    it.putExtra("Czym","")
                    it.putExtra("matchID",matchId)
                    it.putExtra("gameID",game.toString())
                    it.putExtra("setID",set.toString())
                    startActivity(it)
                }
                score(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            }
            else { //serwuje player2
                app.totalpoints2++
                if (findViewById<Button>(R.id.buttonFault).text == "Fault") { //trafiony 1 serwis
                    app.firstservein2++
                }
                else if (findViewById<Button>(R.id.buttonFault).text == "Double Fault") {
                    app.secondservein2++
                    findViewById<Button>(R.id.buttonFault).text = "Fault"
                    findViewById<TextView>(R.id.textViewFS).text = "1st Serve"
                }
                app.ace2++
                val intent=Intent(this,AddActivity::class.java).also{
                    it.putExtra("Pkt1",pkt1.text)
                    it.putExtra("Pkt2",pkt2.text)
                    it.putExtra("Kto",player2.text)
                    it.putExtra("Co","Ace")
                    it.putExtra("Gdzie","")
                    it.putExtra("Czym","")
                    it.putExtra("matchID",matchId)
                    it.putExtra("gameID",game.toString())
                    it.putExtra("setID",set.toString())
                    startActivity(it)
                }
                score(app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
            }
        }

        findViewById<Button>(R.id.buttonFault).setOnClickListener {
            if (findViewById<Button>(R.id.buttonFault).text == "Fault") {
                app.serwis=2
                findViewById<Button>(R.id.buttonFault).text = "Double Fault"
                findViewById<Button>(R.id.buttonFault).textSize = 15.4f
                findViewById<TextView>(R.id.textViewFS).text = "2nd Serve"
            }
            else if (findViewById<Button>(R.id.buttonFault).text == "Double Fault") {
                app.serwis=0
                findViewById<Button>(R.id.buttonFault).text = "Fault"
                findViewById<Button>(R.id.buttonFault).textSize = 19.9f
                findViewById<TextView>(R.id.textViewFS).text = "1st Serve"
                if (serve1.text != "") {
                    app.totalpoints2++
                    app.doublefault1++
                    val intent=Intent(this,AddActivity::class.java).also{
                        it.putExtra("Pkt1",pkt1.text)
                        it.putExtra("Pkt2",pkt2.text)
                        it.putExtra("Kto",player1.text)
                        it.putExtra("Co","Double Fault")
                        it.putExtra("Gdzie","")
                        it.putExtra("Czym","")
                        it.putExtra("matchID",matchId)
                        it.putExtra("gameID",game.toString())
                        it.putExtra("setID",set.toString())
                        startActivity(it)
                    }
                    score(app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                }
                else {
                    app.totalpoints1++
                    app.doublefault2++
                    val intent=Intent(this,AddActivity::class.java).also{
                        it.putExtra("Pkt1",pkt1.text)
                        it.putExtra("Pkt2",pkt2.text)
                        it.putExtra("Kto",player2.text)
                        it.putExtra("Co","Double Fault")
                        it.putExtra("Gdzie","")
                        it.putExtra("Czym","")
                        it.putExtra("matchID",matchId)
                        it.putExtra("gameID",game.toString())
                        it.putExtra("setID",set.toString())
                        startActivity(it)
                    }
                    score(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                }
            }
        }

        findViewById<Button>(R.id.buttonRWF).setOnClickListener{
            if (serve1.text != "") { //serwuje player 1
                app.totalpoints2++
                if (findViewById<Button>(R.id.buttonFault).text == "Fault") { //trafiony 1 serwis
                    app.firstservein1++
                }
                else if (findViewById<Button>(R.id.buttonFault).text == "Double Fault") {
                    app.secondservein1++
                    findViewById<Button>(R.id.buttonFault).text = "Fault"
                    findViewById<TextView>(R.id.textViewFS).text = "1st Serve"
                }
                app.returnwinnerFH2++
                val intent=Intent(this,AddActivity::class.java).also{
                    it.putExtra("Pkt1",pkt1.text)
                    it.putExtra("Pkt2",pkt2.text)
                    it.putExtra("Kto",player2.text)
                    it.putExtra("Co","Winner")
                    it.putExtra("Gdzie","Return")
                    it.putExtra("Czym","Forehand")
                    it.putExtra("matchID",matchId)
                    it.putExtra("gameID",game.toString())
                    it.putExtra("setID",set.toString())
                    startActivity(it)
                }
                score(app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
            }
            else {
                app.totalpoints1++
                if (findViewById<Button>(R.id.buttonFault).text == "Fault") { //trafiony 1 serwis
                    app.firstservein2++
                }
                else if (findViewById<Button>(R.id.buttonFault).text == "Double Fault") {
                    app.secondservein2++
                    findViewById<Button>(R.id.buttonFault).text = "Fault"
                    findViewById<TextView>(R.id.textViewFS).text = "1st Serve"
                }
                app.returnwinnerFH1++
                val intent=Intent(this,AddActivity::class.java).also{
                    it.putExtra("Pkt1",pkt1.text)
                    it.putExtra("Pkt2",pkt2.text)
                    it.putExtra("Kto",player1.text)
                    it.putExtra("Co","Winner")
                    it.putExtra("Gdzie","Return")
                    it.putExtra("Czym","Forehand")
                    it.putExtra("matchID",matchId)
                    it.putExtra("gameID",game.toString())
                    it.putExtra("setID",set.toString())
                    startActivity(it)
                }
                score(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            }
        }

        findViewById<Button>(R.id.buttonRWB).setOnClickListener {
            if (serve1.text != "") { //serwuje player 1
                app.totalpoints2++
                if (findViewById<Button>(R.id.buttonFault).text == "Fault") { //trafiony 1 serwis
                    app.firstservein1++
                }
                else if (findViewById<Button>(R.id.buttonFault).text == "Double Fault") {
                    app.secondservein1++
                    findViewById<Button>(R.id.buttonFault).text = "Fault"
                    findViewById<TextView>(R.id.textViewFS).text = "1st Serve"
                }
                app.returnwinnerBH2++
                val intent=Intent(this,AddActivity::class.java).also{
                    it.putExtra("Pkt1",pkt1.text)
                    it.putExtra("Pkt2",pkt2.text)
                    it.putExtra("Kto",player2.text)
                    it.putExtra("Co","Winner")
                    it.putExtra("Gdzie","Return")
                    it.putExtra("Czym","Backhand")
                    it.putExtra("matchID",matchId)
                    it.putExtra("gameID",game.toString())
                    it.putExtra("setID",set.toString())
                    startActivity(it)
                }
                score(app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
            }
            else {
                app.totalpoints1++
                if (findViewById<Button>(R.id.buttonFault).text == "Fault") { //trafiony 1 serwis
                    app.firstservein2++
                }
                else if (findViewById<Button>(R.id.buttonFault).text == "Double Fault") {
                    app.secondservein2++
                    findViewById<Button>(R.id.buttonFault).text = "Fault"
                    findViewById<TextView>(R.id.textViewFS).text = "1st Serve"
                }
                app.returnwinnerBH1++
                val intent=Intent(this,AddActivity::class.java).also{
                    it.putExtra("Pkt1",pkt1.text)
                    it.putExtra("Pkt2",pkt2.text)
                    it.putExtra("Kto",player1.text)
                    it.putExtra("Co","Winner")
                    it.putExtra("Gdzie","Return")
                    it.putExtra("Czym","Backhand")
                    it.putExtra("matchID",matchId)
                    it.putExtra("gameID",game.toString())
                    it.putExtra("setID",set.toString())
                    startActivity(it)
                }
                score(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            }
        }

        findViewById<Button>(R.id.buttonREF).setOnClickListener {
            if (serve1.text != "") { //serwuje player 1
                app.totalpoints1++
                if (findViewById<Button>(R.id.buttonFault).text == "Fault") { //trafiony 1 serwis
                    app.firstservein1++
                }
                else if (findViewById<Button>(R.id.buttonFault).text == "Double Fault") {
                    app.secondservein1++
                    findViewById<Button>(R.id.buttonFault).text = "Fault"
                    findViewById<TextView>(R.id.textViewFS).text = "1st Serve"
                }
                app.returnerrorFH2++
                val intent=Intent(this,AddActivity::class.java).also{
                    it.putExtra("Pkt1",pkt1.text)
                    it.putExtra("Pkt2",pkt2.text)
                    it.putExtra("Kto",player2.text)
                    it.putExtra("Co","Winner")
                    it.putExtra("Gdzie","Return")
                    it.putExtra("Czym","Forehand")
                    it.putExtra("matchID",matchId)
                    it.putExtra("gameID",game.toString())
                    it.putExtra("setID",set.toString())
                    startActivity(it)
                }
                score(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            }
            else {
                app.totalpoints2++
                if (findViewById<Button>(R.id.buttonFault).text == "Fault") { //trafiony 1 serwis
                    app.firstservein2++
                }
                else if (findViewById<Button>(R.id.buttonFault).text == "Double Fault") {
                    app.secondservein2++
                    findViewById<Button>(R.id.buttonFault).text = "Fault"
                    findViewById<TextView>(R.id.textViewFS).text = "1st Serve"
                }
                app.returnerrorFH1++
                val intent=Intent(this,AddActivity::class.java).also{
                    it.putExtra("Pkt1",pkt1.text)
                    it.putExtra("Pkt2",pkt2.text)
                    it.putExtra("Kto",player1.text)
                    it.putExtra("Co","Winner")
                    it.putExtra("Gdzie","Return")
                    it.putExtra("Czym","Forehand")
                    it.putExtra("matchID",matchId)
                    it.putExtra("gameID",game.toString())
                    it.putExtra("setID",set.toString())
                    startActivity(it)
                }
                score(app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
            }
        }

        findViewById<Button>(R.id.buttonREB).setOnClickListener {
            if (serve1.text != "") { //serwuje player 1
                app.totalpoints1++
                if (findViewById<Button>(R.id.buttonFault).text == "Fault") { //trafiony 1 serwis
                    app.firstservein1++
                }
                else if (findViewById<Button>(R.id.buttonFault).text == "Double Fault") {
                    app.secondservein1++
                    findViewById<Button>(R.id.buttonFault).text = "Fault"
                    findViewById<TextView>(R.id.textViewFS).text = "1st Serve"
                }
                app.returnerrorBH2++
                val intent=Intent(this,AddActivity::class.java).also{
                    it.putExtra("Pkt1",pkt1.text)
                    it.putExtra("Pkt2",pkt2.text)
                    it.putExtra("Kto",player2.text)
                    it.putExtra("Co","Error")
                    it.putExtra("Gdzie","Return")
                    it.putExtra("Czym","Backhand")
                    it.putExtra("matchID",matchId)
                    it.putExtra("gameID",game.toString())
                    it.putExtra("setID",set.toString())
                    startActivity(it)
                }
                score(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            }
            else {
                app.totalpoints2++
                if (findViewById<Button>(R.id.buttonFault).text == "Fault") { //trafiony 1 serwis
                    app.firstservein2++
                }
                else if (findViewById<Button>(R.id.buttonFault).text == "Double Fault") {
                    app.secondservein2++
                    findViewById<Button>(R.id.buttonFault).text = "Fault"
                    findViewById<TextView>(R.id.textViewFS).text = "1st Serve"
                }
                app.returnerrorBH1++
                val intent=Intent(this,AddActivity::class.java).also{
                    it.putExtra("Pkt1",pkt1.text)
                    it.putExtra("Pkt2",pkt2.text)
                    it.putExtra("Kto",player1.text)
                    it.putExtra("Co","Error")
                    it.putExtra("Gdzie","Return")
                    it.putExtra("Czym","Backhand")
                    it.putExtra("matchID",matchId)
                    it.putExtra("gameID",game.toString())
                    it.putExtra("setID",set.toString())
                    startActivity(it)
                }
                score(app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
            }
        }

        findViewById<Button>(R.id.buttonBIP).setOnClickListener {
            if(serve1.text != ""){
                if (findViewById<Button>(R.id.buttonFault).text == "Fault") { //trafiony 1 serwis
                    app.firstservein1++
                }
                else if (findViewById<Button>(R.id.buttonFault).text == "Double Fault") {
                    app.secondservein1++
                    findViewById<Button>(R.id.buttonFault).text = "Fault"
                    findViewById<TextView>(R.id.textViewFS).text = "1st Serve"
                }
            }
            else {
                //app.totalpoints2++
                if (findViewById<Button>(R.id.buttonFault).text == "Fault") { //trafiony 1 serwis
                    app.firstservein2++
                }
                else if (findViewById<Button>(R.id.buttonFault).text == "Double Fault") {
                    app.secondservein2++
                    findViewById<Button>(R.id.buttonFault).text = "Fault"
                    findViewById<TextView>(R.id.textViewFS).text = "1st Serve"
                }
            }
            val intent=Intent(this,ActivityBallInPlay::class.java).also{
                it.putExtra("Pkt1",pkt1.text)
                it.putExtra("Pkt2",pkt2.text)
                it.putExtra("matchID",matchId)
                it.putExtra("gameID",game.toString())
                it.putExtra("setID",set.toString())
                it.putExtra("DanePlayer1",player1.text)
                it.putExtra("DanePlayer2",player2.text)
            }
            startActivity(intent)
            //callActivity() //zmiana aktywnosci na ActivityBallInPlay
        }
    }
}