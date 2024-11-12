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
import com.anw.tenistats.databinding.ActivityBallInPlayBinding
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class BallInPlayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBallInPlayBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var database: DatabaseReference
    var matchId: String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBallInPlayBinding.inflate(layoutInflater)
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
        navigationDrawerHelper.setupNavigationDrawer(drawerLayout, navigationView, firebaseAuth, true)
        val backButton = findViewById<ImageButton>(R.id.buttonUndo)
        backButton.setOnClickListener{
            Toast.makeText(this,"Point cleared", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, StartPointActivity::class.java))
            finish()
        }

        val userEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        val userEmailView = headerView.findViewById<TextView>(R.id.textViewUserEmail)
        if(userEmail.isNotEmpty()) {
            userEmailView.text = userEmail
        }else {
            userEmailView.text = resources.getString(R.string.user_email)
        }
        //MENU

        val app = application as StatsClass
        val serve1 = binding.textViewServe1BIP
        val serve2 = binding.textViewServe2BIP
        val pkt1 = binding.textViewPlayer1PktBIP
        val set1p1 = binding.textViewPlayer1Set1BIP
        val set2p1 = binding.textViewPlayer1Set2BIP
        val set3p1 = binding.textViewPlayer1Set3BIP
        val pkt2 = binding.textViewPlayer2PktBIP
        val set1p2 = binding.textViewPlayer2Set1BIP
        val set2p2 = binding.textViewPlayer2Set2BIP
        val set3p2 = binding.textViewPlayer2Set3BIP

        val player1 = binding.textviewPlayer1
        val player2 = binding.textviewPlayer2

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

        //findViewById<TextView>(R.id.textPL1).text = player1.text
        //findViewById<TextView>(R.id.textPL2).text = player2.text

        fillUpScoreInActivity(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2) //ustawienie poczatkowe wyniku

        binding.buttonWinner1.setOnClickListener {
            Intent(this, BallInPlayDetailsActivity::class.java).also{
                it.putExtra("Pkt1",intent.getStringExtra("Pkt1"))
                it.putExtra("Pkt2",intent.getStringExtra("Pkt2"))
                it.putExtra("matchID",intent.getStringExtra("matchID"))
                it.putExtra("gameID",intent.getStringExtra("gameID"))
                it.putExtra("setID",intent.getStringExtra("setID"))
                it.putExtra("Kto",player1.text)
                it.putExtra("Co","Winner")
                startActivity(it)
                finish()
            }
        }

        binding.buttonWinner2.setOnClickListener {
            Intent(this, BallInPlayDetailsActivity::class.java).also{
                it.putExtra("Pkt1",intent.getStringExtra("Pkt1"))
                it.putExtra("Pkt2",intent.getStringExtra("Pkt2"))
                it.putExtra("matchID",intent.getStringExtra("matchID"))
                it.putExtra("gameID",intent.getStringExtra("gameID"))
                it.putExtra("setID",intent.getStringExtra("setID"))
                it.putExtra("Kto",player2.text)
                it.putExtra("Co","Winner")
                startActivity(it)
                finish()
            }
        }

        binding.buttonForcedError1.setOnClickListener {
            Intent(this, BallInPlayDetailsActivity::class.java).also{
                it.putExtra("Pkt1",intent.getStringExtra("Pkt1"))
                it.putExtra("Pkt2",intent.getStringExtra("Pkt2"))
                it.putExtra("matchID",intent.getStringExtra("matchID"))
                it.putExtra("gameID",intent.getStringExtra("gameID"))
                it.putExtra("setID",intent.getStringExtra("setID"))
                it.putExtra("Kto",player1.text)
                it.putExtra("Co","Forced Error")
                startActivity(it)
                finish()
            }
        }

        binding.buttonForcedError2.setOnClickListener {
            Intent(this, BallInPlayDetailsActivity::class.java).also{
                it.putExtra("Pkt1",intent.getStringExtra("Pkt1"))
                it.putExtra("Pkt2",intent.getStringExtra("Pkt2"))
                it.putExtra("matchID",intent.getStringExtra("matchID"))
                it.putExtra("gameID",intent.getStringExtra("gameID"))
                it.putExtra("setID",intent.getStringExtra("setID"))
                it.putExtra("Kto",player2.text)
                it.putExtra("Co","Forced Error")
                startActivity(it)
                finish()
            }
        }

        binding.buttonUnforcedError1.setOnClickListener {
            Intent(this, BallInPlayDetailsActivity::class.java).also{
                it.putExtra("Pkt1",intent.getStringExtra("Pkt1"))
                it.putExtra("Pkt2",intent.getStringExtra("Pkt2"))
                it.putExtra("matchID",intent.getStringExtra("matchID"))
                it.putExtra("gameID",intent.getStringExtra("gameID"))
                it.putExtra("setID",intent.getStringExtra("setID"))
                it.putExtra("Kto",player1.text)
                it.putExtra("Co","Unforced Error")
                startActivity(it)
                finish()
            }
        }

        binding.buttonUnforcedError2.setOnClickListener {
            Intent(this, BallInPlayDetailsActivity::class.java).also{
                it.putExtra("Pkt1",intent.getStringExtra("Pkt1"))
                it.putExtra("Pkt2",intent.getStringExtra("Pkt2"))
                it.putExtra("matchID",intent.getStringExtra("matchID"))
                it.putExtra("gameID",intent.getStringExtra("gameID"))
                it.putExtra("setID",intent.getStringExtra("setID"))
                it.putExtra("Kto",player2.text)
                it.putExtra("Co","Unforced Error")
                startActivity(it)
                finish()
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

        binding.textPL1.text = player1.text
        binding.textPL2.text = player2.text
    }
}