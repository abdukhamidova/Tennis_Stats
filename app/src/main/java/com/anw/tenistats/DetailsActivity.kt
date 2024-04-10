package com.anw.tenistats

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DetailsActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var czyPlayer1: Boolean = false

        findViewById<TextView>(R.id.textPlayerName).apply {
            text = intent.getStringExtra("Text name")
        }
        findViewById<TextView>(R.id.textShot).apply {
            text = intent.getStringExtra("Text shot")
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

        fillUpScoreInActivity(app,findViewById<TextView>(R.id.textviewPlayer1Details),findViewById<TextView>(R.id.textviewPlayer2Details),serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)

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

        val player1 = findViewById<TextView>(R.id.textviewPlayer1Details)
        val player2 = findViewById<TextView>(R.id.textviewPlayer2Details)
        if(player1.text==intent.getStringExtra("Text name")){
            czyPlayer1 = true
        }

        findViewById<Button>(R.id.buttonMenuDetails).setOnClickListener {
            startActivity(Intent(this,ActivityMenu::class.java))
        }
        //link do poprzedniego activity (BallInPlay)
        findViewById<ImageButton>(R.id.imageButtonBack).setOnClickListener {
            val player1=findViewById<TextView>(R.id.textviewPlayer1).text.toString()
            val player2=findViewById<TextView>(R.id.textviewPlayer2).text.toString()

            val intent=Intent(this,ActivityBallInPlay::class.java).also{
                it.putExtra("DanePlayer1",player1)
                it.putExtra("DanePlayer2",player2)
                startActivity(it)
            }
        }
        findViewById<Button>(R.id.buttonGround).setOnClickListener {
            historia.gdzie.add("Ground")
            database.child("gdzie").push().setValue("Ground")
            when (findViewById<TextView>(R.id.textShot).text)
            {
                "Winner" -> {
                    if(czyPlayer1){
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.winnergroundFH1++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.winnergroundBH1++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                    else
                    {
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.winnergroundFH2++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.winnergroundBH2++

                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                }
                "Forced Error" -> {
                    if(czyPlayer1){
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.forcederrorgroundFH1++
                        }
                        else{
                            app.forcederrorgroundBH1++
                            database.child("czym").push().setValue("BH")
                            historia.czym.add("BH")
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.forcederrorgroundFH2++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.forcederrorgroundBH2++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                }
                "Unforced Error" -> {
                    if(czyPlayer1){
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.unforcederrorgroundFH1++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.unforcederrorgroundBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.unforcederrorgroundFH2++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.unforcederrorgroundBH2++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                }
            }
            startActivity(Intent(this,ActivityStartPoint::class.java))
        }

        findViewById<Button>(R.id.buttonVolley).setOnClickListener {
            historia.gdzie.add("Volley")
            database.child("gdzie").push().setValue("Volley")
            when (findViewById<TextView>(R.id.textShot).text)
            {
                "Winner" -> {
                    if(czyPlayer1){
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.winnervolleyFH1++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.winnervolleyBH1++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                    else
                    {
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.winnervolleyFH2++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.winnervolleyBH2++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                }
                "Forced Error" -> {
                    if(czyPlayer1){
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.forcederrorvolleyFH1++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.forcederrorvolleyBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.forcederrorvolleyFH2++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.forcederrorvolleyBH2++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                }
                "Unforced Error" -> {
                    if(czyPlayer1){
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.unforcederrorvolleyFH1++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.unforcederrorvolleyBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.unforcederrorvolleyFH2++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.unforcederrorvolleyBH2++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                }
            }
            startActivity(Intent(this,ActivityStartPoint::class.java))
        }

        findViewById<Button>(R.id.buttonLob).setOnClickListener {
            historia.gdzie.add("Lob")
            database.child("gdzie").push().setValue("Lob")
            when (findViewById<TextView>(R.id.textShot).text)
            {
                "Winner" -> {
                    if(czyPlayer1){
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.winnerlobFH1++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.winnerlobBH1++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                    else
                    {
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.winnerlobFH2++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.winnerlobBH2++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                }
                "Forced Error" -> {
                    if(czyPlayer1){
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.forcederrorlobFH1++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.forcederrorlobBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.forcederrorlobFH2++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.forcederrorlobBH2++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                }
                "Unforced Error" -> {
                    if(czyPlayer1){
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.unforcederrorlobFH1++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.unforcederrorlobBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.unforcederrorlobFH2++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.unforcederrorlobBH2++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                }
            }
            startActivity(Intent(this,ActivityStartPoint::class.java))
        }

        findViewById<Button>(R.id.buttonSlice).setOnClickListener {
            historia.gdzie.add("Slice")
            database.child("gdzie").push().setValue("Slice")
            when (findViewById<TextView>(R.id.textShot).text)
            {
                "Winner" -> {
                    if(czyPlayer1){
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.winnersliceFH1++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.winnersliceBH1++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                    else
                    {
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.winnersliceFH2++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.winnersliceBH2++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                }
                "Forced Error" -> {
                    if(czyPlayer1){
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.forcederrorsliceFH1++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.forcederrorsliceBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.forcederrorsliceFH2++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.forcederrorsliceBH2++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                }
                "Unforced Error" -> {
                    if(czyPlayer1){
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.unforcederrorsliceFH1++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.unforcederrorsliceBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.unforcederrorsliceFH2++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.unforcederrorsliceBH2++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                }
            }
            startActivity(Intent(this,ActivityStartPoint::class.java))
        }

        findViewById<Button>(R.id.buttonSmash).setOnClickListener {
            historia.gdzie.add("Smash")
            database.child("gdzie").push().setValue("Smash")
            when (findViewById<TextView>(R.id.textShot).text)
            {
                "Winner" -> {
                    if(czyPlayer1){
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.winnersmashFH1++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.winnersmashBH1++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                    else
                    {
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.winnersmashFH2++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.winnersmashBH2++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                }
                "Forced Error" -> {
                    if(czyPlayer1){
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.forcederrorsmashFH1++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.forcederrorsmashBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.forcederrorsmashFH2++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.forcederrorsmashBH2++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                }
                "Unforced Error" -> {
                    if(czyPlayer1){
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.unforcederrorsmashFH1++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.unforcederrorsmashBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.unforcederrorsmashFH2++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.unforcederrorsmashBH2++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                }
            }
            startActivity(Intent(this,ActivityStartPoint::class.java))
        }

        findViewById<Button>(R.id.buttonDropshot).setOnClickListener {
            historia.gdzie.add("Dropshot")
            database.child("gdzie").push().setValue("Dropshot")
            when (findViewById<TextView>(R.id.textShot).text)
            {
                "Winner" -> {
                    if(czyPlayer1){
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.winnerdropshotFH1++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.winnerdropshotBH1++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                    else
                    {
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.winnerdropshotFH2++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.winnerdropshotBH2++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                }
                "Forced Error" -> {
                    if(czyPlayer1){
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.forcederrordropshotFH1++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.forcederrordropshotBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.forcederrordropshotFH2++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.forcederrordropshotBH2++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                }
                "Unforced Error" -> {
                    if(czyPlayer1){
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.unforcederrordropshotFH1++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.unforcederrordropshotBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            historia.czym.add("FH")
                            database.child("czym").push().setValue("FH")
                            app.unforcederrordropshotFH2++
                        }
                        else{
                            historia.czym.add("BH")
                            database.child("czym").push().setValue("BH")
                            app.unforcederrordropshotBH2++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                }
            }
            startActivity(Intent(this,ActivityStartPoint::class.java))
        }
    }
}