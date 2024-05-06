package com.anw.tenistats

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.anw.tenistats.ui.theme.NavigationDrawerHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ActivityServe : AppCompatActivity() {
    private var matchId: String?=null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var database: DatabaseReference
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_serve)

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
            startActivity(Intent(this,StartNewActivity::class.java))
        }

        val userEmail = firebaseAuth.currentUser?.email.toString()
        val userEmailView = headerView.findViewById<TextView>(R.id.textViewUserEmail)
        if(userEmail.isNotEmpty()) {
            userEmailView.text = userEmail
        }else {
            userEmailView.text = resources.getString(R.string.user_email)
        }
        //MENU

        val app = application as Stats
        matchId = intent.getStringExtra("matchID")
        val btnPlayer1 = findViewById<Button>(R.id.buttonPlayer1)
        val btnPlayer2 = findViewById<Button>(R.id.buttonPlayer2)
        //pobranie nazw graczy z activity, skąd nastąpiło przekierowanie
        //& przypisanie do tekstu na buttonach
        val player1 = intent.getStringExtra("DanePlayer1")
        btnPlayer1.apply {
            text=player1
        }
        val player2 = intent.getStringExtra("DanePlayer2")
        btnPlayer2.apply {
            text = player2
        }

        //dodaje imiona playerow do statystyk ~u
        //val app = application as Stats
        app.player1 = btnPlayer1.text.toString()
        app.player2 = btnPlayer2.text.toString()

        //efekt pojawienia się piłki jeżeli button jest przytrzymany
        btnPlayer1.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Button pressed, set icon visible
                    btnPlayer1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ball, 0, 0, 0)
                }
                MotionEvent.ACTION_UP -> {
                    // Button released, set icon invisible
                    btnPlayer1.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }
            }
            // Return false to allow other touch events like clicks to be handled
            false
        }
        btnPlayer2.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Button pressed, set icon visible
                    btnPlayer2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ball, 0, 0, 0)
                }
                MotionEvent.ACTION_UP -> {
                    // Button released, set icon invisible
                    btnPlayer2.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }
            }
            // Return false to allow other touch events like clicks to be handled
            false
        }

        firebaseAuth=FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString()).child("Matches").child(matchId.toString())

        //kliknięcie na gracza, który serwuje jako pierwsze
        //& przekierowanie do kolejnej aktywności
        btnPlayer1.setOnClickListener{
            app.serve1="1" //do statysyk
            app.serve2=""
            database.child("LastServePlayer").setValue(btnPlayer1.text.toString())
            database.child("pktCount").setValue(1)
            callActivity()
        }
        btnPlayer2.setOnClickListener{
            app.serve1=""
            app.serve2="1"
            database.child("LastServePlayer").setValue(btnPlayer2.text.toString())
            database.child("pktCount").setValue(1)
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
            finish()
        }
    }
}