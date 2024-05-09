package com.anw.tenistats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
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

class ActivityStartPoint : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    var matchId: String?=null
    var pointNr: Int = 0

    private lateinit var player1: TextView
    private lateinit var player2: TextView
    private lateinit var serve1: TextView
    private lateinit var serve2: TextView
    private lateinit var pkt1: TextView
    private lateinit var set1p1: TextView
    private lateinit var set2p1: TextView
    private lateinit var set3p1: TextView
    private lateinit var pkt2: TextView
    private lateinit var set1p2: TextView
    private lateinit var set2p2: TextView
    private lateinit var set3p2: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start_point)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()

        //MENU
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

        val userEmail = firebaseAuth.currentUser?.email.toString()
        val userEmailView = headerView.findViewById<TextView>(R.id.textViewUserEmail)
        if(userEmail.isNotEmpty()) {
            userEmailView.text = userEmail
        }else {
            userEmailView.text = resources.getString(R.string.user_email)
        }
        //MENU
        val user = firebaseAuth.currentUser?.uid

        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(user.toString())
        database.child("Current match").get().addOnSuccessListener {dataSnapshot ->
            matchId = dataSnapshot.getValue(String::class.java)
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to read Current Match ID", Toast.LENGTH_SHORT).show()
        }


        //czy to jest potrzebne? bo wydaje mi się, że zmienna database nigdzie nie jest dalej używana
        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(user.toString()).child("Matches").child(matchId.toString())
        val app = application as Stats
        player1 = findViewById<TextView>(R.id.textviewPlayer1)
        player2 = findViewById<TextView>(R.id.textviewPlayer2)
        serve1 = findViewById<TextView>(R.id.textViewBallPl1)
        serve2 = findViewById<TextView>(R.id.textViewBallPl2)
        pkt1 = findViewById<TextView>(R.id.textViewPktPl1)
        set1p1 = findViewById<TextView>(R.id.textViewSet1Pl1)
        set2p1 = findViewById<TextView>(R.id.textViewSet2Pl1)
        set3p1 = findViewById<TextView>(R.id.textViewSet3Pl1)
        pkt2 = findViewById<TextView>(R.id.textViewPktPl2)
        set1p2 = findViewById<TextView>(R.id.textViewSet1Pl2)
        set2p2 = findViewById<TextView>(R.id.textViewSet2Pl2)
        set3p2 = findViewById<TextView>(R.id.textViewSet3Pl2)
        val addPointDialog = AddPointDialog(this,true)
        fillUpScoreInActivity(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)

        val btnFault = findViewById<Button>(R.id.buttonFault)
        val fault_text = resources.getString(R.string.fault)
        val double_fault_text = resources.getString(R.string.double_fault)
        val first_serve_text = resources.getString(R.string.first_serve)
        val second_serve_text = resources.getString(R.string.second_serve)

        val (_, _) = calculateGame(set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)

        findViewById<Button>(R.id.buttonAce).setOnClickListener {
            val (game,set) = calculateGame(set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            if (app.serve1 != "") { //serwuje player 1
                if (btnFault.text == double_fault_text) {
                    btnFault.text = fault_text
                    findViewById<TextView>(R.id.textViewFS).text = first_serve_text
                }
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
                if (btnFault.text == double_fault_text) {
                    btnFault.text = fault_text
                    findViewById<TextView>(R.id.textViewFS).text = first_serve_text
                }
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

        btnFault.setOnClickListener {
            val (game,set) = calculateGame(set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            if (btnFault.text == fault_text) {
                app.serwis=2
                btnFault.text = double_fault_text
                btnFault.textSize = 15.4f
                findViewById<TextView>(R.id.textViewFS).text = second_serve_text
            }
            else if (btnFault.text == double_fault_text) {
                app.serwis=0
                btnFault.text = fault_text
                btnFault.textSize = 19.9f
                findViewById<TextView>(R.id.textViewFS).text = first_serve_text
                if (app.serve1 != "") {
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
                if (btnFault.text == double_fault_text) {
                    btnFault.text = fault_text
                    findViewById<TextView>(R.id.textViewFS).text = first_serve_text
                }
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
                if (btnFault.text == double_fault_text) {
                    btnFault.text = fault_text
                    findViewById<TextView>(R.id.textViewFS).text = first_serve_text
                }
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
                if (btnFault.text == double_fault_text) {
                    btnFault.text = fault_text
                    findViewById<TextView>(R.id.textViewFS).text = first_serve_text
                }
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
                if (btnFault.text == double_fault_text) {
                    btnFault.text = fault_text
                    findViewById<TextView>(R.id.textViewFS).text = first_serve_text
                }
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
                if (btnFault.text == double_fault_text) {
                    btnFault.text = fault_text
                    findViewById<TextView>(R.id.textViewFS).text = first_serve_text
                }
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
                if (btnFault.text == double_fault_text) {
                    btnFault.text = fault_text
                    findViewById<TextView>(R.id.textViewFS).text = first_serve_text
                }
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
                if (btnFault.text == double_fault_text) {
                    btnFault.text = fault_text
                    findViewById<TextView>(R.id.textViewFS).text = first_serve_text
                }
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
                if (btnFault.text == double_fault_text) {
                    btnFault.text = fault_text
                    findViewById<TextView>(R.id.textViewFS).text = first_serve_text
                }
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
                if (btnFault.text == double_fault_text) {
                    btnFault.text = fault_text
                    findViewById<TextView>(R.id.textViewFS).text = first_serve_text
                }
            }
            else {
                if (btnFault.text == double_fault_text) {
                    btnFault.text = fault_text
                    findViewById<TextView>(R.id.textViewFS).text = first_serve_text
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
            finish()
        }

        backButton.setOnClickListener{
            if (matchId != null) {
                database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference(user.toString()).child("Matches").child(matchId.toString())
                database.child("pktCount").get()
                    .addOnSuccessListener { pktCountSnapshot ->
                        val pktCount = pktCountSnapshot.getValue(Int::class.java)
                        if (pktCount != null) {
                            pointNr = pktCount
                            undoAfterCare(user, app)
                        } else {
                            Toast.makeText(this, "pktCount is Null", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Failed to read pktCount", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Current Match ID is null", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun calculateGame(set1p1: TextView, set1p2: TextView, set2p1: TextView, set2p2: TextView, set3p1: TextView, set3p2: TextView): Pair<Int, Int> {
        val game: Int
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

        return Pair(game, set)
    }

    fun undoAfterCare(user: String?, context: Context) {
        if (pointNr <= 1) {
            Toast.makeText(this, "Nothing to undo", Toast.LENGTH_LONG).show()
        } else {
            var scoreIsSet: Boolean
            val app = (context.applicationContext as? Stats)
            //wyliczenie stanu rozgrywki na podstawie wyników z tablicy
            var (gameId, setId) = calculateGame(set1p1, set1p2, set2p1, set2p2, set3p1, set3p2)
            var scoringPlayer: String
            if (app != null) {
                val db =
                    FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                        .getReference(user.toString()).child("Matches").child("$matchId")
                val delPointNr = pointNr - 1 //numer usuwanego punktu
                var delPointNrString = delPointNr.toString()
                if (delPointNr < 10) {
                    delPointNrString = "0" + delPointNr
                }
                val deletePoint = db.child("set $setId").child("game $gameId")
                    .child("point $delPointNrString")

                if (pointNr == 2) {
                    deletePoint.child("servePlayer").get()
                        .addOnSuccessListener { ktoServeSnapshot ->
                            val ktoServe = ktoServeSnapshot.getValue(String::class.java)
                            //ustawienie wartości globalnych
                            if (ktoServe != null) {
                                scoringPlayer = ktoServe
                                if (scoringPlayer == player1.text.toString()) {
                                    app.serve1 = "1"
                                    app.serve2 = ""
                                } else {
                                    app.serve1 = ""
                                    app.serve2 = "1"
                                }

                                fillUpScore(
                                    app,
                                    player1,
                                    toTxtV("0"),
                                    toTxtV("0"),
                                    toTxtV("0"),
                                    toTxtV("0"),
                                    toTxtV(""),
                                    toTxtV(""),
                                    toTxtV(""),
                                    toTxtV("")
                                )
                                //ustawienie wartosci w tabelce
                                fillUpScoreInActivity(
                                    app,
                                    player1,
                                    player2,
                                    serve1,
                                    serve2,
                                    pkt1,
                                    pkt2,
                                    set1p1,
                                    set1p2,
                                    set2p1,
                                    set2p2,
                                    set3p1,
                                    set3p2
                                )
                                deletePoint(delPointNr, db, deletePoint)
                            } else {
                                Toast.makeText(this, "kto is Null", Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener {
                            Toast.makeText(this, "Couldn't get who serves", Toast.LENGTH_SHORT)
                                .show()
                        }
                } else if (pointNr > 2) {
                    val prevPointNr = delPointNr - 1 //numer poprzednika usuwanego punktu
                    var delPointNrString = delPointNr.toString()
                    if (delPointNr < 10) {
                        delPointNrString = "0" + delPointNr
                    }
                    var prevPoint = db.child("set $setId").child("game $gameId")
                        .child("point $delPointNrString")

                    //pobranie poprzeniego stanu meczu przed zdobyciem punktu z wezla score
                    prevPoint.child("score").get().addOnSuccessListener {
                        if (it.exists()) {
                            val getpkt1 = it.child("pkt1").getValue(String::class.java)
                            val getpkt2 = it.child("pkt2").getValue(String::class.java)
                            val getset1p1 = it.child("set1p1").getValue(String::class.java)
                            val getset1p2 = it.child("set1p2").getValue(String::class.java)
                            val getset2p1 = it.child("set2p1").getValue(String::class.java)
                            val getset2p2 = it.child("set2p2").getValue(String::class.java)
                            val getset3p1 = it.child("set3p1").getValue(String::class.java)
                            val getset3p2 = it.child("set3p2").getValue(String::class.java)

                            deletePoint.child("servePlayer").get()
                                .addOnSuccessListener { ktoServeSnapshot ->
                                    val ktoServe = ktoServeSnapshot.getValue(String::class.java)
                                    if (ktoServe != null) {
                                        scoringPlayer = ktoServe
                                        if (scoringPlayer == player1.text.toString()) {
                                            app.serve1 = "1"
                                            app.serve2 = ""

                                        } else {
                                            app.serve1 = ""
                                            app.serve2 = "1"
                                        }
                                        //zmiana wartosci globalnych
                                        fillUpScore(
                                            app,
                                            player1,
                                            toTxtV(getpkt1),
                                            toTxtV(getpkt2),
                                            toTxtV(getset1p1),
                                            toTxtV(getset1p2),
                                            toTxtV(getset2p1),
                                            toTxtV(getset2p2),
                                            toTxtV(getset3p1),
                                            toTxtV(getset3p2)
                                        )
                                        //ustawienie wartosci w tabelce
                                        fillUpScoreInActivity(
                                            app,
                                            player1,
                                            player2,
                                            serve1,
                                            serve2,
                                            pkt1,
                                            pkt2,
                                            set1p1,
                                            set1p2,
                                            set2p1,
                                            set2p2,
                                            set3p1,
                                            set3p2
                                        )
                                        deletePoint(delPointNr, db, deletePoint)
                                    } else {
                                        Toast.makeText(this, "kto is Null", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }.addOnFailureListener {
                                    Toast.makeText(
                                        this,
                                        "Couldn't get who serves",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                        } else {
                            if (gameId > 1) {
                                gameId -= 1
                                prevPoint = db.child("set $setId").child("game $gameId")
                                    .child("point $delPointNrString")
                                prevPoint.child("score").get().addOnSuccessListener {
                                    if (it.exists()) {
                                        val getpkt1 = it.child("pkt1").getValue(String::class.java)
                                        val getpkt2 = it.child("pkt2").getValue(String::class.java)
                                        val getset1p1 =
                                            it.child("set1p1").getValue(String::class.java)
                                        val getset1p2 =
                                            it.child("set1p2").getValue(String::class.java)
                                        val getset2p1 =
                                            it.child("set2p1").getValue(String::class.java)
                                        val getset2p2 =
                                            it.child("set2p2").getValue(String::class.java)
                                        val getset3p1 =
                                            it.child("set3p1").getValue(String::class.java)
                                        val getset3p2 =
                                            it.child("set3p2").getValue(String::class.java)

                                        deletePoint.child("servePlayer").get()
                                            .addOnSuccessListener { ktoServeSnapshot ->
                                                val ktoServe =
                                                    ktoServeSnapshot.getValue(String::class.java)
                                                if (ktoServe != null) {
                                                    scoringPlayer = ktoServe
                                                    if (scoringPlayer == player1.text.toString()) {
                                                        app.serve1 = "1"
                                                        app.serve2 = ""

                                                    } else {
                                                        app.serve1 = ""
                                                        app.serve2 = "1"
                                                    }
                                                    //zmiana wartosci globalnych
                                                    fillUpScore(
                                                        app,
                                                        player1,
                                                        toTxtV(getpkt1),
                                                        toTxtV(getpkt2),
                                                        toTxtV(getset1p1),
                                                        toTxtV(getset1p2),
                                                        toTxtV(getset2p1),
                                                        toTxtV(getset2p2),
                                                        toTxtV(getset3p1),
                                                        toTxtV(getset3p2)
                                                    )
                                                    //ustawienie wartosci w tabelce
                                                    fillUpScoreInActivity(
                                                        app,
                                                        player1,
                                                        player2,
                                                        serve1,
                                                        serve2,
                                                        pkt1,
                                                        pkt2,
                                                        set1p1,
                                                        set1p2,
                                                        set2p1,
                                                        set2p2,
                                                        set3p1,
                                                        set3p2
                                                    )
                                                    deletePoint(delPointNr, db, deletePoint)
                                                } else {
                                                    Toast.makeText(
                                                        this,
                                                        "kto is Null",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }.addOnFailureListener {
                                                Toast.makeText(
                                                    this,
                                                    "Couldn't get who serves",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                }
                            } else if (gameId == 1 && setId > 1) {
                                setId = -1
                                db.child("set${setId}p1").get()
                                    .addOnSuccessListener { dataSnapshot ->
                                        if (it.exists()) {
                                            val game1 = dataSnapshot.getValue(String::class.java)
                                            db.child("set${setId}p2").get()
                                                .addOnSuccessListener { dataSnapshot ->
                                                    if (it.exists()) {
                                                        val game2 =
                                                            dataSnapshot.getValue(String::class.java)
                                                        val game1Int = game1?.toInt()
                                                        val game2Int = game2?.toInt()
                                                        gameId = game1Int?.plus(game2Int!!) ?: 0
                                                        prevPoint = db.child("set $setId")
                                                            .child("game $gameId")
                                                            .child("point $delPointNrString")
                                                        prevPoint.child("score").get()
                                                            .addOnSuccessListener {
                                                                if (it.exists()) {
                                                                    val getpkt1 = it.child("pkt1")
                                                                        .getValue(String::class.java)
                                                                    val getpkt2 = it.child("pkt2")
                                                                        .getValue(String::class.java)
                                                                    val getset1p1 =
                                                                        it.child("set1p1")
                                                                            .getValue(String::class.java)
                                                                    val getset1p2 =
                                                                        it.child("set1p2")
                                                                            .getValue(String::class.java)
                                                                    val getset2p1 =
                                                                        it.child("set2p1")
                                                                            .getValue(String::class.java)
                                                                    val getset2p2 =
                                                                        it.child("set2p2")
                                                                            .getValue(String::class.java)
                                                                    val getset3p1 =
                                                                        it.child("set3p1")
                                                                            .getValue(String::class.java)
                                                                    val getset3p2 =
                                                                        it.child("set3p2")
                                                                            .getValue(String::class.java)

                                                                    deletePoint.child("servePlayer")
                                                                        .get()
                                                                        .addOnSuccessListener { ktoServeSnapshot ->
                                                                            val ktoServe =
                                                                                ktoServeSnapshot.getValue(
                                                                                    String::class.java
                                                                                )
                                                                            if (ktoServe != null) {
                                                                                scoringPlayer =
                                                                                    ktoServe
                                                                                if (scoringPlayer == player1.text.toString()) {
                                                                                    app.serve1 = "1"
                                                                                    app.serve2 = ""

                                                                                } else {
                                                                                    app.serve1 = ""
                                                                                    app.serve2 = "1"
                                                                                }
                                                                                //zmiana wartosci globalnych
                                                                                fillUpScore(
                                                                                    app,
                                                                                    player1,
                                                                                    toTxtV(getpkt1),
                                                                                    toTxtV(getpkt2),
                                                                                    toTxtV(getset1p1),
                                                                                    toTxtV(getset1p2),
                                                                                    toTxtV(getset2p1),
                                                                                    toTxtV(getset2p2),
                                                                                    toTxtV(getset3p1),
                                                                                    toTxtV(getset3p2)
                                                                                )
                                                                                //ustawienie wartosci w tabelce
                                                                                fillUpScoreInActivity(
                                                                                    app,
                                                                                    player1,
                                                                                    player2,
                                                                                    serve1,
                                                                                    serve2,
                                                                                    pkt1,
                                                                                    pkt2,
                                                                                    set1p1,
                                                                                    set1p2,
                                                                                    set2p1,
                                                                                    set2p2,
                                                                                    set3p1,
                                                                                    set3p2
                                                                                )
                                                                                deletePoint(
                                                                                    delPointNr,
                                                                                    db,
                                                                                    deletePoint
                                                                                )
                                                                            } else {
                                                                                Toast.makeText(
                                                                                    this,
                                                                                    "kto is Null",
                                                                                    Toast.LENGTH_SHORT
                                                                                ).show()
                                                                            }
                                                                        }.addOnFailureListener {
                                                                            Toast.makeText(
                                                                                this,
                                                                                "Couldn't get who serves",
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                        }
                                                                }
                                                            }
                                                    }
                                                }
                                        }
                                    }
                            }
                        }
                    }
                }
            }
        }
    }

    // Function to delete a point
    private fun deletePoint(delPointNr: Int, db: DatabaseReference, deletePoint: DatabaseReference) {
        db.child("pktCount").setValue(delPointNr)
        deletePoint.removeValue().addOnSuccessListener {
            Toast.makeText(this, "Undo", Toast.LENGTH_LONG).show()
        }.addOnFailureListener{
            Toast.makeText(this, "Undo failed", Toast.LENGTH_LONG).show()
        }
    }

    private fun toTxtV(a: String?): TextView {
        val textView = TextView(this)
        textView.text = a
        return textView
    }

}