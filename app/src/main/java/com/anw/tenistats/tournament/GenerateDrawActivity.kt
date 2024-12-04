package com.anw.tenistats.tournament

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
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

        if(drawSize == "null" || drawSize == "None" || drawSize.isNullOrEmpty()){
            binding.relativeLayoutRound.visibility = View.GONE
            binding.linearLayoutNoDraw.visibility = View.VISIBLE
            binding.scrollView.visibility = View.GONE
            binding.buttonShowDetails.setOnClickListener {
                val intent = Intent(this, TournamentDetailsActivity::class.java)
                intent.putExtra("tournament_id", tournamentId)
                startActivity(intent)
            }
        }
        else{
            binding.relativeLayoutRound.visibility = View.VISIBLE
            binding.linearLayoutNoDraw.visibility = View.GONE
            binding.scrollView.visibility = View.VISIBLE
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
        // Set reference to the correct Firebase location
        database = FirebaseDatabase.getInstance(
            "https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/"
        ).getReference("Tournaments").child(tournamentId)

        // Fetch data from Firebase
        database.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                val roundsList = mutableListOf<Round>()
                val matchSnapshots = snapshot.children.toList() // Get all children (matches)

                // Calculate the number of matches in the current round
                val itemsCount = if (roundNumber == totalRounds) 1 else (drawSizeInt / (2.0).pow((roundNumber + 1))).toInt()

                var j = ((2.0).pow(totalRounds - roundNumber)).toInt() // Set the starting match number

                Log.d("FirebaseData1", "Snapshot size: ${matchSnapshots.size}")

                var matchesLoaded = 0

                for (i in 0 until itemsCount) {
                    val match1Index = j + 2 * i
                    val match2Index = match1Index + 1

                    // If it's the final round, fetch only one match
                    if (roundNumber == totalRounds) {
                        parseMatch(match1Index) { match1 ->
                            if (match1 != null) {
                                roundsList.add(Round(match1, match1)) // Add the same match twice (final match)
                            }
                            matchesLoaded++
                            if (matchesLoaded == itemsCount) {
                                updateUI(roundsList) // Update UI after the match is loaded
                            }
                        }
                    } else {
                        // Parse both matches if it's not the final round
                        parseMatch(match1Index) { match1 ->
                            if (match1 != null) {
                                parseMatch(match2Index) { match2 ->
                                    if (match2 != null) {
                                        roundsList.add(Round(match1, match2)) // Add both matches as a pair
                                    }
                                    matchesLoaded++
                                    if (matchesLoaded == itemsCount) {
                                        updateUI(roundsList) // Update UI after all matches are loaded
                                    }
                                }
                            } else {
                                matchesLoaded++
                                if (matchesLoaded == itemsCount) {
                                    updateUI(roundsList) // Update UI if no match is found
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
                Log.d("FirebaseData", "Error fetching data: ${error.message}")
            }
        })

        // Update round label
        val a = (drawSizeInt / 2.0.pow(roundNumber)).toInt()
        if(a==1)
            binding.textViewRound.text = "Finale"
        else
            binding.textViewRound.text = "1/$a"
    }

    private fun parseMatch(no: Int, callback: (TournamentMatchDataClass) -> Unit) {
        // Odczytujemy dane z węzła odpowiadającego numerowi meczu
        val matchSnapshotRef = FirebaseDatabase.getInstance(
            "https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/"
        ).getReference("Tournaments").child(tournamentId).child(no.toString())

        matchSnapshotRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Sprawdzamy, czy snapshot zawiera dane
                if (snapshot.exists()) {
                    // Pobieramy dane meczu i zamieniamy "None" na pusty ciąg
                    val matchId = snapshot.child("matchId").getValue(String::class.java)?.let {
                        if (it == "None") "" else it
                    } ?: ""
                    val player1 = snapshot.child("player1").getValue(String::class.java)?.let {
                        if (it == "None") "" else it
                    } ?: ""
                    val player2 = snapshot.child("player2").getValue(String::class.java)?.let {
                        if (it == "None") "" else it
                    } ?: ""
                    val winner = snapshot.child("winner").getValue(String::class.java)?.let {
                        if (it == "None") "" else it
                    } ?: ""
                    val set1p1 = snapshot.child("set1p1").getValue(String::class.java)?.let {
                        if (it == "None") "" else it
                    } ?: ""
                    val set2p1 = snapshot.child("set2p1").getValue(String::class.java)?.let {
                        if (it == "None") "" else it
                    } ?: ""
                    val set3p1 = snapshot.child("set3p1").getValue(String::class.java)?.let {
                        if (it == "None") "" else it
                    } ?: ""
                    val set1p2 = snapshot.child("set1p2").getValue(String::class.java)?.let {
                        if (it == "None") "" else it
                    } ?: ""
                    val set2p2 = snapshot.child("set2p2").getValue(String::class.java)?.let {
                        if (it == "None") "" else it
                    } ?: ""
                    val set3p2 = snapshot.child("set3p2").getValue(String::class.java)?.let {
                        if (it == "None") "" else it
                    } ?: ""

                    // Logi debugujące
                    Log.d("FirebaseData", "Parsing match $no: matchId=$matchId, player1=$player1, player2=$player2, winner=$winner, set1p1=$set1p1, set1p2=$set1p2")

                    // Tworzymy obiekt TournamentMatchDataClass i przekazujemy go przez callback
                    val matchData = TournamentMatchDataClass(
                        number = no.toString(),
                        matchId = matchId,
                        player1 = player1,
                        player2 = player2,
                        winner = winner,
                        set1p1 = set1p1,
                        set2p1 = set2p1,
                        set3p1 = set3p1,
                        set1p2 = set1p2,
                        set2p2 = set2p2,
                        set3p2 = set3p2
                    )

                    // Wywołujemy callback z obiektem meczu
                    callback(matchData)
                } else {
                    // Jeśli dane meczu nie istnieją, logujemy błąd
                    Log.d("FirebaseData", "No data found for match $no")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsługuje błąd odczytu
                Log.d("FirebaseData", "Error reading match $no: ${error.message}")
            }
        })
    }

    private fun updateUI(roundsList: List<Round>) {
        // Sprawdzamy, czy lista rund jest pusta
        Log.d("FirebaseData2", "Rounds list: $roundsList")
        if (roundsList.isEmpty()) {
            binding.textViewNotFound.visibility = View.VISIBLE
        } else {
            binding.textViewNotFound.visibility = View.INVISIBLE
        }

        // Zaktualizowanie adaptera
        roundsArrayList.clear() // Czyszczenie starych rund
        roundsArrayList.addAll(roundsList) // Dodawanie nowych rund
        adapter.updateData(roundsList) // Przekazanie danych do adaptera
        adapter.notifyDataSetChanged() // Powiadomienie adaptera o zmianach
    }

}