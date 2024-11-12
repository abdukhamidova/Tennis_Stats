package com.anw.tenistats.matchplay

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.anw.tenistats.ViewMatchesActivity
import com.anw.tenistats.dialog.AddPointDialog
import com.anw.tenistats.databinding.ActivityStartPointBinding
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StartPointActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartPointBinding
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
        binding = ActivityStartPointBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(user.toString()).child("Matches").child(matchId.toString())

            //ustawienie wyniku w tabeli
            setscore(player1,player2,serve1,serve2,set1p1,set2p1,set3p1,set1p2,set2p2,set3p2,pkt1,pkt2)

            database.child("winner").get().addOnSuccessListener { winnerSnapshot ->
                if(winnerSnapshot.exists()){
                    startActivity(Intent(this, ViewMatchesActivity::class.java))
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to read Current Match ID", Toast.LENGTH_SHORT).show()
        }


        //czy to jest potrzebne? bo wydaje mi się, że zmienna database nigdzie nie jest dalej używana
       // database =
        //    FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
         //       .getReference(user.toString()).child("Matches").child(matchId.toString())

        val app = application as StatsClass
        player1 = binding.textviewPlayer1
        player2 = binding.textviewPlayer2
        serve1 = binding.textViewBallPl1
        serve2 = binding.textViewBallPl2
        pkt1 = binding.textViewPktPl1
        set1p1 = binding.textViewSet1Pl1
        set2p1 = binding.textViewSet2Pl1
        set3p1 = binding.textViewSet3Pl1
        pkt2 = binding.textViewPktPl2
        set1p2 = binding.textViewSet1Pl2
        set2p2 = binding.textViewSet2Pl2
        set3p2 = binding.textViewSet3Pl2
        val addPointDialog = AddPointDialog(this,true)
        fillUpScoreInActivity(app,player1,player2,serve1,serve2,pkt1,pkt2,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)

        val btnFault = binding.buttonFault
        val fault_text = resources.getString(R.string.fault)
        val double_fault_text = resources.getString(R.string.double_fault)
        val first_serve_text = resources.getString(R.string.first_serve)
        val second_serve_text = resources.getString(R.string.second_serve)

       // val (_, _) = calculateGame(set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)

        binding.buttonAce.setOnClickListener {
            val (game,set) = calculateGame(set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            if (app.serve1 != "") { //serwuje player 1
                if (btnFault.text == double_fault_text) {
                    btnFault.text = fault_text
                    binding.textViewFS.text = first_serve_text
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
                    binding.textViewFS.text = first_serve_text
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
                binding.textViewFS.text = second_serve_text
            }
            else if (btnFault.text == double_fault_text) {
                app.serwis=0
                btnFault.text = fault_text
                btnFault.textSize = 19.9f
                binding.textViewFS.text = first_serve_text
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

        binding.buttonRWF.setOnClickListener{
            val (game,set) = calculateGame(set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            if (app.serve1 != "") { //serwuje player 1
                if (btnFault.text == double_fault_text) {
                    btnFault.text = fault_text
                    binding.textViewFS.text = first_serve_text
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
                    binding.textViewFS.text = first_serve_text
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

        binding.buttonRWB.setOnClickListener {
            val (game,set) = calculateGame(set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            if (app.serve1 != "") { //serwuje player 1
                if (btnFault.text == double_fault_text) {
                    btnFault.text = fault_text
                    binding.textViewFS.text = first_serve_text
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
                    binding.textViewFS.text = first_serve_text
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

        binding.buttonREF.setOnClickListener {
            val (game,set) = calculateGame(set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            if (app.serve1 != "") { //serwuje player 1
                if (btnFault.text == double_fault_text) {
                    btnFault.text = fault_text
                    binding.textViewFS.text = first_serve_text
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
                    binding.textViewFS.text = first_serve_text
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

        binding.buttonREB.setOnClickListener {
            val (game,set) = calculateGame(set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            if (app.serve1 != "") { //serwuje player 1
                if (btnFault.text == double_fault_text) {
                    btnFault.text = fault_text
                    binding.textViewFS.text = first_serve_text
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
                    binding.textViewFS.text = first_serve_text
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

        binding.buttonBIP.setOnClickListener {
            val (game,set) = calculateGame(set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
            if(app.serve1 != ""){
                if (btnFault.text == double_fault_text) {
                    btnFault.text = fault_text
                    binding.textViewFS.text = first_serve_text
                }
            }
            else {
                if (btnFault.text == double_fault_text) {
                    btnFault.text = fault_text
                    binding.textViewFS.text = first_serve_text
                }
            }

            val intent=Intent(this, BallInPlayActivity::class.java).also{
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
        database.child("winner").get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val winner = dataSnapshot.getValue(String::class.java)
                val goldenLaurel = getGoldenDrawable(applicationContext, R.drawable.icon_laurel3)
                serve1.setCompoundDrawablesWithIntrinsicBounds(goldenLaurel, null, null, null)
                serve2.setCompoundDrawablesWithIntrinsicBounds(goldenLaurel, null, null, null)
                if (winner == player1.text) {
                    serve1.visibility = View.VISIBLE
                    serve2.visibility = View.INVISIBLE
                } else {
                    serve1.visibility = View.INVISIBLE
                    serve2.visibility = View.VISIBLE
                }
            } else {
                database.child("LastServePlayer").get().addOnSuccessListener { dataSnapshot ->
                    val lastServe = dataSnapshot.getValue(String::class.java)
                    serve1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ball, 0, 0, 0)
                    serve2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ball, 0, 0, 0)
                    if (lastServe == player1.text) {
                        serve1.visibility = View.VISIBLE
                        serve2.visibility = View.INVISIBLE
                    } else {
                        serve1.visibility = View.INVISIBLE
                        serve2.visibility = View.VISIBLE
                    }
                    if(app.player1 == lastServe){
                        app.serve1="1"
                        app.serve2=""
                    } else{
                        app.serve1=""
                        app.serve2="1"
                    }
                }.addOnFailureListener {
                    // Handle failure
                }
            }
        }.addOnFailureListener {
            // Handle failure
        }
    }

    private fun calculateGame(set1p1: TextView, set1p2: TextView, set2p1: TextView, set2p2: TextView, set3p1: TextView, set3p2: TextView): Pair<Int, Int> {
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

    private fun undoAfterCare(user: String?, context: Context) {
        if (pointNr <= 1) {
            Toast.makeText(this, "Nothing to undo", Toast.LENGTH_LONG).show()
        } else {
            val app = (context.applicationContext as? StatsClass)
            //wyliczenie stanu rozgrywki na podstawie wyników z tablicy
            val (gameId, setId) = calculateGame(set1p1, set1p2, set2p1, set2p2, set3p1, set3p2)

            if (app != null) {
                val db =
                    FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                        .getReference(user.toString()).child("Matches").child("$matchId")
                val delPointNr = pointNr - 1 //numer usuwanego punktu

                //numer punktu dla sciezki (z zerem)
                val delPointNrString = addZeros(delPointNr)
                //val gameIdString = addZeros(gameId)
                //sceizka do usuwanego punktu
                getPointSetGame(db, setId, gameId, delPointNrString) { setId, gameId ->
                    //uzycie odpowiedniej sciezki do usuwanego punktu
                    val gameIdString = addZeros(gameId)
                    val deletePoint = db.child("set $setId").child("game $gameIdString")
                        .child("point $delPointNrString")

                    if (pointNr == 2) {
                        //jest tylko jeden punkt, wyzerowac tablice
                        deletePoint.child("servePlayer").get()
                            .addOnSuccessListener { ktoServeSnapshot ->
                                val ktoServe = ktoServeSnapshot.getValue(String::class.java)
                                //ustawienie wartości globalnych, kto teraz serwuje
                                if (ktoServe != null) {
                                    if (ktoServe == player1.text.toString()) {
                                        app.serve1 = "1"
                                        app.serve2 = ""
                                    } else {
                                        app.serve1 = ""
                                        app.serve2 = "1"
                                    }

                                    //ustawienie wartosci globalnych
                                    fillUpScore(app, player1, toTxtV("0"), toTxtV("0"), toTxtV("0"), toTxtV("0"), toTxtV(""), toTxtV(""), toTxtV(""), toTxtV(""))
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
                                    //usuniecie punktu
                                    deletePoint(delPointNr, db, deletePoint)
                                } else {
                                    Toast.makeText(this, "kto is Null", Toast.LENGTH_SHORT).show()
                                }
                            }.addOnFailureListener {
                                Toast.makeText(this, "Couldn't get who serves", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                    else if (pointNr > 2) {
                        //punktow jest przynajmniej dwa, wrocic do poprzedniego stanu
                        val prevPointNr = delPointNr - 1 //numer poprzednika usuwanego punktu
                        val prevPointNrString = addZeros(prevPointNr)
                        //znalezenie sciezki set, game poprzedniego punktu
                        getPointSetGame(db, setId, gameId, prevPointNrString) { prevPointSet, prevPointGame ->
                            //uzycie odpowiedniej sciezki do przywrocenia poprzedniego stanu meczu
                            prevPointScore(app, db, deletePoint, delPointNr, prevPointSet, prevPointGame, prevPointNrString)
                        }
                    }
                }

            }
        }
    }

    //funkcja dla punktu poprzedniego
    private fun prevPointScore(context: Context, db: DatabaseReference, deletePoint: DatabaseReference, delPointNr: Int, setId: Int, gameId: Int, prevPointNrString: String) {
        val app = (context.applicationContext as? StatsClass)
        val gameIdString = addZeros(gameId)
        if(app!=null){
            val prevPoint = db.child("set $setId").child("game $gameIdString")
                .child("point $prevPointNrString")

            //pobranie poprzeniego stanu meczu z wezla score przed zdobyciem punktu
            prevPoint.child("score").get().addOnSuccessListener {
                if (it.exists()) {
                    val ktoServe = it.child("servePlayer").getValue(String::class.java)
                    val getpkt1 = it.child("pkt1").getValue(String::class.java)
                    val getpkt2 = it.child("pkt2").getValue(String::class.java)
                    val getset1p1 = it.child("set1p1").getValue(String::class.java)
                    val getset1p2 = it.child("set1p2").getValue(String::class.java)
                    val getset2p1 = it.child("set2p1").getValue(String::class.java)
                    val getset2p2 = it.child("set2p2").getValue(String::class.java)
                    val getset3p1 = it.child("set3p1").getValue(String::class.java)
                    val getset3p2 = it.child("set3p2").getValue(String::class.java)
                    db.child("LastServePlayer").setValue(ktoServe)
                    db.child("pkt1").setValue(getpkt1)
                    db.child("pkt2").setValue(getpkt2)
                    db.child("set1p1").setValue(getset1p1)
                    db.child("set1p2").setValue(getset1p2)
                    db.child("set2p1").setValue(getset2p1)
                    db.child("set2p2").setValue(getset2p2)
                    db.child("set3p1").setValue(getset3p1)
                    db.child("set3p2").setValue(getset3p2)
                    if (ktoServe != null) {
                        if (ktoServe == player1.text.toString()) {
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
                        //usuniecie punktu
                        deletePoint(delPointNr, db, deletePoint)
                    } else {
                        Toast.makeText(this, "kto is Null", Toast.LENGTH_SHORT)
                            .show()
                    }

                }else{
                    Toast.makeText(this, "Failed to read prevPointScore", Toast.LENGTH_SHORT).show()
                }

            }
        }


    }

    private fun getPointSetGame(db: DatabaseReference, setId: Int, gameId: Int, PointNrString: String, completion: (Int, Int) -> Unit) {
        var retGame: Int
        var retSet: Int
        val gameIdString = addZeros(gameId)
        //sprawdza czy prevPoint jest w tym gamie
        db.child("set $setId").child("game $gameIdString").child("point $PointNrString").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // jezeli wezel poprzedniego punktu nie istnieje
                    if (gameId > 1) {
                        //to jest w tym samym set, poprzednim game
                        retGame = gameId - 1
                        retSet = setId
                        completion(retSet, retGame)
                    } else if (gameId == 1 && setId > 1) {
                        //to jest w poprzednim set, ostatnim game poprzedniego setu
                        retSet = setId - 1
                        sumGame(db, retSet) { sum ->
                            //sum to ilosc gamow poprzednieho setu => numer ostatnego gamu
                            retGame = sum
                            completion(retSet, retGame)
                        }
                    }
                } else {
                    //prevPoint jest w tym gamie, zwracam bez zadnych zmian
                    retGame = gameId
                    retSet = setId
                    completion(retSet, retGame)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                //Toast.makeText(this, "game read failed", Toast.LENGTH_SHORT).show()
            }
        })
    }
    //zwraca numer ostatniego game poprzedniego setu
    fun sumGame(db: DatabaseReference, setId: Int, completion: (Int) -> Unit) {
        val setPrefix = "set${setId}p"
        var sum = 0

        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach { setIdSnapshot ->
                    if (setIdSnapshot.key?.startsWith(setPrefix) == true) {
                        val value = setIdSnapshot.getValue(String::class.java)
                        if (!value.isNullOrBlank()) {
                            sum += value.toIntOrNull() ?: 0
                        }
                    }
                }
                completion(sum)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                completion(0)
            }
        })
    }
    // funkcja usuwajaca punkt
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

    private fun addZeros(a: Int) : String
    {
        val resultString: String = if(a<10){
            //"00$a" jezeli dodane 100
            "00$a"
        } else if(a<100) {
            "0$a"
        }else a.toString()
        return resultString
    }
}