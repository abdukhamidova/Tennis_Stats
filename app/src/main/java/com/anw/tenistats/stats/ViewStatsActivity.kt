package com.anw.tenistats.stats

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.anw.tenistats.R
import com.anw.tenistats.databinding.ActivityViewStatsBinding
import com.anw.tenistats.matchplay.getGoldenDrawable
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ViewStatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewStatsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityViewStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth=FirebaseAuth.getInstance()

    //MENU
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
        backButton.visibility = View.GONE

        val userEmail = firebaseAuth.currentUser?.email.toString()
        val userEmailView = headerView.findViewById<TextView>(R.id.textViewUserEmail)
        if(userEmail.isNotEmpty()) {
            userEmailView.text = userEmail
        }else {
            userEmailView.text = resources.getString(R.string.user_email)
        }
    //MENU

        val app = application as StatsClass
    //pola w tabeli wyniku
        val player1 = binding.textviewPlayer1Stats
        val player2 = binding.textviewPlayer2Stats
        val serve1 = binding.textViewServe1Stats
        val serve2 = binding.textViewServe2Stats
        val set1p1 = binding.textViewP1Set1Stats
        val set2p1 = binding.textViewP1Set2Stats
        val set3p1 = binding.textViewP1Set3Stats
        val set1p2 = binding.textViewP2Set1Stats
        val set2p2 = binding.textViewP2Set2Stats
        val set3p2 = binding.textViewP2Set3Stats
        val pkt1 = binding.textViewPlayer1PktStats
        val pkt2 = binding.textViewPlayer2PktStats


        val matchID = intent.getStringExtra("matchID").toString()
        val user = firebaseAuth.currentUser?.uid
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString()).child("Matches").child(matchID)

    //ustawienie wyniku w tabeli
        setscore(player1,player2,serve1,serve2,set1p1,set2p1,set3p1,set1p2,set2p2,set3p2,pkt1,pkt2)

    // Odbierz datę meczu w formacie milisekund z poprzedniej aktywności
        val matchDateInMillis = intent.getLongExtra("matchDateInMillis", 0L)

    //pola nad statystykami
        val player1name = binding.player1name
        val player2name = binding.player2name
        database.child("player1").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val player1Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            player1name.text = player1Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }

        database.child("player2").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player2" z bazy danych
            val player2Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            player2name.text = player2Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }

        var selectedSetNumber = 0

    //lista rozwijana STATS & HIST
        val spinner1: Spinner = binding.spinnerStats
        val items1 = arrayOf("STATISTICS","HISTORY")
        val adapter1 = ArrayAdapter(this, R.layout.spinner_item_stats_left_base,items1)
        adapter1.setDropDownViewResource(R.layout.spinner_item_stats_left)
        spinner1.adapter = adapter1

        spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when(position)
                {
                    1 -> {
                        val intent = Intent(this@ViewStatsActivity, ViewHistoryActivity::class.java)
                        intent.putExtra("matchDateInMillis",matchDateInMillis)
                        startActivity(intent)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    //lista rozwijana STATS & HIST

    //lista rozwijana TOTAL,RETURN,WINNERS,FORCED ERR.,UNFOR. ERR.
        val spinner2: Spinner = binding.spinnerStatsCategory
        val items2 = arrayOf("TOTAL","RETURN","WINNERS","FORCED ERRORS","UNFORCED ERRORS")
        val adapter2 = ArrayAdapter(this, R.layout.spinner_item_stats_right_base,items2)
        adapter2.setDropDownViewResource(R.layout.spinner_item_stats_right)
        spinner2.adapter = adapter2

        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                setAppValues(app,selectedSetNumber,player1name,player2name,spinner2.selectedItemPosition)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    //lista rozwijana TOTAL,RETURN,WINNERS,FORCED ERR.,UNFOR. ERR.

        binding.buttonAllStats.setOnClickListener {
            selectedSetNumber = 0
            //zmiana kolorow textViews ALL, SET1,SET2,SET3
            binding.buttonAllStats.setBackgroundResource(R.drawable.rectangle_button)
            binding.buttonAllStats.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.buttonSet1Stats.setBackgroundResource(R.drawable.rec_btn_not_selected)
            binding.buttonSet1Stats.setTextColor(ContextCompat.getColor(this,
                R.color.general_text_color
            ))
            binding.buttonSet2Stats.setBackgroundResource(R.drawable.rec_btn_not_selected)
            binding.buttonSet2Stats.setTextColor(ContextCompat.getColor(this,
                R.color.general_text_color
            ))
            binding.buttonSet3Stats.setBackgroundResource(R.drawable.rec_btn_not_selected)
            binding.buttonSet3Stats.setTextColor(ContextCompat.getColor(this,
                R.color.general_text_color
            ))
            //ustawienie statystyk ALL
            setAppValues(app,selectedSetNumber,player1name,player2name,spinner2.selectedItemPosition)
        }
        binding.buttonSet1Stats.setOnClickListener {
            Log.d("ViewStatsActivity", "Button clicked")
            selectedSetNumber = 1
            //zmiana kolorow textViews ALL, SET1,SET2,SET3
            binding.buttonAllStats.setBackgroundResource(R.drawable.rec_btn_not_selected)
            binding.buttonAllStats.setTextColor(ContextCompat.getColor(this,
                R.color.general_text_color
            ))
            binding.buttonSet1Stats.setBackgroundResource(R.drawable.rectangle_button)
            binding.buttonSet1Stats.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.buttonSet2Stats.setBackgroundResource(R.drawable.rec_btn_not_selected)
            binding.buttonSet2Stats.setTextColor(ContextCompat.getColor(this,
                R.color.general_text_color
            ))
            binding.buttonSet3Stats.setBackgroundResource(R.drawable.rec_btn_not_selected)
            binding.buttonSet3Stats.setTextColor(ContextCompat.getColor(this,
                R.color.general_text_color
            ))
            //ustawienie statystyk SET1
            setAppValues(app,selectedSetNumber,player1name,player2name,spinner2.selectedItemPosition)
        }
        binding.buttonSet2Stats.setOnClickListener {
            if (set2p1.text != ""){
                selectedSetNumber = 2
                //zmiana kolorow textViews ALL, SET1,SET2,SET3
                binding.buttonAllStats.setBackgroundResource(R.drawable.rec_btn_not_selected)
                binding.buttonAllStats.setTextColor(ContextCompat.getColor(this,
                    R.color.general_text_color
                ))
                binding.buttonSet1Stats.setBackgroundResource(R.drawable.rec_btn_not_selected)
                binding.buttonSet1Stats.setTextColor(ContextCompat.getColor(this,
                    R.color.general_text_color
                ))
                binding.buttonSet2Stats.setBackgroundResource(R.drawable.rectangle_button)
                binding.buttonSet2Stats.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.buttonSet3Stats.setBackgroundResource(R.drawable.rec_btn_not_selected)
                binding.buttonSet3Stats.setTextColor(ContextCompat.getColor(this,
                    R.color.general_text_color
                ))
                //ustawienie statystyk SET2
                setAppValues(app, selectedSetNumber, player1name, player2name,spinner2.selectedItemPosition)
            }
            else{
                Toast.makeText(this, "Set2 does not exist", Toast.LENGTH_SHORT).show()
            }
        }
        binding.buttonSet3Stats.setOnClickListener {
            if(set3p1.text!=""){
                selectedSetNumber = 3
                //zmiana kolorow textViews ALL, SET1,SET2,SET3
                binding.buttonAllStats.setBackgroundResource(R.drawable.rec_btn_not_selected)
                binding.buttonAllStats.setTextColor(ContextCompat.getColor(this,
                    R.color.general_text_color
                ))
                binding.buttonSet1Stats.setBackgroundResource(R.drawable.rec_btn_not_selected)
                binding.buttonSet1Stats.setTextColor(ContextCompat.getColor(this,
                    R.color.general_text_color
                ))
                binding.buttonSet2Stats.setBackgroundResource(R.drawable.rec_btn_not_selected)
                binding.buttonSet2Stats.setTextColor(ContextCompat.getColor(this,
                    R.color.general_text_color
                ))
                binding.buttonSet3Stats.setBackgroundResource(R.drawable.rectangle_button)
                binding.buttonSet3Stats.setTextColor(ContextCompat.getColor(this, R.color.white))
                //ustawienie statystyk SET3
                setAppValues(app,selectedSetNumber,player1name,player2name,spinner2.selectedItemPosition)
            }
            else{
                Toast.makeText(this, "Set3 does not exist", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setscore(player1: TextView, player2: TextView, serve1: TextView, serve2: TextView,
                         set1p1: TextView, set2p1: TextView, set3p1: TextView,
                         set1p2: TextView, set2p2: TextView, set3p2: TextView,
                         pkt1: TextView, pkt2: TextView) {
        lifecycleScope.launch(Dispatchers.IO) {
            val dataSnapshot = database.get().await()

            withContext(Dispatchers.Main) {
                player1.text = dataSnapshot.child("player1").getValue(String::class.java) ?: ""
                player2.text = dataSnapshot.child("player2").getValue(String::class.java) ?: ""
                set1p1.text = dataSnapshot.child("set1p1").getValue(String::class.java) ?: ""
                set2p1.text = dataSnapshot.child("set2p1").getValue(String::class.java) ?: ""
                set3p1.text = dataSnapshot.child("set3p1").getValue(String::class.java) ?: ""
                set1p2.text = dataSnapshot.child("set1p2").getValue(String::class.java) ?: ""
                set2p2.text = dataSnapshot.child("set2p2").getValue(String::class.java) ?: ""
                set3p2.text = dataSnapshot.child("set3p2").getValue(String::class.java) ?: ""
                pkt1.text = dataSnapshot.child("pkt1").getValue(String::class.java) ?: ""
                pkt2.text = dataSnapshot.child("pkt2").getValue(String::class.java) ?: ""

                // Ustawienie grafiki lauru lub serwisu
                if(dataSnapshot.child("winner").exists()) {
                    val winner = dataSnapshot.child("winner").getValue(String::class.java)
                    val goldenLaurel =
                        getGoldenDrawable(applicationContext, R.drawable.icon_laurel3)
                    if (winner == player1.text.toString()) {
                        serve1.visibility = View.VISIBLE
                        serve2.visibility = View.INVISIBLE
                        serve1.setCompoundDrawablesWithIntrinsicBounds(
                            goldenLaurel,
                            null,
                            null,
                            null
                        )
                    } else {
                        serve1.visibility = View.INVISIBLE
                        serve2.visibility = View.VISIBLE
                        serve2.setCompoundDrawablesWithIntrinsicBounds(
                            goldenLaurel,
                            null,
                            null,
                            null
                        )
                    }
                }
                else{
                    // Pobranie wartości "LastServePlayer" z bazy danych
                    val lastserve = dataSnapshot.child("LastServePlayer").getValue(String::class.java)
                    serve1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ball, 0, 0, 0)
                    serve2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ball, 0, 0, 0)
                    // Ustawienie wartości w TextView
                    if(lastserve==player1.text.toString()){
                        serve1.visibility = View.VISIBLE
                        serve2.visibility = View.INVISIBLE
                    }
                    else{
                        serve1.visibility = View.INVISIBLE
                        serve2.visibility = View.VISIBLE
                    }
                }
            }
        }
    }


    private suspend fun countForPlayer(setNumber: Int, playerName: String, shotName: String, placeName: String, sideName: String): Int {
        return withContext(Dispatchers.IO) {
            var result = 0
            if (setNumber != 0) {
                val setSnapshot = database.child("set $setNumber").get().await()
                for (gameSnapshot in setSnapshot.children) {
                    for (pointSnapshot in gameSnapshot.children) {
                        val co = pointSnapshot.child("co").getValue(String::class.java)
                        val czym = pointSnapshot.child("czym").getValue(String::class.java)
                        val gdzie = pointSnapshot.child("gdzie").getValue(String::class.java)
                        val player = pointSnapshot.child("kto").getValue(String::class.java)
                        if (player == playerName && co == shotName && gdzie == placeName && czym == sideName) {
                            result++
                        }
                    }
                }
            } else {
                val dataSnapshot = database.get().await()
                for (setSnapshot in dataSnapshot.children) {
                    for (gameSnapshot in setSnapshot.children) {
                        for (pointSnapshot in gameSnapshot.children) {
                            val co = pointSnapshot.child("co").getValue(String::class.java)
                            val czym = pointSnapshot.child("czym").getValue(String::class.java)
                            val gdzie = pointSnapshot.child("gdzie").getValue(String::class.java)
                            val player = pointSnapshot.child("kto").getValue(String::class.java)
                            if (player == playerName && co == shotName && gdzie == placeName && czym == sideName) {
                                result++
                            }
                        }
                    }
                }
            }
            result
        }
    }

    private suspend fun countServeForPlayer(setNumber: Int, playerName: String, serveNumber: Int): Int {
        return withContext(Dispatchers.IO) {
            var result = 0
            if (setNumber != 0) {
                val setSnapshot = database.child("set $setNumber").get().await()
                for (gameSnapshot in setSnapshot.children) {
                    for (pointSnapshot in gameSnapshot.children) {
                        val servePlayer = pointSnapshot.child("servePlayer").getValue(String::class.java)
                        val serwis = pointSnapshot.child("serwis").getValue(Int::class.java)
                        if (servePlayer == playerName && serwis == serveNumber) {
                            result++
                        }
                    }
                }
            } else {
                val dataSnapshot = database.get().await()
                for (setSnapshot in dataSnapshot.children) {
                    for (gameSnapshot in setSnapshot.children) {
                        for (pointSnapshot in gameSnapshot.children) {
                            val servePlayer = pointSnapshot.child("servePlayer").getValue(String::class.java)
                            val serwis = pointSnapshot.child("serwis").getValue(Int::class.java)
                            if (servePlayer == playerName && serwis == serveNumber) {
                                result++
                            }
                        }
                    }
                }
            }
            result
        }
    }

    fun setAppValues(app: StatsClass, setNumber: Int, player1name: TextView, player2name: TextView, position: Int){
        refreshNumbers(app,position)
        lifecycleScope.launch {
            when(position){
                0 -> {
                    binding.totalPointsLayout.visibility = View.VISIBLE;
                    binding.returnLayout.visibility = View.GONE;
                    binding.winnersLayout.visibility = View.GONE;
                    binding.forcedErrorsLayout.visibility = View.GONE;
                    binding.unforcedErrorsLayout.visibility = View.GONE;
                } //TOTAL
                1 -> {
                    binding.totalPointsLayout.visibility = View.GONE;
                    binding.returnLayout.visibility = View.VISIBLE;
                    binding.winnersLayout.visibility = View.GONE;
                    binding.forcedErrorsLayout.visibility = View.GONE;
                    binding.unforcedErrorsLayout.visibility = View.GONE;
                } //RETURN
                2 -> {
                    binding.totalPointsLayout.visibility = View.GONE;
                    binding.returnLayout.visibility = View.GONE;
                    binding.winnersLayout.visibility = View.VISIBLE;
                    binding.forcedErrorsLayout.visibility = View.GONE;
                    binding.unforcedErrorsLayout.visibility = View.GONE;
                } //WINNERS
                3 -> {
                    binding.totalPointsLayout.visibility = View.GONE;
                    binding.returnLayout.visibility = View.GONE;
                    binding.winnersLayout.visibility = View.GONE;
                    binding.forcedErrorsLayout.visibility = View.VISIBLE;
                    binding.unforcedErrorsLayout.visibility = View.GONE;
                } //FORCED ERRORS
                4 -> {
                    binding.totalPointsLayout.visibility = View.GONE;
                    binding.returnLayout.visibility = View.GONE;
                    binding.winnersLayout.visibility = View.GONE;
                    binding.forcedErrorsLayout.visibility = View.GONE;
                    binding.unforcedErrorsLayout.visibility = View.VISIBLE;
                } //UNFORCED ERRORS
            }
            val ace1Deferred =
                async { countForPlayer(setNumber, player1name.text.toString(), "Ace", "", "") }
            val ace2Deferred =
                async { countForPlayer(setNumber, player2name.text.toString(), "Ace", "", "") }
            val doubleFault1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Double Fault",
                    "",
                    ""
                )
            }
            val doubleFault2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Double Fault",
                    "",
                    ""
                )
            }

            app.ace1 = ace1Deferred.await().also { app.totalpoints1 += it }
            app.ace2 = ace2Deferred.await().also { app.totalpoints2 += it }
            app.doublefault1 = doubleFault1Deferred.await().also { app.totalpoints2 += it }
            app.doublefault2 = doubleFault2Deferred.await().also { app.totalpoints1 += it }

            val firstServeIn1Deferred =
                async { countServeForPlayer(setNumber, player1name.text.toString(), 1) }
            val firstServeIn2Deferred =
                async { countServeForPlayer(setNumber, player2name.text.toString(), 1) }

            app.firstservein1 = firstServeIn1Deferred.await()
            app.firstservein2 = firstServeIn2Deferred.await()
            app.secondservein1 =
                async { countServeForPlayer(setNumber, player1name.text.toString(), 2) }.await()
            app.secondservein2 =
                async { countServeForPlayer(setNumber, player2name.text.toString(), 2) }.await()

            app.secondserve1 = app.secondservein1 + app.doublefault1
            app.secondserve2 = app.secondservein2 + app.doublefault2
            app.firstserve1 = app.firstservein1 + app.secondserve1
            app.firstserve2 = app.firstservein2 + app.secondserve2

            val returnWinnerFH1 = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Winner",
                    "Return",
                    "Forehand"
                )
            }
            val returnWinnerFH2 = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Winner",
                    "Return",
                    "Forehand"
                )
            }

            app.returnwinnerFH1 = returnWinnerFH1.await().also { app.totalpoints1 += it }
            app.returnwinnerFH2 = returnWinnerFH2.await().also { app.totalpoints2 += it }

            val returnWinnerBH1 = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Winner",
                    "Return",
                    "Backhand"
                )
            }
            val returnWinnerBH2 = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Winner",
                    "Return",
                    "Backhand"
                )
            }

            app.returnwinnerBH1 = returnWinnerBH1.await().also { app.totalpoints1 += it }
            app.returnwinnerBH2 = returnWinnerBH2.await().also { app.totalpoints2 += it }

            val returnErrorFH1 = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Error",
                    "Return",
                    "Forehand"
                )
            }
            val returnErrorFH2 = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Error",
                    "Return",
                    "Forehand"
                )
            }

            app.returnerrorFH1 = returnErrorFH1.await().also { app.totalpoints2 += it }
            app.returnerrorFH2 = returnErrorFH2.await().also { app.totalpoints1 += it }

            val returnErrorBH1 = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Error",
                    "Return",
                    "Backhand"
                )
            }
            val returnErrorBH2 = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Error",
                    "Return",
                    "Backhand"
                )
            }

            app.returnerrorBH1 = returnErrorBH1.await().also { app.totalpoints2 += it }
            app.returnerrorBH2 = returnErrorBH2.await().also { app.totalpoints1 += it }

            val winnerGroundFH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Winner",
                    "Ground",
                    "Forehand"
                )
            }
            val winnerGroundFH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Winner",
                    "Ground",
                    "Forehand"
                )
            }

            app.winnergroundFH1 = winnerGroundFH1Deferred.await().also { app.totalpoints1 += it }
            app.winnergroundFH2 = winnerGroundFH2Deferred.await().also { app.totalpoints2 += it }

            val winnerGroundBH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Winner",
                    "Ground",
                    "Backhand"
                )
            }
            val winnerGroundBH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Winner",
                    "Ground",
                    "Backhand"
                )
            }

            app.winnergroundBH1 = winnerGroundBH1Deferred.await().also { app.totalpoints1 += it }
            app.winnergroundBH2 = winnerGroundBH2Deferred.await().also { app.totalpoints2 += it }

            val winnerSliceFH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Winner",
                    "Slice",
                    "Forehand"
                )
            }
            val winnerSliceFH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Winner",
                    "Slice",
                    "Forehand"
                )
            }

            app.winnersliceFH1 = winnerSliceFH1Deferred.await().also { app.totalpoints1 += it }
            app.winnersliceFH2 = winnerSliceFH2Deferred.await().also { app.totalpoints2 += it }

            val winnerSliceBH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Winner",
                    "Slice",
                    "Backhand"
                )
            }
            val winnerSliceBH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Winner",
                    "Slice",
                    "Backhand"
                )
            }

            app.winnersliceBH1 = winnerSliceBH1Deferred.await().also { app.totalpoints1 += it }
            app.winnersliceBH2 = winnerSliceBH2Deferred.await().also { app.totalpoints2 += it }

            val winnerSmashFH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Winner",
                    "Smash",
                    "Forehand"
                )
            }
            val winnerSmashFH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Winner",
                    "Smash",
                    "Forehand"
                )
            }

            app.winnersmashFH1 = winnerSmashFH1Deferred.await().also { app.totalpoints1 += it }
            app.winnersmashFH2 = winnerSmashFH2Deferred.await().also { app.totalpoints2 += it }

            val winnerSmashBH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Winner",
                    "Smash",
                    "Backhand"
                )
            }
            val winnerSmashBH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Winner",
                    "Smash",
                    "Backhand"
                )
            }

            app.winnersmashBH1 = winnerSmashBH1Deferred.await().also { app.totalpoints1 += it }
            app.winnersmashBH2 = winnerSmashBH2Deferred.await().also { app.totalpoints2 += it }

            val winnerVolleyFH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Winner",
                    "Volley",
                    "Forehand"
                )
            }
            val winnerVolleyFH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Winner",
                    "Volley",
                    "Forehand"
                )
            }

            app.winnervolleyFH1 = winnerVolleyFH1Deferred.await().also { app.totalpoints1 += it }
            app.winnervolleyFH2 = winnerVolleyFH2Deferred.await().also { app.totalpoints2 += it }

            val winnerVolleyBH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Winner",
                    "Volley",
                    "Backhand"
                )
            }
            val winnerVolleyBH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Winner",
                    "Volley",
                    "Backhand"
                )
            }

            app.winnervolleyBH1 = winnerVolleyBH1Deferred.await().also { app.totalpoints1 += it }
            app.winnervolleyBH2 = winnerVolleyBH2Deferred.await().also { app.totalpoints2 += it }

            val winnerDropshotFH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Winner",
                    "Dropshot",
                    "Forehand"
                )
            }
            val winnerDropshotFH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Winner",
                    "Dropshot",
                    "Forehand"
                )
            }

            app.winnerdropshotFH1 =
                winnerDropshotFH1Deferred.await().also { app.totalpoints1 += it }
            app.winnerdropshotFH2 =
                winnerDropshotFH2Deferred.await().also { app.totalpoints2 += it }

            val winnerDropshotBH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Winner",
                    "Dropshot",
                    "Backhand"
                )
            }
            val winnerDropshotBH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Winner",
                    "Dropshot",
                    "Backhand"
                )
            }

            app.winnerdropshotBH1 =
                winnerDropshotBH1Deferred.await().also { app.totalpoints1 += it }
            app.winnerdropshotBH2 =
                winnerDropshotBH2Deferred.await().also { app.totalpoints2 += it }

            val winnerLobFH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Winner",
                    "Lob",
                    "Forehand"
                )
            }
            val winnerLobFH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Winner",
                    "Lob",
                    "Forehand"
                )
            }

            app.winnerlobFH1 = winnerLobFH1Deferred.await().also { app.totalpoints1 += it }
            app.winnerlobFH2 = winnerLobFH2Deferred.await().also { app.totalpoints2 += it }

            val winnerLobBH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Winner",
                    "Lob",
                    "Backhand"
                )
            }
            val winnerLobBH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Winner",
                    "Lob",
                    "Backhand"
                )
            }

            app.winnerlobBH1 = winnerLobBH1Deferred.await().also { app.totalpoints1 += it }
            app.winnerlobBH2 = winnerLobBH2Deferred.await().also { app.totalpoints2 += it }

            val forcedErrorFH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Forced Error",
                    "Ground",
                    "Forehand"
                )
            }
            val forcedErrorFH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Forced Error",
                    "Ground",
                    "Forehand"
                )
            }

            app.forcederrorgroundFH1 =
                forcedErrorFH1Deferred.await().also { app.totalpoints2 += it }
            app.forcederrorgroundFH2 =
                forcedErrorFH2Deferred.await().also { app.totalpoints1 += it }

            val forcedErrorBH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Forced Error",
                    "Ground",
                    "Backhand"
                )
            }
            val forcedErrorBH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Forced Error",
                    "Ground",
                    "Backhand"
                )
            }

            app.forcederrorgroundBH1 =
                forcedErrorBH1Deferred.await().also { app.totalpoints2 += it }
            app.forcederrorgroundBH2 =
                forcedErrorBH2Deferred.await().also { app.totalpoints1 += it }

            val forcedErrorSliceFH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Forced Error",
                    "Slice",
                    "Forehand"
                )
            }
            val forcedErrorSliceFH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Forced Error",
                    "Slice",
                    "Forehand"
                )
            }

            app.forcederrorsliceFH1 =
                forcedErrorSliceFH1Deferred.await().also { app.totalpoints2 += it }
            app.forcederrorsliceFH2 =
                forcedErrorSliceFH2Deferred.await().also { app.totalpoints1 += it }

            val forcedErrorSliceBH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Forced Error",
                    "Slice",
                    "Backhand"
                )
            }
            val forcedErrorSliceBH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Forced Error",
                    "Slice",
                    "Backhand"
                )
            }

            app.forcederrorsliceBH1 =
                forcedErrorSliceBH1Deferred.await().also { app.totalpoints2 += it }
            app.forcederrorsliceBH2 =
                forcedErrorSliceBH2Deferred.await().also { app.totalpoints1 += it }

            val forcedErrorSmashFH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Forced Error",
                    "Smash",
                    "Forehand"
                )
            }
            val forcedErrorSmashFH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Forced Error",
                    "Smash",
                    "Forehand"
                )
            }

            app.forcederrorsmashFH1 =
                forcedErrorSmashFH1Deferred.await().also { app.totalpoints2 += it }
            app.forcederrorsmashFH2 =
                forcedErrorSmashFH2Deferred.await().also { app.totalpoints1 += it }

            val forcedErrorSmashBH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Forced Error",
                    "Smash",
                    "Backhand"
                )
            }
            val forcedErrorSmashBH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Forced Error",
                    "Smash",
                    "Backhand"
                )
            }

            app.forcederrorsmashBH1 =
                forcedErrorSmashBH1Deferred.await().also { app.totalpoints2 += it }
            app.forcederrorsmashBH2 =
                forcedErrorSmashBH2Deferred.await().also { app.totalpoints1 += it }

            val forcedErrorVolleyFH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Forced Error",
                    "Volley",
                    "Forehand"
                )
            }
            val forcedErrorVolleyFH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Forced Error",
                    "Volley",
                    "Forehand"
                )
            }

            app.forcederrorvolleyFH1 =
                forcedErrorVolleyFH1Deferred.await().also { app.totalpoints2 += it }
            app.forcederrorvolleyFH2 =
                forcedErrorVolleyFH2Deferred.await().also { app.totalpoints1 += it }

            val forcedErrorVolleyBH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Forced Error",
                    "Volley",
                    "Backhand"
                )
            }
            val forcedErrorVolleyBH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Forced Error",
                    "Volley",
                    "Backhand"
                )
            }

            app.forcederrorvolleyBH1 =
                forcedErrorVolleyBH1Deferred.await().also { app.totalpoints2 += it }
            app.forcederrorvolleyBH2 =
                forcedErrorVolleyBH2Deferred.await().also { app.totalpoints1 += it }

            val forcedErrorDropshotFH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Forced Error",
                    "Dropshot",
                    "Forehand"
                )
            }
            val forcedErrorDropshotFH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Forced Error",
                    "Dropshot",
                    "Forehand"
                )
            }

            app.forcederrordropshotFH1 =
                forcedErrorDropshotFH1Deferred.await().also { app.totalpoints2 += it }
            app.forcederrordropshotFH2 =
                forcedErrorDropshotFH2Deferred.await().also { app.totalpoints1 += it }

            val forcedErrorDropshotBH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Forced Error",
                    "Dropshot",
                    "Backhand"
                )
            }
            val forcedErrorDropshotBH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Forced Error",
                    "Dropshot",
                    "Backhand"
                )
            }

            app.forcederrordropshotBH1 =
                forcedErrorDropshotBH1Deferred.await().also { app.totalpoints2 += it }
            app.forcederrordropshotBH2 =
                forcedErrorDropshotBH2Deferred.await().also { app.totalpoints1 += it }

            val forcedErrorLobFH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Forced Error",
                    "Lob",
                    "Forehand"
                )
            }
            val forcedErrorLobFH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Forced Error",
                    "Lob",
                    "Forehand"
                )
            }

            app.forcederrorlobFH1 =
                forcedErrorLobFH1Deferred.await().also { app.totalpoints2 += it }
            app.forcederrorlobFH2 =
                forcedErrorLobFH2Deferred.await().also { app.totalpoints1 += it }

            val forcedErrorLobBH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Forced Error",
                    "Lob",
                    "Backhand"
                )
            }
            val forcedErrorLobBH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Forced Error",
                    "Lob",
                    "Backhand"
                )
            }

            app.forcederrorlobBH1 =
                forcedErrorLobBH1Deferred.await().also { app.totalpoints2 += it }
            app.forcederrorlobBH2 =
                forcedErrorLobBH2Deferred.await().also { app.totalpoints1 += it }

            val unforcedErrorFH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Unforced Error",
                    "Ground",
                    "Forehand"
                )
            }
            val unforcedErrorFH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Unforced Error",
                    "Ground",
                    "Forehand"
                )
            }

            app.unforcederrorgroundFH1 =
                unforcedErrorFH1Deferred.await().also { app.totalpoints2 += it }
            app.unforcederrorgroundFH2 =
                unforcedErrorFH2Deferred.await().also { app.totalpoints1 += it }

            val unforcedErrorBH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Unforced Error",
                    "Ground",
                    "Backhand"
                )
            }
            val unforcedErrorBH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Unforced Error",
                    "Ground",
                    "Backhand"
                )
            }

            app.unforcederrorgroundBH1 =
                unforcedErrorBH1Deferred.await().also { app.totalpoints2 += it }
            app.unforcederrorgroundBH2 =
                unforcedErrorBH2Deferred.await().also { app.totalpoints1 += it }

            val unforcedErrorSliceFH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Unforced Error",
                    "Slice",
                    "Forehand"
                )
            }
            val unforcedErrorSliceFH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Unforced Error",
                    "Slice",
                    "Forehand"
                )
            }

            app.unforcederrorsliceFH1 =
                unforcedErrorSliceFH1Deferred.await().also { app.totalpoints2 += it }
            app.unforcederrorsliceFH2 =
                unforcedErrorSliceFH2Deferred.await().also { app.totalpoints1 += it }

            val unforcedErrorSliceBH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Unforced Error",
                    "Slice",
                    "Backhand"
                )
            }
            val unforcedErrorSliceBH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Unforced Error",
                    "Slice",
                    "Backhand"
                )
            }

            app.unforcederrorsliceBH1 =
                unforcedErrorSliceBH1Deferred.await().also { app.totalpoints2 += it }
            app.unforcederrorsliceBH2 =
                unforcedErrorSliceBH2Deferred.await().also { app.totalpoints1 += it }

            val unforcedErrorSmashFH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Unforced Error",
                    "Smash",
                    "Forehand"
                )
            }
            val unforcedErrorSmashFH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Unforced Error",
                    "Smash",
                    "Forehand"
                )
            }

            app.unforcederrorsmashFH1 =
                unforcedErrorSmashFH1Deferred.await().also { app.totalpoints2 += it }
            app.unforcederrorsmashFH2 =
                unforcedErrorSmashFH2Deferred.await().also { app.totalpoints1 += it }

            val unforcedErrorSmashBH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Unforced Error",
                    "Smash",
                    "Backhand"
                )
            }
            val unforcedErrorSmashBH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Unforced Error",
                    "Smash",
                    "Backhand"
                )
            }

            app.unforcederrorsmashBH1 =
                unforcedErrorSmashBH1Deferred.await().also { app.totalpoints2 += it }
            app.unforcederrorsmashBH2 =
                unforcedErrorSmashBH2Deferred.await().also { app.totalpoints1 += it }

            val unforcedErrorVolleyFH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Unforced Error",
                    "Volley",
                    "Forehand"
                )
            }
            val unforcedErrorVolleyFH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Unforced Error",
                    "Volley",
                    "Forehand"
                )
            }

            app.unforcederrorvolleyFH1 =
                unforcedErrorVolleyFH1Deferred.await().also { app.totalpoints2 += it }
            app.unforcederrorvolleyFH2 =
                unforcedErrorVolleyFH2Deferred.await().also { app.totalpoints1 += it }

            val unforcedErrorVolleyBH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Unforced Error",
                    "Volley",
                    "Backhand"
                )
            }
            val unforcedErrorVolleyBH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Unforced Error",
                    "Volley",
                    "Backhand"
                )
            }

            app.unforcederrorvolleyBH1 =
                unforcedErrorVolleyBH1Deferred.await().also { app.totalpoints2 += it }
            app.unforcederrorvolleyBH2 =
                unforcedErrorVolleyBH2Deferred.await().also { app.totalpoints1 += it }

            val unforcedErrorDropshotFH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Unforced Error",
                    "Dropshot",
                    "Forehand"
                )
            }
            val unforcedErrorDropshotFH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Unforced Error",
                    "Dropshot",
                    "Forehand"
                )
            }

            app.unforcederrordropshotFH1 =
                unforcedErrorDropshotFH1Deferred.await().also { app.totalpoints2 += it }
            app.unforcederrordropshotFH2 =
                unforcedErrorDropshotFH2Deferred.await().also { app.totalpoints1 += it }

            val unforcedErrorDropshotBH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Unforced Error",
                    "Dropshot",
                    "Backhand"
                )
            }
            val unforcedErrorDropshotBH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Unforced Error",
                    "Dropshot",
                    "Backhand"
                )
            }

            app.unforcederrordropshotBH1 =
                unforcedErrorDropshotBH1Deferred.await().also { app.totalpoints2 += it }
            app.unforcederrordropshotBH2 =
                unforcedErrorDropshotBH2Deferred.await().also { app.totalpoints1 += it }

            val unforcedErrorLobFH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Unforced Error",
                    "Lob",
                    "Forehand"
                )
            }
            val unforcedErrorLobFH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Unforced Error",
                    "Lob",
                    "Forehand"
                )
            }

            app.unforcederrorlobFH1 =
                unforcedErrorLobFH1Deferred.await().also { app.totalpoints2 += it }
            app.unforcederrorlobFH2 =
                unforcedErrorLobFH2Deferred.await().also { app.totalpoints1 += it }

            val unforcedErrorLobBH1Deferred = async {
                countForPlayer(
                    setNumber,
                    player1name.text.toString(),
                    "Unforced Error",
                    "Lob",
                    "Backhand"
                )
            }
            val unforcedErrorLobBH2Deferred = async {
                countForPlayer(
                    setNumber,
                    player2name.text.toString(),
                    "Unforced Error",
                    "Lob",
                    "Backhand"
                )
            }

            app.unforcederrorlobBH1 =
                unforcedErrorLobBH1Deferred.await().also { app.totalpoints2 += it }
            app.unforcederrorlobBH2 =
                unforcedErrorLobBH2Deferred.await().also { app.totalpoints1 += it }
            setTableValue(app,binding.spinnerStatsCategory.selectedItemPosition)
        }
    }

    fun setTableValue(app: StatsClass, position: Int){
        val scale = resources.displayMetrics.density
        when(position){
            0 -> {
                binding.totalPlayer1.text = app.totalpoints1.toString()
                binding.totalPlayer2.text = app.totalpoints2.toString()

                //kod do ustawienia wykresow totalpoints
                if(app.totalpoints1>app.totalpoints2){
                    binding.graphTotalPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.totalpoints2.toDouble() * 100 /app.totalpoints1.toDouble()).toInt()
                    binding.graphTotalPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }
                else{
                    binding.graphTotalPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.totalpoints1.toDouble() * 100 / app.totalpoints2.toDouble()).toInt()
                    binding.graphTotalPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }

                app.totalpoints1 = 0
                app.totalpoints2 = 0

                binding.acePlayer1.text = app.ace1.toString()
                binding.acePlayer2.text = app.ace2.toString()
                if(app.ace1==app.ace2 && app.ace1==0){
                    binding.graphAcePlayer1.layoutParams.width=0
                    binding.graphAcePlayer2.layoutParams.width=0
                }
                else if(app.ace1>app.ace2){
                    binding.graphAcePlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.ace2.toDouble() * 100 /app.ace1.toDouble()).toInt()
                    binding.graphAcePlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }
                else{
                    binding.graphAcePlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.ace1.toDouble() * 100 / app.ace2.toDouble()).toInt()
                    binding.graphAcePlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }

                binding.doublePlayer1.text = app.doublefault1.toString()
                binding.doublePlayer2.text = app.doublefault2.toString()
                if(app.doublefault1==app.doublefault2 && app.doublefault1==0){
                    binding.graphDoubleFaultPlayer1.layoutParams.width=0
                    binding.graphDoubleFaultPlayer2.layoutParams.width=0
                }
                else if(app.doublefault1>app.doublefault2){
                    binding.graphDoubleFaultPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.doublefault2.toDouble() * 100 / app.doublefault1.toDouble()).toInt()
                    binding.graphDoubleFaultPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }
                else{
                    binding.graphDoubleFaultPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.doublefault1.toDouble() * 100 / app.doublefault2.toDouble()).toInt()
                    binding.graphDoubleFaultPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }

                val firstServeIn1 = app.firstservein1
                val allFirstServe1 = app.firstserve1
                val firstServePercentage1 = (firstServeIn1.toDouble() / allFirstServe1.toDouble() * 100).toInt()
                binding.firstinPlayer1.text = "$firstServeIn1/$allFirstServe1 ($firstServePercentage1%)"

                val firstServeIn2 = app.firstservein2
                val allFirstServe2 = app.firstserve2
                val firstServePercentage2 = (firstServeIn2.toDouble() / allFirstServe2.toDouble() * 100).toInt()
                binding.firstinPlayer2.text = "$firstServeIn2/$allFirstServe2 ($firstServePercentage2%)"

                binding.graphFirstPlayer1.layoutParams.width=(firstServePercentage1 * scale + 0.5f).toInt()
                binding.graphFirstPlayer2.layoutParams.width=(firstServePercentage2 * scale + 0.5f).toInt()

                val secondServeIn1 = app.secondservein1
                val allSecondServe1 = app.secondserve1
                val secondServePercentage1 = (secondServeIn1.toDouble() / allSecondServe1.toDouble() * 100).toInt()
                binding.secondinPlayer1.text = "$secondServeIn1/$allSecondServe1 ($secondServePercentage1%)"

                val secondServeIn2 = app.secondservein2
                val allSecondServe2 = app.secondserve2
                val secondServePercentage2 = (secondServeIn2.toDouble() / allSecondServe2.toDouble() * 100).toInt()
                binding.secondinPlayer2.text = "$secondServeIn2/$allSecondServe2 ($secondServePercentage2%)"

                binding.graphSecondPlayer1.layoutParams.width=(secondServePercentage1 * scale + 0.5f).toInt()
                binding.graphSecondPlayer2.layoutParams.width=(secondServePercentage2 * scale + 0.5f).toInt()

                //odswiazanie
                binding.graphTotalPlayer1.requestLayout()
                binding.graphTotalPlayer2.requestLayout()
                binding.graphAcePlayer1.requestLayout()
                binding.graphAcePlayer2.requestLayout()
                binding.graphDoubleFaultPlayer1.requestLayout()
                binding.graphDoubleFaultPlayer2.requestLayout()
                binding.graphFirstPlayer1.requestLayout()
                binding.graphFirstPlayer2.requestLayout()
                binding.graphSecondPlayer1.requestLayout()
                binding.graphSecondPlayer1.requestLayout()
            } //TOTAL
            1 -> {
                binding.returnWfhPlayer1.text = app.returnwinnerFH1.toString()
                binding.returnWfhPlayer2.text = app.returnwinnerFH2.toString()

                //kod do ustawienia wykresow
                if(app.returnwinnerFH1==app.returnwinnerFH2 && app.returnwinnerFH1==0){
                    binding.graphReturnWfhPlayer1.layoutParams.width=0
                    binding.graphReturnWfhPlayer2.layoutParams.width=0
                }
                else if(app.returnwinnerFH1>app.returnwinnerFH2){
                    binding.graphReturnWfhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.returnwinnerFH2.toDouble() * 100 /app.returnwinnerFH1.toDouble()).toInt()
                    binding.graphReturnWfhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }
                else{
                    binding.graphReturnWfhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.returnwinnerFH1.toDouble() * 100 / app.returnwinnerFH2.toDouble()).toInt()
                    binding.graphReturnWfhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }

                binding.returnWbhPlayer1.text = app.returnwinnerBH1.toString()
                binding.returnWbhPlayer2.text = app.returnwinnerBH2.toString()

                if(app.returnwinnerBH1==app.returnwinnerBH2 && app.returnwinnerBH1==0){
                    binding.graphReturnWbhPlayer1.layoutParams.width=0
                    binding.graphReturnWbhPlayer2.layoutParams.width=0
                }
                else if(app.returnwinnerBH1>app.returnwinnerBH2){
                    binding.graphReturnWbhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.returnwinnerBH2.toDouble() * 100 /app.returnwinnerBH1.toDouble()).toInt()
                    binding.graphReturnWbhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }
                else{
                    binding.graphReturnWbhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.returnwinnerBH1.toDouble() * 100 / app.returnwinnerBH2.toDouble()).toInt()
                    binding.graphReturnWbhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }

                binding.returnEfhPlayer1.text = app.returnerrorFH1.toString()
                binding.returnEfhPlayer2.text = app.returnerrorFH2.toString()

                if(app.returnerrorFH1==app.returnerrorFH2 && app.returnerrorFH1==0){
                    binding.graphReturnEfhPlayer1.layoutParams.width=0
                    binding.graphReturnEfhPlayer2.layoutParams.width=0
                }
                else if(app.returnerrorFH1>app.returnerrorFH2){
                    binding.graphReturnEfhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.returnerrorFH2.toDouble() * 100 /app.returnerrorFH1.toDouble()).toInt()
                    binding.graphReturnEfhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }
                else{
                    binding.graphReturnEfhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.returnerrorFH1.toDouble() * 100 / app.returnerrorFH2.toDouble()).toInt()
                    binding.graphReturnEfhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }

                binding.returnEbhPlayer1.text = app.returnerrorBH1.toString()
                binding.returnEbhPlayer2.text = app.returnerrorBH2.toString()

                if(app.returnerrorBH1==app.returnerrorBH2 && app.returnerrorBH1==0){
                    binding.graphReturnEbhPlayer1.layoutParams.width=0
                    binding.graphReturnEbhPlayer2.layoutParams.width=0
                }
                else if(app.returnerrorBH1>app.returnerrorBH2){
                    binding.graphReturnEbhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.returnerrorBH2.toDouble() * 100 /app.returnerrorBH1.toDouble()).toInt()
                    binding.graphReturnEbhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }
                else{
                    binding.graphReturnEbhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.returnerrorBH1.toDouble() * 100 / app.returnerrorBH2.toDouble()).toInt()
                    binding.graphReturnEbhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }
                //odswiazanie
                binding.graphReturnWfhPlayer1.requestLayout()
                binding.graphReturnWfhPlayer2.requestLayout()
                binding.graphReturnWbhPlayer1.requestLayout()
                binding.graphReturnWbhPlayer2.requestLayout()
                binding.graphReturnEfhPlayer1.requestLayout()
                binding.graphReturnEfhPlayer2.requestLayout()
                binding.graphReturnEbhPlayer1.requestLayout()
                binding.graphReturnEbhPlayer2.requestLayout()
            } //RETURN
            2 -> {
                binding.winnerGroundfhPlayer1.text = app.winnergroundFH1.toString()
                binding.winnerGroundfhPlayer2.text = app.winnergroundFH2.toString()

                if(app.winnergroundFH1==app.winnergroundFH2 && app.winnergroundFH1==0){
                    binding.graphWinnerGroundfhPlayer1.layoutParams.width=0
                    binding.graphWinnerGroundfhPlayer2.layoutParams.width=0
                }
                else if(app.winnergroundFH1>app.winnergroundFH2){
                    binding.graphWinnerGroundfhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnergroundFH2.toDouble() * 100 /app.winnergroundFH1.toDouble()).toInt()
                    binding.graphWinnerGroundfhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }
                else{
                    binding.graphWinnerGroundfhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnergroundFH1.toDouble() * 100 / app.winnergroundFH2.toDouble()).toInt()
                    binding.graphWinnerGroundfhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }

                binding.winnerGroundbhPlayer1.text = app.winnergroundBH1.toString()
                binding.winnerGroundbhPlayer2.text = app.winnergroundBH2.toString()

                if(app.winnergroundBH1==app.winnergroundBH2 && app.winnergroundBH1==0){
                    binding.graphWinnerGroundbhPlayer1.layoutParams.width=0
                    binding.graphWinnerGroundbhPlayer2.layoutParams.width=0
                }
                else if(app.winnergroundBH1>app.winnergroundBH2){
                    binding.graphWinnerGroundbhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnergroundBH2.toDouble() * 100 /app.winnergroundBH1.toDouble()).toInt()
                    binding.graphWinnerGroundbhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }
                else{
                    binding.graphWinnerGroundbhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnergroundBH1.toDouble() * 100 / app.winnergroundBH2.toDouble()).toInt()
                    binding.graphWinnerGroundbhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }

                binding.winnerSlicefhPlayer1.text = app.winnersliceFH1.toString()
                binding.winnerSlicefhPlayer2.text = app.winnersliceFH2.toString()

                if(app.winnersliceFH1==app.winnersliceFH2 && app.winnersliceFH1==0){
                    binding.graphWinnerSlicefhPlayer1.layoutParams.width=0
                    binding.graphWinnerSlicefhPlayer2.layoutParams.width=0
                }
                else if(app.winnersliceFH1>app.winnersliceFH2){
                    binding.graphWinnerSlicefhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnersliceFH2.toDouble() * 100 /app.winnersliceFH1.toDouble()).toInt()
                    binding.graphWinnerSlicefhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }
                else{
                    binding.graphWinnerSlicefhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnersliceFH1.toDouble() * 100 / app.winnersliceFH2.toDouble()).toInt()
                    binding.graphWinnerSlicefhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }

                binding.winnerSlicebhPlayer1.text = app.winnersliceBH1.toString()
                binding.winnerSlicebhPlayer2.text = app.winnersliceBH2.toString()

                if(app.winnersliceBH1==app.winnersliceBH2 && app.winnersliceBH1==0){
                    binding.graphWinnerSlicebhPlayer1.layoutParams.width=0
                    binding.graphWinnerSlicebhPlayer2.layoutParams.width=0
                }
                else if(app.winnersliceBH1>app.winnersliceBH2){
                    binding.graphWinnerSlicebhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnersliceBH2.toDouble() * 100 /app.winnersliceBH1.toDouble()).toInt()
                    binding.graphWinnerSlicebhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }
                else{
                    binding.graphWinnerSlicebhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnersliceBH1.toDouble() * 100 / app.winnersliceBH2.toDouble()).toInt()
                    binding.graphWinnerSlicebhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }

                binding.winnerSmashfhPlayer1.text = app.winnersmashFH1.toString()
                binding.winnerSmashfhPlayer2.text = app.winnersmashFH2.toString()

                if(app.winnersmashFH1==app.winnersmashFH2 && app.winnersmashFH1==0){
                    binding.graphWinnerSmashfhPlayer1.layoutParams.width=0
                    binding.graphWinnerSmashfhPlayer2.layoutParams.width=0
                }
                else if(app.winnersmashFH1>app.winnersmashFH2){
                    binding.graphWinnerSmashfhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnersmashFH2.toDouble() * 100 /app.winnersmashFH1.toDouble()).toInt()
                    binding.graphWinnerSmashfhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }
                else{
                    binding.graphWinnerSmashfhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnersmashFH1.toDouble() * 100 / app.winnersmashFH2.toDouble()).toInt()
                    binding.graphWinnerSmashfhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }

                binding.winnerSmashbhPlayer1.text = app.winnersmashBH1.toString()
                binding.winnerSmashbhPlayer2.text = app.winnersmashBH2.toString()

                if(app.winnersmashBH1==app.winnersmashBH2 && app.winnersmashBH1==0){
                    binding.graphWinnerSmashbhPlayer1.layoutParams.width=0
                    binding.graphWinnerSmashbhPlayer2.layoutParams.width=0
                }
                else if(app.winnersmashBH1>app.winnersmashBH2){
                    binding.graphWinnerSmashbhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnersmashBH2.toDouble() * 100 /app.winnersmashBH1.toDouble()).toInt()
                    binding.graphWinnerSmashbhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }
                else{
                    binding.graphWinnerSmashbhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnersmashBH1.toDouble() * 100 / app.winnersmashBH2.toDouble()).toInt()
                    binding.graphWinnerSmashbhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }

                binding.winnerVolleyfhPlayer1.text = app.winnervolleyFH1.toString()
                binding.winnerVolleyfhPlayer2.text = app.winnervolleyFH2.toString()

                if(app.winnervolleyFH1==app.winnervolleyFH2 && app.winnervolleyFH1==0){
                    binding.graphWinnerVolleyfhPlayer1.layoutParams.width=0
                    binding.graphWinnerVolleyfhPlayer2.layoutParams.width=0
                }
                else if(app.winnervolleyFH1>app.winnervolleyFH2){
                    binding.graphWinnerVolleyfhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnervolleyFH2.toDouble() * 100 /app.winnervolleyFH1.toDouble()).toInt()
                    binding.graphWinnerVolleyfhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }
                else{
                    binding.graphWinnerVolleyfhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnervolleyFH1.toDouble() * 100 / app.winnervolleyFH2.toDouble()).toInt()
                    binding.graphWinnerVolleyfhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }

                binding.winnerVolleybhPlayer1.text = app.winnervolleyBH1.toString()
                binding.winnerVolleybhPlayer2.text = app.winnervolleyBH2.toString()

                if(app.winnervolleyBH1==app.winnervolleyBH2 && app.winnervolleyBH1==0){
                    binding.graphWinnerVolleybhPlayer1.layoutParams.width=0
                    binding.graphWinnerVolleybhPlayer2.layoutParams.width=0
                }
                else if(app.winnervolleyBH1>app.winnervolleyBH2){
                    binding.graphWinnerVolleybhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnervolleyBH2.toDouble() * 100 /app.winnervolleyBH1.toDouble()).toInt()
                    binding.graphWinnerVolleybhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }
                else{
                    binding.graphWinnerVolleybhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnervolleyBH1.toDouble() * 100 / app.winnervolleyBH2.toDouble()).toInt()
                    binding.graphWinnerVolleybhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }

                binding.winnerDropshotfhPlayer1.text = app.winnerdropshotFH1.toString()
                binding.winnerDropshotfhPlayer2.text = app.winnerdropshotFH2.toString()

                if(app.winnerdropshotFH1==app.winnerdropshotFH2 && app.winnerdropshotFH1==0){
                    binding.graphWinnerDropshotfhPlayer1.layoutParams.width=0
                    binding.graphWinnerDropshotfhPlayer2.layoutParams.width=0
                }
                else if(app.winnerdropshotFH1>app.winnerdropshotFH2){
                    binding.graphWinnerDropshotfhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnerdropshotFH2.toDouble() * 100 /app.winnerdropshotFH1.toDouble()).toInt()
                    binding.graphWinnerDropshotfhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }
                else{
                    binding.graphWinnerDropshotfhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnerdropshotFH1.toDouble() * 100 / app.winnerdropshotFH2.toDouble()).toInt()
                    binding.graphWinnerDropshotfhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }

                binding.winnerDropshotbhPlayer1.text = app.winnerdropshotBH1.toString()
                binding.winnerDropshotbhPlayer2.text = app.winnerdropshotBH2.toString()

                if(app.winnerdropshotBH1==app.winnerdropshotBH2 && app.winnerdropshotBH1==0){
                    binding.graphWinnerDropshotbhPlayer1.layoutParams.width=0
                    binding.graphWinnerDropshotbhPlayer2.layoutParams.width=0
                }
                else if(app.winnerdropshotBH1>app.winnerdropshotBH2){
                    binding.graphWinnerDropshotbhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnerdropshotBH2.toDouble() * 100 /app.winnerdropshotBH1.toDouble()).toInt()
                    binding.graphWinnerDropshotbhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }
                else{
                    binding.graphWinnerDropshotbhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnerdropshotBH1.toDouble() * 100 / app.winnerdropshotBH2.toDouble()).toInt()
                    binding.graphWinnerDropshotbhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }

                binding.winnerLobfhPlayer1.text = app.winnerlobFH1.toString()
                binding.winnerLobfhPlayer2.text = app.winnerlobFH2.toString()

                if(app.winnerlobFH1==app.winnerlobFH2 && app.winnerlobFH1==0){
                    binding.graphWinnerLobfhPlayer1.layoutParams.width=0
                    binding.graphWinnerLobfhPlayer2.layoutParams.width=0
                }
                else if(app.winnerlobFH1>app.winnerlobFH2){
                    binding.graphWinnerLobfhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnerlobFH2.toDouble() * 100 /app.winnerlobFH1.toDouble()).toInt()
                    binding.graphWinnerLobfhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }
                else{
                    binding.graphWinnerLobfhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnerlobFH1.toDouble() * 100 / app.winnerlobFH2.toDouble()).toInt()
                    binding.graphWinnerLobfhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }

                binding.winnerLobbhPlayer1.text = app.winnerlobBH1.toString()
                binding.winnerLobbhPlayer2.text = app.winnerlobBH2.toString()

                if(app.winnerlobBH1==app.winnerlobBH2 && app.winnerlobBH1==0){
                    binding.graphWinnerLobbhPlayer1.layoutParams.width=0
                    binding.graphWinnerLobbhPlayer2.layoutParams.width=0
                }
                else if(app.winnerlobBH1>app.winnerlobBH2){
                    binding.graphWinnerLobbhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnerlobBH2.toDouble() * 100 /app.winnerlobBH1.toDouble()).toInt()
                    binding.graphWinnerLobbhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }
                else{
                    binding.graphWinnerLobbhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.winnerlobBH1.toDouble() * 100 / app.winnerlobBH2.toDouble()).toInt()
                    binding.graphWinnerLobbhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                }

                //odswiazanie
                binding.graphWinnerGroundfhPlayer1.requestLayout()
                binding.graphWinnerGroundfhPlayer2.requestLayout()
                binding.graphWinnerGroundbhPlayer1.requestLayout()
                binding.graphWinnerGroundbhPlayer2.requestLayout()
                binding.graphWinnerVolleyfhPlayer1.requestLayout()
                binding.graphWinnerVolleyfhPlayer2.requestLayout()
                binding.graphWinnerVolleybhPlayer1.requestLayout()
                binding.graphWinnerVolleybhPlayer2.requestLayout()
                binding.graphWinnerSlicefhPlayer1.requestLayout()
                binding.graphWinnerSlicefhPlayer2.requestLayout()
                binding.graphWinnerSlicebhPlayer1.requestLayout()
                binding.graphWinnerSlicebhPlayer2.requestLayout()
                binding.graphWinnerSmashfhPlayer1.requestLayout()
                binding.graphWinnerSmashfhPlayer2.requestLayout()
                binding.graphWinnerSmashbhPlayer1.requestLayout()
                binding.graphWinnerSmashbhPlayer2.requestLayout()
                binding.graphWinnerLobfhPlayer1.requestLayout()
                binding.graphWinnerLobfhPlayer2.requestLayout()
                binding.graphWinnerLobbhPlayer1.requestLayout()
                binding.graphWinnerLobbhPlayer2.requestLayout()
                binding.graphWinnerDropshotfhPlayer1.requestLayout()
                binding.graphWinnerDropshotfhPlayer2.requestLayout()
                binding.graphWinnerDropshotbhPlayer1.requestLayout()
                binding.graphWinnerDropshotbhPlayer2.requestLayout()
            } //WINNERS
            3 -> {
                binding.forcedErrorGroundfhPlayer1.text = app.forcederrorgroundFH1.toString()
                binding.forcedErrorGroundfhPlayer2.text = app.forcederrorgroundFH2.toString()

                if(app.forcederrorgroundFH1==app.forcederrorgroundFH2 && app.forcederrorgroundFH1==0){
                    binding.graphForcedErrorGroundfhPlayer1.layoutParams.width=0
                    binding.graphForcedErrorGroundfhPlayer2.layoutParams.width=0
                }
                else if(app.forcederrorgroundFH1>app.forcederrorgroundFH2){
                    binding.graphForcedErrorGroundfhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrorgroundFH2.toDouble() * 100 /app.forcederrorgroundFH1.toDouble()).toInt()
                    binding.graphForcedErrorGroundfhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphForcedErrorGroundfhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrorgroundFH1.toDouble() * 100 / app.forcederrorgroundFH2.toDouble()).toInt()
                    binding.graphForcedErrorGroundfhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                binding.forcedErrorGroundbhPlayer1.text = app.forcederrorgroundBH1.toString()
                binding.forcedErrorGroundbhPlayer2.text = app.forcederrorgroundBH2.toString()

                if(app.forcederrorgroundBH1==app.forcederrorgroundBH2 && app.forcederrorgroundBH1==0){
                    binding.graphForcedErrorGroundbhPlayer1.layoutParams.width=0
                    binding.graphForcedErrorGroundbhPlayer2.layoutParams.width=0
                }
                else if(app.forcederrorgroundBH1>app.forcederrorgroundBH2){
                    binding.graphForcedErrorGroundbhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrorgroundBH2.toDouble() * 100 /app.forcederrorgroundBH1.toDouble()).toInt()
                    binding.graphForcedErrorGroundbhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphForcedErrorGroundbhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrorgroundBH1.toDouble() * 100 / app.forcederrorgroundBH2.toDouble()).toInt()
                    binding.graphForcedErrorGroundbhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                binding.forcedErrorSlicefhPlayer1.text = app.forcederrorsliceFH1.toString()
                binding.forcedErrorSlicefhPlayer2.text = app.forcederrorsliceFH2.toString()

                if(app.forcederrorsliceFH1==app.forcederrorsliceFH2 && app.forcederrorsliceFH1==0){
                    binding.graphForcedErrorSlicefhPlayer1.layoutParams.width=0
                    binding.graphForcedErrorSlicefhPlayer2.layoutParams.width=0
                }
                else if(app.forcederrorsliceFH1>app.forcederrorsliceFH2){
                    binding.graphForcedErrorSlicefhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrorsliceFH2.toDouble() * 100 /app.forcederrorsliceFH1.toDouble()).toInt()
                    binding.graphForcedErrorSlicefhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphForcedErrorSlicefhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrorsliceFH1.toDouble() * 100 / app.forcederrorsliceFH2.toDouble()).toInt()
                    binding.graphForcedErrorSlicefhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                binding.forcedErrorSlicebhPlayer1.text = app.forcederrorsliceBH1.toString()
                binding.forcedErrorSlicebhPlayer2.text = app.forcederrorsliceBH2.toString()

                if(app.forcederrorsliceBH1==app.forcederrorsliceBH2 && app.forcederrorsliceBH1==0){
                    binding.graphForcedErrorSlicebhPlayer1.layoutParams.width=0
                    binding.graphForcedErrorSlicebhPlayer2.layoutParams.width=0
                }
                else if(app.forcederrorsliceBH1>app.forcederrorsliceBH2){
                    binding.graphForcedErrorSlicebhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrorsliceBH2.toDouble() * 100 /app.forcederrorsliceBH1.toDouble()).toInt()
                    binding.graphForcedErrorSlicebhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphForcedErrorSlicebhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrorsliceBH1.toDouble() * 100 / app.forcederrorsliceBH2.toDouble()).toInt()
                    binding.graphForcedErrorSlicebhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                binding.forcedErrorSmashfhPlayer1.text = app.forcederrorsmashFH1.toString()
                binding.forcedErrorSmashfhPlayer2.text = app.forcederrorsmashFH2.toString()

                if(app.forcederrorsmashFH1==app.forcederrorsmashFH2 && app.forcederrorsmashFH1==0){
                    binding.graphForcedErrorSmashfhPlayer1.layoutParams.width=0
                    binding.graphForcedErrorSmashfhPlayer2.layoutParams.width=0
                }
                else if(app.forcederrorsmashFH1>app.forcederrorsmashFH2){
                    binding.graphForcedErrorSmashfhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrorsmashFH2.toDouble() * 100 /app.forcederrorsmashFH1.toDouble()).toInt()
                    binding.graphForcedErrorSmashfhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphForcedErrorSmashfhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrorsmashFH1.toDouble() * 100 / app.forcederrorsmashFH2.toDouble()).toInt()
                    binding.graphForcedErrorSmashfhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                binding.forcedErrorSmashbhPlayer1.text = app.forcederrorsmashBH1.toString()
                binding.forcedErrorSmashbhPlayer2.text = app.forcederrorsmashBH2.toString()

                if(app.forcederrorsmashBH1==app.forcederrorsmashBH2 && app.forcederrorsmashBH1==0){
                    binding.graphForcedErrorSmashbhPlayer1.layoutParams.width=0
                    binding.graphForcedErrorSmashbhPlayer2.layoutParams.width=0
                }
                else if(app.forcederrorsmashBH1>app.forcederrorsmashBH2){
                    binding.graphForcedErrorSmashbhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrorsmashBH2.toDouble() * 100 /app.forcederrorsmashBH1.toDouble()).toInt()
                    binding.graphForcedErrorSmashbhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphForcedErrorSmashbhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrorsmashBH1.toDouble() * 100 / app.forcederrorsmashBH2.toDouble()).toInt()
                    binding.graphForcedErrorSmashbhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                binding.forcedErrorVolleyfhPlayer1.text = app.forcederrorvolleyFH1.toString()
                binding.forcedErrorVolleyfhPlayer2.text = app.forcederrorvolleyFH2.toString()

                if(app.forcederrorvolleyFH1==app.forcederrorvolleyFH2 && app.forcederrorvolleyFH1==0){
                    binding.graphForcedErrorVolleyfhPlayer1.layoutParams.width=0
                    binding.graphForcedErrorVolleyfhPlayer2.layoutParams.width=0
                }
                else if(app.forcederrorvolleyFH1>app.forcederrorvolleyFH2){
                    binding.graphForcedErrorVolleyfhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrorvolleyFH2.toDouble() * 100 /app.forcederrorvolleyFH1.toDouble()).toInt()
                    binding.graphForcedErrorVolleyfhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphForcedErrorVolleyfhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrorvolleyFH1.toDouble() * 100 / app.forcederrorvolleyFH2.toDouble()).toInt()
                    binding.graphForcedErrorVolleyfhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                binding.forcedErrorVolleybhPlayer1.text = app.forcederrorvolleyBH1.toString()
                binding.forcedErrorVolleybhPlayer2.text = app.forcederrorvolleyBH2.toString()

                if(app.forcederrorvolleyBH1==app.forcederrorvolleyBH2 && app.forcederrorvolleyBH1==0){
                    binding.graphForcedErrorVolleybhPlayer1.layoutParams.width=0
                    binding.graphForcedErrorVolleybhPlayer2.layoutParams.width=0
                }
                else if(app.forcederrorvolleyBH1>app.forcederrorvolleyBH2){
                    binding.graphForcedErrorVolleybhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrorvolleyBH2.toDouble() * 100 /app.forcederrorvolleyBH1.toDouble()).toInt()
                    binding.graphForcedErrorVolleybhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphForcedErrorVolleybhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrorvolleyBH1.toDouble() * 100 / app.forcederrorvolleyBH2.toDouble()).toInt()
                    binding.graphForcedErrorVolleybhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                binding.forcedErrorDropshotfhPlayer1.text = app.forcederrordropshotFH1.toString()
                binding.forcedErrorDropshotfhPlayer2.text = app.forcederrordropshotFH2.toString()

                if(app.forcederrordropshotFH1==app.forcederrordropshotFH2 && app.forcederrordropshotFH1==0){
                    binding.graphForcedErrorDropshotfhPlayer1.layoutParams.width=0
                    binding.graphForcedErrorDropshotfhPlayer2.layoutParams.width=0
                }
                else if(app.forcederrordropshotFH1>app.forcederrordropshotFH2){
                    binding.graphForcedErrorDropshotfhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrordropshotFH2.toDouble() * 100 /app.forcederrordropshotFH1.toDouble()).toInt()
                    binding.graphForcedErrorDropshotfhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphForcedErrorDropshotfhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrordropshotFH1.toDouble() * 100 / app.forcederrordropshotFH2.toDouble()).toInt()
                    binding.graphForcedErrorDropshotfhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                findViewById<TextView>(R.id.forcedErrorDropshotbhPlayer1).text = app.forcederrordropshotBH1.toString()
                findViewById<TextView>(R.id.forcedErrorDropshotbhPlayer2).text = app.forcederrordropshotBH2.toString()

                if(app.forcederrordropshotBH1==app.forcederrordropshotBH2 && app.forcederrordropshotBH1==0){
                    binding.graphForcedErrorDropshotbhPlayer1.layoutParams.width=0
                    binding.graphForcedErrorDropshotbhPlayer2.layoutParams.width=0
                }
                else if(app.forcederrordropshotBH1>app.forcederrordropshotBH2){
                    binding.graphForcedErrorDropshotbhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrordropshotBH2.toDouble() * 100 /app.forcederrordropshotBH1.toDouble()).toInt()
                    binding.graphForcedErrorDropshotbhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphForcedErrorDropshotbhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrordropshotBH1.toDouble() * 100 / app.forcederrordropshotBH2.toDouble()).toInt()
                    binding.graphForcedErrorDropshotbhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                findViewById<TextView>(R.id.forcedErrorLobfhPlayer1).text = app.forcederrorlobFH1.toString()
                findViewById<TextView>(R.id.forcedErrorLobfhPlayer2).text = app.forcederrorlobFH2.toString()

                if(app.forcederrorlobFH1==app.forcederrorlobFH2 && app.forcederrorlobFH1==0){
                    binding.graphForcedErrorLobfhPlayer1.layoutParams.width=0
                    binding.graphForcedErrorLobfhPlayer2.layoutParams.width=0
                }
                else if(app.forcederrorlobFH1>app.forcederrorlobFH2){
                    binding.graphForcedErrorLobfhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrorlobFH2.toDouble() * 100 /app.forcederrorlobFH1.toDouble()).toInt()
                    binding.graphForcedErrorLobfhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphForcedErrorLobfhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrorlobFH1.toDouble() * 100 / app.forcederrorlobFH2.toDouble()).toInt()
                    binding.graphForcedErrorLobfhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                findViewById<TextView>(R.id.forcedErrorLobbhPlayer1).text = app.forcederrorlobBH1.toString()
                findViewById<TextView>(R.id.forcedErrorLobbhPlayer2).text = app.forcederrorlobBH2.toString()

                if(app.forcederrorlobBH1==app.forcederrorlobBH2 && app.forcederrorlobBH1==0){
                    binding.graphForcedErrorLobbhPlayer1.layoutParams.width=0
                    binding.graphForcedErrorLobbhPlayer2.layoutParams.width=0
                }
                else if(app.forcederrorlobBH1>app.forcederrorlobBH2){
                    binding.graphForcedErrorLobbhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrorlobBH2.toDouble() * 100 /app.forcederrorlobBH1.toDouble()).toInt()
                    binding.graphForcedErrorLobbhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphForcedErrorLobbhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.forcederrorlobBH1.toDouble() * 100 / app.forcederrorlobBH2.toDouble()).toInt()
                    binding.graphForcedErrorLobbhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                //odswiazanie
                binding.graphForcedErrorGroundfhPlayer1.requestLayout()
                binding.graphForcedErrorGroundfhPlayer2.requestLayout()
                binding.graphForcedErrorGroundbhPlayer1.requestLayout()
                binding.graphForcedErrorGroundbhPlayer2.requestLayout()
                binding.graphForcedErrorVolleyfhPlayer1.requestLayout()
                binding.graphForcedErrorVolleyfhPlayer2.requestLayout()
                binding.graphForcedErrorVolleybhPlayer1.requestLayout()
                binding.graphForcedErrorVolleybhPlayer2.requestLayout()
                binding.graphForcedErrorSlicefhPlayer1.requestLayout()
                binding.graphForcedErrorSlicefhPlayer2.requestLayout()
                binding.graphForcedErrorSlicebhPlayer1.requestLayout()
                binding.graphForcedErrorSlicebhPlayer2.requestLayout()
                binding.graphForcedErrorSmashfhPlayer1.requestLayout()
                binding.graphForcedErrorSmashfhPlayer2.requestLayout()
                binding.graphForcedErrorSmashbhPlayer1.requestLayout()
                binding.graphForcedErrorSmashbhPlayer2.requestLayout()
                binding.graphForcedErrorLobfhPlayer1.requestLayout()
                binding.graphForcedErrorLobfhPlayer2.requestLayout()
                binding.graphForcedErrorLobbhPlayer1.requestLayout()
                binding.graphForcedErrorLobbhPlayer2.requestLayout()
                binding.graphForcedErrorDropshotfhPlayer1.requestLayout()
                binding.graphForcedErrorDropshotfhPlayer2.requestLayout()
                binding.graphForcedErrorDropshotbhPlayer1.requestLayout()
                binding.graphForcedErrorDropshotbhPlayer2.requestLayout()
            } //FORCED ERRORS
            4 -> {
                binding.unforcedErrorGroundfhPlayer1.text = app.unforcederrorgroundFH1.toString()
                binding.unforcedErrorGroundfhPlayer2.text = app.unforcederrorgroundFH2.toString()

                if(app.unforcederrorgroundFH1==app.unforcederrorgroundFH2 && app.unforcederrorgroundFH1==0){
                    binding.graphUnforcedErrorGroundfhPlayer1.layoutParams.width=0
                    binding.graphUnforcedErrorGroundfhPlayer2.layoutParams.width=0
                }
                else if(app.unforcederrorgroundFH1>app.unforcederrorgroundFH2){
                    binding.graphUnforcedErrorGroundfhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrorgroundFH2.toDouble() * 100 /app.unforcederrorgroundFH1.toDouble()).toInt()
                    binding.graphUnforcedErrorGroundfhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphUnforcedErrorGroundfhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrorgroundFH1.toDouble() * 100 / app.unforcederrorgroundFH2.toDouble()).toInt()
                    binding.graphUnforcedErrorGroundfhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                binding.unforcedErrorGroundbhPlayer1.text = app.unforcederrorgroundBH1.toString()
                binding.unforcedErrorGroundbhPlayer2.text = app.unforcederrorgroundBH2.toString()

                if(app.unforcederrorgroundBH1==app.unforcederrorgroundBH2 && app.unforcederrorgroundBH1==0){
                    binding.graphUnforcedErrorGroundbhPlayer1.layoutParams.width=0
                    binding.graphUnforcedErrorGroundbhPlayer2.layoutParams.width=0
                }
                else if(app.unforcederrorgroundBH1>app.unforcederrorgroundBH2){
                    binding.graphUnforcedErrorGroundbhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrorgroundBH2.toDouble() * 100 /app.unforcederrorgroundBH1.toDouble()).toInt()
                    binding.graphUnforcedErrorGroundbhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphUnforcedErrorGroundbhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrorgroundBH1.toDouble() * 100 / app.unforcederrorgroundBH2.toDouble()).toInt()
                    binding.graphUnforcedErrorGroundbhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                binding.unforcedErrorSlicefhPlayer1.text = app.unforcederrorsliceFH1.toString()
                binding.unforcedErrorSlicefhPlayer2.text = app.unforcederrorsliceFH2.toString()

                if(app.unforcederrorsliceFH1==app.unforcederrorsliceFH2 && app.unforcederrorsliceFH1==0){
                    binding.graphUnforcedErrorSlicefhPlayer1.layoutParams.width=0
                    binding.graphUnforcedErrorSlicefhPlayer2.layoutParams.width=0
                }
                else if(app.unforcederrorsliceFH1>app.unforcederrorsliceFH2){
                    binding.graphUnforcedErrorSlicefhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrorsliceFH2.toDouble() * 100 /app.unforcederrorsliceFH1.toDouble()).toInt()
                    binding.graphUnforcedErrorSlicefhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphUnforcedErrorSlicefhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrorsliceFH1.toDouble() * 100 / app.unforcederrorsliceFH2.toDouble()).toInt()
                    binding.graphUnforcedErrorSlicefhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                binding.unforcedErrorSlicebhPlayer1.text = app.unforcederrorsliceBH1.toString()
                binding.unforcedErrorSlicebhPlayer2.text = app.unforcederrorsliceBH2.toString()

                if(app.unforcederrorsliceBH1==app.unforcederrorsliceBH2 && app.unforcederrorsliceBH1==0){
                    binding.graphUnforcedErrorSlicebhPlayer1.layoutParams.width=0
                    binding.graphUnforcedErrorSlicebhPlayer2.layoutParams.width=0
                }
                else if(app.unforcederrorsliceBH1>app.unforcederrorsliceBH2){
                    binding.graphUnforcedErrorSlicebhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrorsliceBH2.toDouble() * 100 /app.unforcederrorsliceBH1.toDouble()).toInt()
                    binding.graphUnforcedErrorSlicebhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphUnforcedErrorSlicebhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrorsliceBH1.toDouble() * 100 / app.unforcederrorsliceBH2.toDouble()).toInt()
                    binding.graphUnforcedErrorSlicebhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                binding.unforcedErrorSmashfhPlayer1.text = app.unforcederrorsmashFH1.toString()
                binding.unforcedErrorSmashfhPlayer2.text = app.unforcederrorsmashFH2.toString()

                if(app.unforcederrorsmashFH1==app.unforcederrorsmashFH2 && app.unforcederrorsmashFH1==0){
                    binding.graphUnforcedErrorSmashfhPlayer1.layoutParams.width=0
                    binding.graphUnforcedErrorSmashfhPlayer2.layoutParams.width=0
                }
                else if(app.unforcederrorsmashFH1>app.unforcederrorsmashFH2){
                    binding.graphUnforcedErrorSmashfhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrorsmashFH2.toDouble() * 100 /app.unforcederrorsmashFH1.toDouble()).toInt()
                    binding.graphUnforcedErrorSmashfhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphUnforcedErrorSmashfhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrorsmashFH1.toDouble() * 100 / app.unforcederrorsmashFH2.toDouble()).toInt()
                    binding.graphUnforcedErrorSmashfhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                binding.unforcedErrorSmashbhPlayer1.text = app.unforcederrorsmashBH1.toString()
                binding.unforcedErrorSmashbhPlayer2.text = app.unforcederrorsmashBH2.toString()

                if(app.unforcederrorsmashBH1==app.unforcederrorsmashBH2 && app.unforcederrorsmashBH1==0){
                    binding.graphUnforcedErrorSmashbhPlayer1.layoutParams.width=0
                    binding.graphUnforcedErrorSmashbhPlayer2.layoutParams.width=0
                }
                else if(app.unforcederrorsmashBH1>app.unforcederrorsmashBH2){
                    binding.graphUnforcedErrorSmashbhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrorsmashBH2.toDouble() * 100 /app.unforcederrorsmashBH1.toDouble()).toInt()
                    binding.graphUnforcedErrorSmashbhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphUnforcedErrorSmashbhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrorsmashBH1.toDouble() * 100 / app.unforcederrorsmashBH2.toDouble()).toInt()
                    binding.graphUnforcedErrorSmashbhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                binding.unforcedErrorVolleyfhPlayer1.text = app.unforcederrorvolleyFH1.toString()
                binding.unforcedErrorVolleyfhPlayer2.text = app.unforcederrorvolleyFH2.toString()

                if(app.unforcederrorvolleyFH1==app.unforcederrorvolleyFH2 && app.unforcederrorvolleyFH1==0){
                    binding.graphUnforcedErrorVolleyfhPlayer1.layoutParams.width=0
                    binding.graphUnforcedErrorVolleyfhPlayer2.layoutParams.width=0
                }
                else if(app.unforcederrorvolleyFH1>app.unforcederrorvolleyFH2){
                    binding.graphUnforcedErrorVolleyfhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrorvolleyFH2.toDouble() * 100 /app.unforcederrorvolleyFH1.toDouble()).toInt()
                    binding.graphUnforcedErrorVolleyfhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphUnforcedErrorVolleyfhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrorvolleyFH1.toDouble() * 100 / app.unforcederrorvolleyFH2.toDouble()).toInt()
                    binding.graphUnforcedErrorVolleyfhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                binding.unforcedErrorVolleybhPlayer1.text = app.unforcederrorvolleyBH1.toString()
                binding.unforcedErrorVolleybhPlayer2.text = app.unforcederrorvolleyBH2.toString()

                if(app.unforcederrorvolleyBH1==app.unforcederrorvolleyBH2 && app.unforcederrorvolleyBH1==0){
                    binding.graphUnforcedErrorVolleybhPlayer1.layoutParams.width=0
                    binding.graphUnforcedErrorVolleybhPlayer2.layoutParams.width=0
                }
                else if(app.unforcederrorvolleyBH1>app.unforcederrorvolleyBH2){
                    binding.graphUnforcedErrorVolleybhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrorvolleyBH2.toDouble() * 100 /app.unforcederrorvolleyBH1.toDouble()).toInt()
                    binding.graphUnforcedErrorVolleybhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphUnforcedErrorVolleybhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrorvolleyBH1.toDouble() * 100 / app.unforcederrorvolleyBH2.toDouble()).toInt()
                    binding.graphUnforcedErrorVolleybhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                binding.unforcedErrorLobfhPlayer1.text = app.unforcederrorlobFH1.toString()
                binding.unforcedErrorLobfhPlayer2.text = app.unforcederrorlobFH2.toString()

                if(app.unforcederrorlobFH1==app.unforcederrorlobFH2 && app.unforcederrorlobFH1==0){
                    binding.graphUnforcedErrorLobfhPlayer1.layoutParams.width=0
                    binding.graphUnforcedErrorLobfhPlayer2.layoutParams.width=0
                }
                else if(app.unforcederrorlobFH1>app.unforcederrorlobFH2){
                    binding.graphUnforcedErrorLobfhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrorlobFH2.toDouble() * 100 /app.unforcederrorlobFH1.toDouble()).toInt()
                    binding.graphUnforcedErrorLobfhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphUnforcedErrorLobfhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrorlobFH1.toDouble() * 100 / app.unforcederrorlobFH2.toDouble()).toInt()
                    binding.graphUnforcedErrorLobfhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                binding.unforcedErrorLobbhPlayer1.text = app.unforcederrorlobBH1.toString()
                binding.unforcedErrorLobbhPlayer2.text = app.unforcederrorlobBH2.toString()

                if(app.unforcederrorlobBH1==app.unforcederrorlobBH2 && app.unforcederrorlobBH1==0){
                    binding.graphUnforcedErrorLobbhPlayer1.layoutParams.width=0
                    binding.graphUnforcedErrorLobbhPlayer2.layoutParams.width=0
                }
                else if(app.unforcederrorlobBH1>app.unforcederrorlobBH2){
                    binding.graphUnforcedErrorLobbhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrorlobBH2.toDouble() * 100 /app.unforcederrorlobBH1.toDouble()).toInt()
                    binding.graphUnforcedErrorLobbhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphUnforcedErrorLobbhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrorlobBH1.toDouble() * 100 / app.unforcederrorlobBH2.toDouble()).toInt()
                    binding.graphUnforcedErrorLobbhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                binding.unforcedErrorDropshotfhPlayer1.text = app.unforcederrordropshotFH1.toString()
                binding.unforcedErrorDropshotfhPlayer2.text = app.unforcederrordropshotFH2.toString()

                if(app.unforcederrordropshotFH1==app.unforcederrordropshotFH2 && app.unforcederrordropshotFH1==0){
                    binding.graphUnforcedErrorDropshotfhPlayer1.layoutParams.width=0
                    binding.graphUnforcedErrorDropshotfhPlayer2.layoutParams.width=0
                }
                else if(app.unforcederrordropshotFH1>app.unforcederrordropshotFH2){
                    binding.graphUnforcedErrorDropshotfhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrordropshotFH2.toDouble() * 100 /app.unforcederrordropshotFH1.toDouble()).toInt()
                    binding.graphUnforcedErrorDropshotfhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphUnforcedErrorDropshotfhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrordropshotFH1.toDouble() * 100 / app.unforcederrordropshotFH2.toDouble()).toInt()
                    binding.graphUnforcedErrorDropshotfhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                binding.unforcedErrorDropshotbhPlayer1.text = app.unforcederrordropshotBH1.toString()
                binding.unforcedErrorDropshotbhPlayer2.text = app.unforcederrordropshotBH2.toString()

                if(app.unforcederrordropshotBH1==app.unforcederrordropshotBH2 && app.unforcederrordropshotBH1==0){
                    binding.graphUnforcedErrorDropshotbhPlayer1.layoutParams.width=0
                    binding.graphUnforcedErrorDropshotbhPlayer2.layoutParams.width=0
                }
                else if(app.unforcederrordropshotBH1>app.unforcederrordropshotBH2){
                    binding.graphUnforcedErrorDropshotbhPlayer1.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrordropshotBH2.toDouble() * 100 /app.unforcederrordropshotBH1.toDouble()).toInt()
                    binding.graphUnforcedErrorDropshotbhPlayer2.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer2).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }
                else{
                    binding.graphUnforcedErrorDropshotbhPlayer2.layoutParams.width=(100 * scale + 0.5f).toInt()
                    val wyn = (app.unforcederrordropshotBH1.toDouble() * 100 / app.unforcederrordropshotBH2.toDouble()).toInt()
                    binding.graphUnforcedErrorDropshotbhPlayer1.layoutParams.width=(wyn * scale + 0.5f).toInt()
                    /*binding.graphAcePlayer1).layoutParams.width=(50 * scale + 0.5f).toInt()*/
                }

                //odswiazanie
                binding.graphUnforcedErrorGroundfhPlayer1.requestLayout()
                binding.graphUnforcedErrorGroundfhPlayer2.requestLayout()
                binding.graphUnforcedErrorGroundbhPlayer1.requestLayout()
                binding.graphUnforcedErrorGroundbhPlayer2.requestLayout()
                binding.graphUnforcedErrorVolleyfhPlayer1.requestLayout()
                binding.graphUnforcedErrorVolleyfhPlayer2.requestLayout()
                binding.graphUnforcedErrorVolleybhPlayer1.requestLayout()
                binding.graphUnforcedErrorVolleybhPlayer2.requestLayout()
                binding.graphUnforcedErrorSlicefhPlayer1.requestLayout()
                binding.graphUnforcedErrorSlicefhPlayer2.requestLayout()
                binding.graphUnforcedErrorSlicebhPlayer1.requestLayout()
                binding.graphUnforcedErrorSlicebhPlayer2.requestLayout()
                binding.graphUnforcedErrorSmashfhPlayer1.requestLayout()
                binding.graphUnforcedErrorSmashfhPlayer2.requestLayout()
                binding.graphUnforcedErrorSmashbhPlayer1.requestLayout()
                binding.graphUnforcedErrorSmashbhPlayer2.requestLayout()
                binding.graphUnforcedErrorLobfhPlayer1.requestLayout()
                binding.graphUnforcedErrorLobfhPlayer2.requestLayout()
                binding.graphUnforcedErrorLobbhPlayer1.requestLayout()
                binding.graphUnforcedErrorLobbhPlayer2.requestLayout()
                binding.graphUnforcedErrorDropshotfhPlayer1.requestLayout()
                binding.graphUnforcedErrorDropshotfhPlayer2.requestLayout()
                binding.graphUnforcedErrorDropshotbhPlayer1.requestLayout()
                binding.graphUnforcedErrorDropshotbhPlayer2.requestLayout()
            } //UNFORCED ERRORS
        }
    }

    fun refreshNumbers(app: StatsClass, position: Int) {
        when (position){
            0 -> {
                app.totalpoints1 = 0   //ostatecznie kto ile wygrał punktów
                app.totalpoints2 = 0

                app.firstserve1 =
                    0 //ilosc ogolnie zagranych 1 serwisow (firstservein1 + secondserve1)
                app.firstserve2 = 0
                app.firstservein1 =
                    0 //ilosc trafionych 1 serwisow (w procentach: firstservein1/firstserve1 * 100%)
                app.firstservein2 = 0

                app.secondserve1 =
                    0 //ilosc ogolnie zagranych 2 serwisow (secondservein1 + doublefault1)
                app.secondserve2 = 0
                app.secondservein1 =
                    0 //ilosc trafionych 2 serwisow (w procentach: secondservein1/secondserve1 * 100%)
                app.secondservein2 = 0

                app.ace1 = 0 //ilosc zagranych asow playera 1
                app.ace2 = 0
                app.doublefault1 = 0 //ilosc podwojnych bledow
                app.doublefault2 = 0
            }
            1 -> {
                app.returnwinnerFH1 = 0
                app.returnwinnerFH2 = 0
                app.returnwinnerBH1 = 0
                app.returnwinnerBH2 = 0

                app.returnerrorFH1 = 0
                app.returnerrorFH2 = 0
                app.returnerrorBH1 = 0
                app.returnerrorBH2 = 0
            }
            2 -> {
                app.winnergroundFH1 = 0
                app.winnergroundFH2 = 0
                app.winnergroundBH1 = 0
                app.winnergroundBH2 = 0

                app.winnersliceFH1 = 0
                app.winnersliceFH2 = 0
                app.winnersliceBH1 = 0
                app.winnersliceBH2 = 0

                app.winnervolleyFH1 = 0
                app.winnervolleyFH2 = 0
                app.winnervolleyBH1 = 0
                app.winnervolleyBH2 = 0

                app.winnersmashFH1 = 0
                app.winnersmashFH2 = 0
                app.winnersmashBH1 = 0
                app.winnersmashBH2 = 0

                app.winnerlobFH1 = 0
                app.winnerlobFH2 = 0
                app.winnerlobBH1 = 0
                app.winnerlobBH2 = 0

                app.winnerdropshotFH1 = 0
                app.winnerdropshotFH2 = 0
                app.winnerdropshotBH1 = 0
                app.winnerdropshotBH2 = 0
            }
            3 -> {
                app.forcederrorgroundFH1 = 0
                app.forcederrorgroundFH2 = 0
                app.forcederrorgroundBH1 = 0
                app.forcederrorgroundBH2 = 0

                app.forcederrorsliceFH1 = 0
                app.forcederrorsliceFH2 = 0
                app.forcederrorsliceBH1 = 0
                app.forcederrorsliceBH2 = 0

                app.forcederrorvolleyFH1 = 0
                app.forcederrorvolleyFH2 = 0
                app.forcederrorvolleyBH1 = 0
                app.forcederrorvolleyBH2 = 0

                app.forcederrorsmashFH1 = 0
                app.forcederrorsmashFH2 = 0
                app.forcederrorsmashBH1 = 0
                app.forcederrorsmashBH2 = 0

                app.forcederrorlobFH1 = 0
                app.forcederrorlobFH2 = 0
                app.forcederrorlobBH1 = 0
                app.forcederrorlobBH2 = 0

                app.forcederrordropshotFH1 = 0
                app.forcederrordropshotFH2 = 0
                app.forcederrordropshotBH1 = 0
                app.forcederrordropshotBH2 = 0
            }
            4 -> {
                app.unforcederrorgroundFH1 = 0
                app.unforcederrorgroundFH2 = 0
                app.unforcederrorgroundBH1 = 0
                app.unforcederrorgroundBH2 = 0

                app.unforcederrorsliceFH1 = 0
                app.unforcederrorsliceFH2 = 0
                app.unforcederrorsliceBH1 = 0
                app.unforcederrorsliceBH2 = 0

                app.unforcederrorvolleyFH1 = 0
                app.unforcederrorvolleyFH2 = 0
                app.unforcederrorvolleyBH1 = 0
                app.unforcederrorvolleyBH2 = 0

                app.unforcederrorsmashFH1 = 0
                app.unforcederrorsmashFH2 = 0
                app.unforcederrorsmashBH1 = 0
                app.unforcederrorsmashBH2 = 0

                app.unforcederrorlobFH1 = 0
                app.unforcederrorlobFH2 = 0
                app.unforcederrorlobBH1 = 0
                app.unforcederrorlobBH2 = 0

                app.unforcederrordropshotFH1 = 0
                app.unforcederrordropshotFH2 = 0
                app.unforcederrordropshotBH1 = 0
                app.unforcederrordropshotBH2 = 0
            }
        }
    }
}
