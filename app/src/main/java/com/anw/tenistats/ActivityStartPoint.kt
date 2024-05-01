package com.anw.tenistats

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
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

class ActivityStartPoint : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    var matchId: String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start_point)

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
        val backButton = findViewById<ImageButton>(R.id.buttonReturnUndo)
        backButton.setOnClickListener{
            startActivity(Intent(this,ActivityMenu::class.java))
        }

        val userEmail = firebaseAuth.currentUser?.email.toString()
        val userEmailView = headerView.findViewById<TextView>(R.id.textViewUserEmail)
        if(userEmail.isNotEmpty()) {
            userEmailView.text = userEmail
        }else {
            userEmailView.text = "user_email@smth.com"
        }
        //MENU

        //val matchId = intent.getStringExtra("matchID")
        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(user.toString()).child("Current match")
        database.get().addOnSuccessListener {dataSnapshot ->
            matchId = dataSnapshot.getValue(String::class.java)
        }

        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(user.toString()).child("Matches").child(matchId.toString())

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
        val addPointDialog = AddPointDialog(this,true)
        fillUpScoreInActivity(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)

        //database.child("pkt1").setValue(pkt1.text)
        //database.child("pkt2").setValue(pkt2.text)


        val (_, _) = calculateGame(set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)

        findViewById<Button>(R.id.buttonAce).setOnClickListener {
            val (game,set) = calculateGame(set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            if (app.serve1 != "") { //serwuje player 1
                app.totalpoints1++
                val btnFault = findViewById<Button>(R.id.buttonFault)
                if (btnFault.text == "Fault") { //trafiony 1 serwis
                    app.firstservein1++
                }
                else if (btnFault.text == "Double Fault") {
                    app.secondservein1++
                    btnFault.text = "Fault"
                    findViewById<TextView>(R.id.textViewFS).text = "1st Serve"
                }
                app.ace1++
                addPointDialog.show(
                    player1, player2, serve1, serve2, pkt1, pkt2, set1p1, set1p2, set2p1, set2p2, set3p1, set3p2,
                    pkt1.text.toString(),
                    pkt2.text.toString(),
                    player1.text.toString(),
                    "Ace",
                    "",
                    "",
                    matchId.toString(),
                    game.toString(),
                    set.toString())
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
                addPointDialog.show(
                    player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
                    pkt1.text.toString(),
                    pkt2.text.toString(),
                    player2.text.toString(),
                    "Ace",
                    "",
                    "",
                    matchId.toString(),
                    game.toString(),
                    set.toString())
            }
        }

        findViewById<Button>(R.id.buttonFault).setOnClickListener {
            val (game,set) = calculateGame(set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
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
                if (app.serve1 != "") {
                    app.totalpoints2++
                    app.doublefault1++
                    addPointDialog.show(
                        player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
                        pkt1.text.toString(),
                        pkt2.text.toString(),
                        player1.text.toString(),
                        "Double Fault",
                        "",
                        "",
                        matchId.toString(),
                        game.toString(),
                        set.toString())
                }
                else {
                    app.totalpoints1++
                    app.doublefault2++
                    addPointDialog.show(
                        player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
                        pkt1.text.toString(),
                        pkt2.text.toString(),
                        player2.text.toString(),
                        "Double Fault",
                        "",
                        "",
                        matchId.toString(),
                        game.toString(),
                        set.toString())
                }
            }
        }

        findViewById<Button>(R.id.buttonRWF).setOnClickListener{
            val (game,set) = calculateGame(set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            if (app.serve1 != "") { //serwuje player 1
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
                addPointDialog.show(
                    player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
                    pkt1.text.toString(),
                    pkt2.text.toString(),
                    player2.text.toString(),
                    "Winner",
                    "Return",
                    "Forehand",
                    matchId.toString(),
                    game.toString(),
                    set.toString())
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
                addPointDialog.show(
                    player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
                    pkt1.text.toString(),
                    pkt2.text.toString(),
                    player1.text.toString(),
                    "Winner",
                    "Return",
                    "Forehand",
                    matchId.toString(),
                    game.toString(),
                    set.toString())
            }
        }

        findViewById<Button>(R.id.buttonRWB).setOnClickListener {
            val (game,set) = calculateGame(set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            if (app.serve1 != "") { //serwuje player 1
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
                addPointDialog.show(
                    player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
                    pkt1.text.toString(),
                    pkt2.text.toString(),
                    player2.text.toString(),
                    "Winner",
                    "Return",
                    "Backhand",
                    matchId.toString(),
                    game.toString(),
                    set.toString())
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
                addPointDialog.show(
                    player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
                    pkt1.text.toString(),
                    pkt2.text.toString(), player1.text.toString(),
                    "Winner",
                    "Return",
                    "Backhand",
                    matchId.toString(),
                    game.toString(),
                    set.toString())
            }
        }

        findViewById<Button>(R.id.buttonREF).setOnClickListener {
            val (game,set) = calculateGame(set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            if (app.serve1 != "") { //serwuje player 1
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
                addPointDialog.show(
                    player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
                    pkt1.text.toString(),
                    pkt2.text.toString(),
                    player2.text.toString(),
                    "Winner",
                    "Return",
                    "Forehand",
                    matchId.toString(),
                    game.toString(),
                    set.toString())
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
                addPointDialog.show(
                    player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
                    pkt1.text.toString(),
                    pkt2.text.toString(),
                    player1.text.toString(),
                    "Winner",
                    "Return",
                    "Forehand",
                    matchId.toString(),
                    game.toString(),
                    set.toString())
            }
        }

        findViewById<Button>(R.id.buttonREB).setOnClickListener {
            val (game,set) = calculateGame(set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            if (app.serve1 != "") { //serwuje player 1
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
                addPointDialog.show(
                    player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2,
                    pkt1.text.toString(),
                    pkt2.text.toString(),
                    player2.text.toString(),
                    "Error",
                    "Return",
                    "Backhand",
                    matchId.toString(),
                    game.toString(),
                    set.toString())
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
                addPointDialog.show (
                    player2,player1,serve2,serve1,pkt2,pkt1,set1p2,set1p1,set2p2,set2p1,set3p2,set3p1,
                    pkt1.text.toString(),
                    pkt2.text.toString(),
                    player1.text.toString(),
                    "Error",
                    "Return",
                    "Backhand",
                    matchId.toString(),
                    game.toString(),
                    set.toString())
            }
        }

        findViewById<Button>(R.id.buttonBIP).setOnClickListener {
            val (game,set) = calculateGame(set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            if(app.serve1 != ""){
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

    fun calculateGame(set1p1: TextView, set1p2: TextView, set2p1: TextView, set2p2: TextView, set3p1: TextView, set3p2: TextView): Pair<Int, Int> {
        var game = 1
        var set = 1

        if (set2p1.text == "") {
            val game1 = set1p1.text.toString().toInt()
            val game2 = set1p2.text.toString().toInt()
            game = game1 + game2 + 1
        } else if (set3p1.text == "") {
            set = 2
            val game1 = set2p1.text.toString().toInt()
            val game2 = set2p2.text.toString().toInt()
            game = game1 + game2 + 1
        } else {
            set = 3
            val game1 = set3p1.text.toString().toInt()
            val game2 = set3p2.text.toString().toInt()
            game = game1 + game2 + 1
        }
        /*database.child("set1p1").setValue((set1p1.text.toString()))
        database.child("set2p1").setValue((set2p1.text.toString()))
        database.child("set3p1").setValue((set3p1.text.toString()))
        database.child("set1p2").setValue((set1p2.text.toString()))
        database.child("set2p2").setValue((set2p2.text.toString()))
        database.child("set3p2").setValue((set3p2.text.toString()))*/

        return Pair(game, set)
    }
}