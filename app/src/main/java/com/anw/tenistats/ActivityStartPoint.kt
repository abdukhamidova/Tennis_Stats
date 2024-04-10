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
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start_point)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val app = application as Stats
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
        //link do menu
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

        findViewById<Button>(R.id.buttonMenuSP).setOnClickListener {
            startActivity(Intent(this,ActivityMenu::class.java))
        }
        //link do poprzedniego activity (Serve)
        findViewById<ImageButton>(R.id.imageButtonBack).setOnClickListener {
            val player1=findViewById<TextView>(R.id.textviewPlayer1).text.toString()
            val player2=findViewById<TextView>(R.id.textviewPlayer2).text.toString()

            val intent=Intent(this,ActivityServe::class.java).also{
                it.putExtra("DanePlayer1",player1)
                it.putExtra("DanePlayer2",player2)
                startActivity(it)
            }
        }

        findViewById<Button>(R.id.buttonAce).setOnClickListener {
            setScoreInDatabse(historia, match,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,pkt1,pkt2)
            historia.kto.add(player1.text.toString()) //player1 serwowal
            historia.co.add("Ace") //dodaje do listy history.co
            historia.gdzie.add("") //byl ace, wiec nie ma 'gdzie'
            historia.czym.add("") //byl ace, wiec nie ma 'czym'
            database.setValue(match)
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
                score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
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
                score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
            }
        }

        findViewById<Button>(R.id.buttonFault).setOnClickListener {
            if (findViewById<Button>(R.id.buttonFault).text == "Fault") {
                historia.serwis.set(historia.serwis.size - 1,"2") //ustawiam historia.serwis na "2" (drugi serwis)
                findViewById<Button>(R.id.buttonFault).text = "Double Fault"
                findViewById<Button>(R.id.buttonFault).textSize = 15.4f
                findViewById<TextView>(R.id.textViewFS).text = "2nd Serve"
            }
            else if (findViewById<Button>(R.id.buttonFault).text == "Double Fault") {
                setScoreInDatabse(historia, match,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,pkt1,pkt2)
                historia.serwis.set((historia.serwis).size - 1,"0") //ustawiam historia.serwis na "0" (podwojny blad)
                historia.co.add("Double Fault") //podwojny blad
                historia.gdzie.add("") //nie ma, bo byl podwojny blad
                historia.czym.add("") //nie ma, bo byl podwojny blad
                findViewById<Button>(R.id.buttonFault).text = "Fault"
                findViewById<Button>(R.id.buttonFault).textSize = 19.9f
                findViewById<TextView>(R.id.textViewFS).text = "1st Serve"
                if (serve1.text != "") {
                    historia.kto.add(player1.text.toString())
                    app.totalpoints2++
                    app.doublefault1++
                    score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                }
                else {
                    historia.kto.add(player2.text.toString())
                    app.totalpoints1++
                    app.doublefault2++
                    score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                }
            }
            database.setValue(match)
        }

        findViewById<Button>(R.id.buttonRWF).setOnClickListener{
            setScoreInDatabse(historia, match,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,pkt1,pkt2)
            historia.co.add("Winner")
            historia.gdzie.add("Return")
            historia.czym.add("FH")
            if (serve1.text != "") { //serwuje player 1
                historia.kto.add(player2.text.toString()) //return winner zagral player2
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
                score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
            }
            else {
                historia.kto.add(player1.text.toString()) //return winner zagral player1
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
                score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            }
            database.setValue(match)
        }

        findViewById<Button>(R.id.buttonRWB).setOnClickListener {
            setScoreInDatabse(historia, match,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,pkt1,pkt2)
            historia.co.add("Winner")
            historia.gdzie.add("Return")
            historia.czym.add("BH")
            if (serve1.text != "") { //serwuje player 1
                historia.kto.add(player2.text.toString()) //return winner zagral player2
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
                score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
            }
            else {
                historia.kto.add(player1.text.toString()) //return winner zagral player1
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
                score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            }
            database.setValue(match)
        }

        findViewById<Button>(R.id.buttonREF).setOnClickListener {
            setScoreInDatabse(historia, match,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,pkt1,pkt2)
            historia.co.add("Error")
            historia.gdzie.add("Return")
            historia.czym.add("FH")
            if (serve1.text != "") { //serwuje player 1
                historia.kto.add(player2.text.toString()) //return winner zagral player2
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
                score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            }
            else {
                historia.kto.add(player1.text.toString()) //return winner zagral player1
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
                score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
            }
            database.setValue(match)
        }

        findViewById<Button>(R.id.buttonREB).setOnClickListener {
            setScoreInDatabse(historia, match,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,pkt1,pkt2)
            historia.co.add("Error")
            historia.gdzie.add("Return")
            historia.czym.add("BH")
            if (serve1.text != "") { //serwuje player 1
                historia.kto.add(player2.text.toString()) //return winner zagral player2
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
                score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            }
            else {
                historia.kto.add(player1.text.toString()) //return winner zagral player2
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
                score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
            }
            database.setValue(match)
        }

        findViewById<Button>(R.id.buttonBIP).setOnClickListener {
            setScoreInDatabse(historia, match,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,pkt1,pkt2)
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
            callActivity() //zmiana aktywnosci na ActivityBallInPlay
        }
    }

    fun callActivity() {
        val player1=findViewById<TextView>(R.id.textviewPlayer1).text.toString()
        val player2=findViewById<TextView>(R.id.textviewPlayer2).text.toString()

        val intent=Intent(this,ActivityBallInPlay::class.java).also{
            it.putExtra("DanePlayer1",player1)
            it.putExtra("DanePlayer2",player2)
            startActivity(it)
        }
    }

    fun setScoreInDatabse(historia: Row,match: Match,set1p1: TextView,set1p2: TextView,set2p1: TextView,set2p2: TextView,set3p1: TextView,set3p2: TextView,pkt1: TextView,pkt2: TextView)
    {
        historia.set1.add(set1p1.text.toString() + ":" + set1p2.text.toString())
        if(!set2p1.text.toString().isNullOrEmpty()) {
            historia.set2.add(
                set2p1.text.toString() + ":" + set2p2.text?.toString()
            )
            if (!set3p1.text.toString().isNullOrEmpty()) {
                historia.set3.add(
                    set3p1.text?.toString() + ":" + set3p2.text?.toString()
                )
            }
            else{
                historia.set3.add("0:0")
            }
        }
        else{
            historia.set2.add("0:0")
            historia.set3.add("0:0")
        }
        historia.wynik.add(pkt1.text.toString() + ":" + pkt2.text.toString()) //dodaje do listy history.wynik aktualny wynik w gemie
        historia.serwis.add("1") //domyslnie ustawiamy, ze weszedl 1 serwis
        database.setValue(match)
    }
}