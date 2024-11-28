package com.anw.tenistats.tournament

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anw.tenistats.R
import com.anw.tenistats.adapter.TournamentRoundAdapter
import com.anw.tenistats.databinding.ActivityGenerateDrawBinding
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.google.android.material.navigation.NavigationView
import com.google.android.play.integrity.internal.i
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.log2
import kotlin.math.pow

class GenerateDrawActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGenerateDrawBinding
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var tournamentId: String
    private lateinit var drawSize: String
    private var drawSizeInt: Int = 0
    private var totalRounds: Int = 0
    private lateinit var drawRecyclerView: RecyclerView
    private lateinit var roundsArrayList: ArrayList<Round>
    private lateinit var adapter: TournamentRoundAdapter
    private var roundNumber: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGenerateDrawBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()

        //region ---MENU---
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val menu = findViewById<ImageButton>(R.id.buttonMenu)
        val navigationView = findViewById<NavigationView>(R.id.navigationViewMenu)
        val headerView = navigationView.getHeaderView(0)

        menu.setOnClickListener {
            drawerLayout.open()
        }
        navigationDrawerHelper = NavigationDrawerHelper(this)
        navigationDrawerHelper.setupNavigationDrawer(drawerLayout, navigationView, firebaseAuth)
        val backButton = findViewById<ImageButton>(R.id.buttonUndo)
        backButton.visibility = View.GONE

        val userEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        if (userEmail.isNotEmpty()) {
            headerView.findViewById<TextView>(R.id.textViewUserEmail).text = userEmail
        } else {
            findViewById<TextView>(R.id.textViewUserEmail).text =
                resources.getString(R.string.user_email)
        }
        //endregion

        tournamentId = intent.getStringExtra("tournament_id").toString()
        drawSize = intent.getStringExtra("draw_size").toString()

        drawRecyclerView = binding.roundList
        drawRecyclerView.layoutManager = LinearLayoutManager(this)
        drawRecyclerView.setHasFixedSize(true)

        if(drawSize == "null" || drawSize == "None"){
            binding.relativeLayoutRound.visibility = View.INVISIBLE
            binding.linearLayoutNoDraw.visibility = View.VISIBLE
            binding.buttonShowDetails.setOnClickListener {
                val intent = Intent(this, TournamentDetailsActivity::class.java)
                intent.putExtra("tournament_id", tournamentId)
                startActivity(intent)
            }
        }
        else{
            binding.relativeLayoutRound.visibility = View.VISIBLE
            binding.linearLayoutNoDraw.visibility = View.INVISIBLE
            binding.roundList.visibility = View.VISIBLE
            //val round = drawSize.toInt()/2
            //binding.textViewRound.text = "1/$round"

            drawSizeInt = drawSize.toIntOrNull() ?: 0 //ilosc zawodnikow w turnieju
            totalRounds = log2(drawSize.toDouble()).toInt() //ilosc rund

            binding.imageButtonNextRound.setOnClickListener {
                if (roundNumber < totalRounds) {
                    roundNumber++ // Increment the round number
                    binding.imageButtonPreviousRound.visibility = View.VISIBLE

                    if (roundNumber == totalRounds) {
                        binding.imageButtonNextRound.visibility = View.INVISIBLE
                    }

                    // Re-fetch and update the data for the new round
                    getMatchesInTournamentData()
                }
            }
            binding.imageButtonPreviousRound.setOnClickListener {
                if (roundNumber > 1) {
                    roundNumber-- // Decrement the round number
                    binding.imageButtonNextRound.visibility = View.VISIBLE

                    if (roundNumber == 1) {
                        binding.imageButtonPreviousRound.visibility = View.INVISIBLE
                    }

                    // Re-fetch and update the data for the new round
                    getMatchesInTournamentData()
                }
            }

            getMatchesInTournamentData()

            roundsArrayList = arrayListOf()
            adapter = TournamentRoundAdapter(roundsArrayList){ match ->
                val intent = Intent(this, EditMatchActivity::class.java)
                intent.putExtra("tournament_id", tournamentId)
                intent.putExtra("match_number", match.number)
                startActivity(intent)
            }
            drawRecyclerView.adapter = adapter
        }
    }
    private fun getMatchesInTournamentData() {
        database = FirebaseDatabase.getInstance(
            "https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/"
        ).getReference("Tournaments").child(tournamentId)

        database.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                val roundsList = mutableListOf<Round>()
                val matchSnapshots = snapshot.children.toList()

                val itemsCount = (drawSizeInt/((2.0)).pow((roundNumber + 1))).toInt() //ilosc itemow (x2 mecze)

                //var j = (totalRounds-roundNumber).toDouble().pow(2).toInt()
                var j = ((2.0).pow(totalRounds-roundNumber)).toInt()
                // Process matches in pairs to create rounds
                for (i in 0 until itemsCount) {
                    val match1 = matchSnapshots.getOrNull(i)?.let { parseMatch(it,j) }
                    val match2 = matchSnapshots.getOrNull(i + 1)?.let { parseMatch(it,j+1) }

                    if (match1 != null && match2 != null) {
                        roundsList.add(Round(match1, match2))
                    }
                    j+=2
                }

                if (roundsList.isEmpty()) {
                    binding.textViewNotFound.visibility = View.VISIBLE
                } else {
                    binding.textViewNotFound.visibility = View.INVISIBLE
                }

                roundsArrayList.clear() // Clear the old rounds
                roundsArrayList.addAll(roundsList) // Add the new rounds
                adapter.updateData(roundsList) // Pass data to adapter
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        val a = (drawSizeInt / 2.0.pow(roundNumber)).toInt()
        binding.textViewRound.text = "1/$a"


    }

    private fun parseMatch(snapshot: DataSnapshot,no: Int): TournamentMatchDataClass {
        return TournamentMatchDataClass(
            number = no.toString(),
            matchId = snapshot.child("matchId").getValue(String::class.java).orEmpty(),
            player1Name = snapshot.child("player1").getValue(String::class.java).orEmpty(),
            player2Name = snapshot.child("player2").getValue(String::class.java).orEmpty(),
            winner = snapshot.child("winner").getValue(String::class.java).toString(),
            player1Set1 = snapshot.child("player1Set1").getValue(String::class.java).orEmpty(),
            player1Set2 = snapshot.child("player1Set2").getValue(String::class.java).orEmpty(),
            player1Set3 = snapshot.child("player1Set3").getValue(String::class.java).orEmpty(),
            player2Set1 = snapshot.child("player2Set1").getValue(String::class.java).orEmpty(),
            player2Set2 = snapshot.child("player2Set2").getValue(String::class.java).orEmpty(),
            player2Set3 = snapshot.child("player2Set3").getValue(String::class.java).orEmpty()
        )
    }
}