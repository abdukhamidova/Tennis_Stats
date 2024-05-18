package com.anw.tenistats

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.anw.tenistats.com.anw.tenistats.AddPointDialog
import com.anw.tenistats.ui.theme.NavigationDrawerHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DetailsActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var database: DatabaseReference
    var matchId: String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //MENU
        firebaseAuth = FirebaseAuth.getInstance()
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.navigationViewMenu)
        val menu = findViewById<ImageButton>(R.id.buttonMenu)
        val headerView = navigationView.getHeaderView(0)
        menu.setOnClickListener{
            drawerLayout.open()
        }
        navigationDrawerHelper = NavigationDrawerHelper(this)
        navigationDrawerHelper.setupNavigationDrawer(drawerLayout, navigationView, firebaseAuth)
        val backButton = findViewById<ImageButton>(R.id.buttonUndo)
        backButton.setOnClickListener{
            Toast.makeText(this,"Point cleared", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this,ActivityStartPoint::class.java))
            finish()
        }

        val userEmail = firebaseAuth.currentUser?.email.toString()
        val userEmailView = headerView.findViewById<TextView>(R.id.textViewUserEmail)
        if(userEmail.isNotEmpty()) {
            userEmailView.text = userEmail
        }else {
            userEmailView.text = resources.getString(R.string.user_email)
        }
        //MENU

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

        val player1 = findViewById<TextView>(R.id.textviewPlayer1Details)
        val player2 = findViewById<TextView>(R.id.textviewPlayer2Details)

        val user = firebaseAuth.currentUser?.uid

        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(user.toString())
        database.child("Current match").get().addOnSuccessListener {dataSnapshot ->
            matchId = dataSnapshot.getValue(String::class.java)
            database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(user.toString()).child("Matches").child(matchId.toString())

            //ustawienie wyniku w tabeli
            setscore(player1,player2,serve1,serve2,set1p1,set2p1,set3p1,set1p2,set2p2,set3p2,pkt1,pkt2)
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to read Current Match ID", Toast.LENGTH_SHORT).show()
        }

        fillUpScoreInActivity(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)

        //spr kto serwuje
        var czyPlayer1 = false
        if(player1.text==intent.getStringExtra("Kto")){
            czyPlayer1 = true
        }

        findViewById<Button>(R.id.buttonGround).setOnClickListener {
            if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
            {
                if(intent.getStringExtra("Co")=="Winner") {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Ground",
                            "Forehand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
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
                }else {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Ground",
                            "Forehand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
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
                }
            }
            else{
                if(intent.getStringExtra("Co")=="Winner") {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Ground",
                            "Backhand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
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
                }else {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Ground",
                            "Backhand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
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
                }
            }
        }

        findViewById<Button>(R.id.buttonVolley).setOnClickListener {
            if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
            {
                if(intent.getStringExtra("Co")=="Winner") {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Volley",
                            "Forehand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
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
                }else {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Volley",
                            "Forehand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
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
                }
            }
            else{
                if(intent.getStringExtra("Co")=="Winner") {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Volley",
                            "Backhand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
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
                }else {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Volley",
                            "Backhand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
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
                }
            }
        }

        findViewById<Button>(R.id.buttonLob).setOnClickListener {
            if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
            {
                if(intent.getStringExtra("Co")=="Winner") {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Lob",
                            "Forehand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
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
                }else {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Lob",
                            "Forehand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
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
                }
            }
            else{
                if(intent.getStringExtra("Co")=="Winner") {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Lob",
                            "Backhand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
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
                }else {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Lob",
                            "Backhand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
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
                }
            }
        }

        findViewById<Button>(R.id.buttonSlice).setOnClickListener {
            if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
            {
                if(intent.getStringExtra("Co")=="Winner") {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Slice",
                            "Forehand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
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
                }else {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Slice",
                            "Forehand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
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
                }
            }
            else{
                if(intent.getStringExtra("Co")=="Winner") {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Slice",
                            "Backhand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
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
                }else {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Slice",
                            "Backhand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
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
                }
            }
        }

        findViewById<Button>(R.id.buttonSmash).setOnClickListener {
            if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
            {
                if(intent.getStringExtra("Co")=="Winner") {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Smash",
                            "Forehand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
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
                }else {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Smash",
                            "Forehand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
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
                }
            }
            else{
                if(intent.getStringExtra("Co")=="Winner") {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Smash",
                            "Backhand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
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
                }else {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Smash",
                            "Backhand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
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
                }
            }
        }

        findViewById<Button>(R.id.buttonDropshot).setOnClickListener {
            if(findViewById<RadioButton>(R.id.radioButtonFH).isChecked)
            {
                if(intent.getStringExtra("Co")=="Winner") {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Dropshot",
                            "Forehand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
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
                }else {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Dropshot",
                            "Forehand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
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
                }
            }
            else{
                if(intent.getStringExtra("Co")=="Winner") {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Dropshot",
                            "Backhand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
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
                }else {
                    if(czyPlayer1) {
                        addPointDialog.show(
                            player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
                            intent.getStringExtra("Pkt1").toString(),
                            intent.getStringExtra("Pkt2").toString(),
                            intent.getStringExtra("Kto").toString(),
                            intent.getStringExtra("Co").toString(),
                            "Dropshot",
                            "Backhand",
                            intent.getStringExtra("matchID").toString(),
                            intent.getStringExtra("gameID").toString(),
                            intent.getStringExtra("setID").toString())
                    }else{
                        addPointDialog.show(
                            player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
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
                }
            }
        }
    }
    fun setscore(player1: TextView,player2: TextView,serve1: TextView,serve2: TextView,set1p1: TextView,set2p1: TextView,set3p1: TextView,set1p2: TextView,set2p2: TextView,set3p2: TextView,pkt1:TextView,pkt2:TextView)
    {
        database.child("player1").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val player1Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            player1.text = player1Value.toString()
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }

        database.child("player2").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val player2Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            player2.text = player2Value.toString()
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }

        database.child("set1p1").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val set1p1Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            set1p1.text = set1p1Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("set2p1").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val set2p1Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            set2p1.text = set2p1Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("set3p1").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val set3p1Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            set3p1.text = set3p1Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("set1p2").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val set1p2Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            set1p2.text = set1p2Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("set2p2").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val set2p2Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            set2p2.text = set2p2Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("set3p2").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val set3p2Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            set3p2.text = set3p2Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("pkt1").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val pkt1Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            pkt1.text = pkt1Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("pkt2").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val pkt2Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            pkt2.text = pkt2Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("winner").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            if(dataSnapshot.exists()){
                // Pobranie wartości "player1" z bazy danych
                val winner = dataSnapshot.getValue(String::class.java)
                val goldenLaurel = getGoldenDrawable(applicationContext, R.drawable.icon_laurel3)
                serve1.setCompoundDrawablesWithIntrinsicBounds(goldenLaurel, null, null, null)
                serve2.setCompoundDrawablesWithIntrinsicBounds(goldenLaurel, null, null, null)
                // Ustawienie wartości w TextView
                if(winner==player1.text){
                    serve1.visibility = View.VISIBLE
                    serve2.visibility = View.INVISIBLE
                }
                else{
                    serve1.visibility = View.INVISIBLE
                    serve2.visibility = View.VISIBLE
                }
            }
            else{
                database.child("LastServePlayer").get().addOnSuccessListener { dataSnapshot ->
                    // Pobranie wartości "player1" z bazy danych
                    val lastserve = dataSnapshot.getValue(String::class.java)
                    serve1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ball, 0, 0, 0)
                    serve2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ball, 0, 0, 0)
                    // Ustawienie wartości w TextView
                    if(lastserve==player1.text){
                        serve1.visibility = View.VISIBLE
                        serve2.visibility = View.INVISIBLE
                    }
                    else{
                        serve1.visibility = View.INVISIBLE
                        serve2.visibility = View.VISIBLE
                    }
                }.addOnFailureListener { exception ->
                    // Obsługa błędów
                }
            }
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
    }
}