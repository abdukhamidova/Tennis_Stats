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

        val player1 = findViewById<TextView>(R.id.textviewPlayer1Details)
        val player2 = findViewById<TextView>(R.id.textviewPlayer2Details)
        if(player1.text==intent.getStringExtra("Text name")){
            czyPlayer1 = true
        }

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
            when (findViewById<TextView>(R.id.textShot).text)
            {
                "Winner" -> {
                    if(czyPlayer1){
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            app.winnergroundFH1++
                        }
                        else{
                            app.winnergroundBH1++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                    else
                    {
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            app.winnergroundFH2++
                        }
                        else{
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
                            app.forcederrorgroundFH1++
                        }
                        else{
                            app.forcederrorgroundBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            app.forcederrorgroundFH2++
                        }
                        else{
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
                            app.unforcederrorgroundFH1++
                        }
                        else{
                            app.unforcederrorgroundBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            app.unforcederrorgroundFH2++
                        }
                        else{
                            app.unforcederrorgroundBH2++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                }
            }
            startActivity(Intent(this,ActivityStartPoint::class.java))
        }

        findViewById<Button>(R.id.buttonVolley).setOnClickListener {
            when (findViewById<TextView>(R.id.textShot).text)
            {
                "Winner" -> {
                    if(czyPlayer1){
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            app.winnervolleyFH1++
                        }
                        else{
                            app.winnervolleyBH1++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                    else
                    {
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            app.winnervolleyFH2++
                        }
                        else{
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
                            app.forcederrorvolleyFH1++
                        }
                        else{
                            app.forcederrorvolleyBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            app.forcederrorvolleyFH2++
                        }
                        else{
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
                            app.unforcederrorvolleyFH1++
                        }
                        else{
                            app.unforcederrorvolleyBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            app.unforcederrorvolleyFH2++
                        }
                        else{
                            app.unforcederrorvolleyBH2++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                }
            }
            startActivity(Intent(this,ActivityStartPoint::class.java))
        }

        findViewById<Button>(R.id.buttonLob).setOnClickListener {
            when (findViewById<TextView>(R.id.textShot).text)
            {
                "Winner" -> {
                    if(czyPlayer1){
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            app.winnerlobFH1++
                        }
                        else{
                            app.winnerlobBH1++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                    else
                    {
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            app.winnerlobFH2++
                        }
                        else{
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
                            app.forcederrorlobFH1++
                        }
                        else{
                            app.forcederrorlobBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            app.forcederrorlobFH2++
                        }
                        else{
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
                            app.unforcederrorlobFH1++
                        }
                        else{
                            app.unforcederrorlobBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            app.unforcederrorlobFH2++
                        }
                        else{
                            app.unforcederrorlobBH2++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                }
            }
            startActivity(Intent(this,ActivityStartPoint::class.java))
        }

        findViewById<Button>(R.id.buttonSlice).setOnClickListener {
            when (findViewById<TextView>(R.id.textShot).text)
            {
                "Winner" -> {
                    if(czyPlayer1){
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            app.winnersliceFH1++
                        }
                        else{
                            app.winnersliceBH1++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                    else
                    {
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            app.winnersliceFH2++
                        }
                        else{
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
                            app.forcederrorsliceFH1++
                        }
                        else{
                            app.forcederrorsliceBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            app.forcederrorsliceFH2++
                        }
                        else{
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
                            app.unforcederrorsliceFH1++
                        }
                        else{
                            app.unforcederrorsliceBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            app.unforcederrorsliceFH2++
                        }
                        else{
                            app.unforcederrorsliceBH2++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                }
            }
            startActivity(Intent(this,ActivityStartPoint::class.java))
        }

        findViewById<Button>(R.id.buttonSmash).setOnClickListener {
            when (findViewById<TextView>(R.id.textShot).text)
            {
                "Winner" -> {
                    if(czyPlayer1){
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            app.winnersmashFH1++
                        }
                        else{
                            app.winnersmashBH1++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                    else
                    {
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            app.winnersmashFH2++
                        }
                        else{
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
                            app.forcederrorsmashFH1++
                        }
                        else{
                            app.forcederrorsmashBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            app.forcederrorsmashFH2++
                        }
                        else{
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
                            app.unforcederrorsmashFH1++
                        }
                        else{
                            app.unforcederrorsmashBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            app.unforcederrorsmashFH2++
                        }
                        else{
                            app.unforcederrorsmashBH2++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                }
            }
            startActivity(Intent(this,ActivityStartPoint::class.java))
        }

        findViewById<Button>(R.id.buttonDropshot).setOnClickListener {
            when (findViewById<TextView>(R.id.textShot).text)
            {
                "Winner" -> {
                    if(czyPlayer1){
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
                        {
                            app.winnerdropshotFH1++
                        }
                        else{
                            app.winnerdropshotBH1++
                        }
                        score(this,app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                    }
                    else
                    {
                        app.totalpoints2++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            app.winnerdropshotFH2++
                        }
                        else{
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
                            app.forcederrordropshotFH1++
                        }
                        else{
                            app.forcederrordropshotBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            app.forcederrordropshotFH2++
                        }
                        else{
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
                            app.unforcederrordropshotFH1++
                        }
                        else{
                            app.unforcederrordropshotBH1++
                        }
                        score(this,app,player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1)
                    }
                    else
                    {
                        app.totalpoints1++
                        if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked){
                            app.unforcederrordropshotFH2++
                        }
                        else{
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