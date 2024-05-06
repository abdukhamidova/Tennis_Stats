package com.anw.tenistats

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.anw.tenistats.ui.theme.NavigationDrawerHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class ActivityBallInPlay : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ball_in_play)
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
            startActivity(Intent(this,ActivityStartPoint::class.java))
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

        val app = application as Stats
        val serve1 = findViewById<TextView>(R.id.textViewServe1BIP)
        val serve2 = findViewById<TextView>(R.id.textViewServe2BIP)
        val pkt1 = findViewById<TextView>(R.id.textViewPlayer1PktBIP)
        val set1p1 = findViewById<TextView>(R.id.textViewPlayer1Set1BIP)
        val set2p1 = findViewById<TextView>(R.id.textViewPlayer1Set2BIP)
        val set3p1 = findViewById<TextView>(R.id.textViewPlayer1Set3BIP)
        val pkt2 = findViewById<TextView>(R.id.textViewPlayer2PktBIP)
        val set1p2 = findViewById<TextView>(R.id.textViewPlayer2Set1BIP)
        val set2p2 = findViewById<TextView>(R.id.textViewPlayer2Set2BIP)
        val set3p2 = findViewById<TextView>(R.id.textViewPlayer2Set3BIP)

        val player1 = findViewById<TextView>(R.id.textviewPlayer1)
        val player2 = findViewById<TextView>(R.id.textviewPlayer2)

        fillUpScoreInActivity(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2) //ustawienie poczatkowe wyniku

        findViewById<TextView>(R.id.textPL1).text = player1.text
        findViewById<TextView>(R.id.textPL2).text = player2.text

        findViewById<Button>(R.id.buttonWinner1).setOnClickListener {
            Intent(this,DetailsActivity::class.java).also{
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
            Intent(this,DetailsActivity::class.java).also{
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
            Intent(this,DetailsActivity::class.java).also{
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
            Intent(this,DetailsActivity::class.java).also{
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
            Intent(this,DetailsActivity::class.java).also{
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
            Intent(this,DetailsActivity::class.java).also{
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