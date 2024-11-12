package com.anw.tenistats.matchplay

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.anw.tenistats.R
import com.anw.tenistats.stats.StatsClass
import com.anw.tenistats.dialog.AddPointDialog
import com.anw.tenistats.databinding.ActivityDetailsBinding
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class BallInPlayDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var database: DatabaseReference
    var matchId: String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
            startActivity(Intent(this, StartPointActivity::class.java))
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
        binding.textPlayerName.apply {
            text = intent.getStringExtra("Kto")
        }
        binding.textShot.apply {
            text = intent.getStringExtra("Co")
        }

        val app = application as StatsClass
        val serve1 = binding.textViewServe1Details
        val serve2 = binding.textViewServe2Details
        val pkt1 = binding.textviewPlayer1PktDetails
        val set1p1 = binding.textviewPlayer1Set1Details
        val set2p1 = binding.textviewPlayer1Set2Details
        val set3p1 = binding.textviewPlayer1Set3Details
        val pkt2 = binding.textviewPlayer2PktDetails
        val set1p2 = binding.textviewPlayer2Set1Details
        val set2p2 = binding.textviewPlayer2Set2Details
        val set3p2 = binding.textviewPlayer2Set3Details
        val addPointDialog = AddPointDialog(this,false)

        val player1 = binding.textviewPlayer1Details
        val player2 = binding.textviewPlayer2Details

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

        //spr kto gra
        var czyPlayer1 = false
        if(player1.text==intent.getStringExtra("Kto")){
            czyPlayer1 = true
        }

        binding.buttonGround.setOnClickListener {
            if(binding.radioButtonFH.isChecked)
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

        binding.buttonVolley.setOnClickListener {
            if(binding.radioButtonFH.isChecked)
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

        binding.buttonLob.setOnClickListener {
            if(binding.radioButtonFH.isChecked)
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

        binding.buttonSlice.setOnClickListener {
            if(binding.radioButtonFH.isChecked)
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

        binding.buttonSmash.setOnClickListener {
            if(binding.radioButtonFH.isChecked)
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

        binding.buttonDropshot.setOnClickListener {
            if(binding.radioButtonFH.isChecked)
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
    private fun setscore(
        player1: TextView, player2: TextView, serve1: TextView, serve2: TextView,
        set1p1: TextView, set2p1: TextView, set3p1: TextView, set1p2: TextView,
        set2p2: TextView, set3p2: TextView, pkt1: TextView, pkt2: TextView
    ) {
        val app = application as StatsClass

        val fields = listOf(
            "player1" to player1,
            "player2" to player2,
            "set1p1" to set1p1, "set2p1" to set2p1, "set3p1" to set3p1,
            "set1p2" to set1p2, "set2p2" to set2p2, "set3p2" to set3p2,
            "pkt1" to pkt1, "pkt2" to pkt2
        )

        fields.forEach { (key, textView) ->
            database.child(key).get().addOnSuccessListener { dataSnapshot ->
                val value = dataSnapshot.getValue(String::class.java) ?: ""
                textView.text = value
                app.javaClass.getDeclaredField(key).apply {
                    isAccessible = true
                    set(app, value)
                }
            }.addOnFailureListener {
                // Handle failure
            }
        }
    }
}