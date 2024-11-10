package com.anw.tenistats

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListPopupWindow
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.anw.tenistats.com.anw.tenistats.ResumeOrStatsDialogActivity
import com.anw.tenistats.databinding.ActivityHead2HeadBinding
import com.anw.tenistats.ui.theme.NavigationDrawerHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Head2HeadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHead2HeadBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private val playersList = mutableListOf<String>()
    private lateinit var spinner1: Spinner
    private lateinit var spinner2: Spinner
    private val finishedMatches = mutableListOf<String>()
    private val inProgressMatches = mutableListOf<String>()
    private val finishedDates = mutableListOf<Long>()
    private val inProgressDates = mutableListOf<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHead2HeadBinding.inflate(layoutInflater)
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
        backButton.visibility = View.GONE

        val userEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        val userEmailView = headerView.findViewById<TextView>(R.id.textViewUserEmail)
        if(userEmail.isNotEmpty()) {
            userEmailView.text = userEmail
        }else {
            userEmailView.text = "user_email@smth.com"
        }
    //MENU

    // Pobierz listę graczy z bazy danych
        val user = firebaseAuth.currentUser?.uid
        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(user.toString()).child("Players")

        database.orderByChild("active").equalTo(true).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                playersList.clear()
                for (playerSnapshot in snapshot.children) {
                    val playerName = playerSnapshot.key
                    playerName?.let { playersList.add(it)}
                }
                setSpinners()
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsługa błędów odczytu danych z bazy danych
                Toast.makeText(
                    this@Head2HeadActivity,
                    "Failed to read players from database",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
    private fun setData(){
        var player1 = binding.autoNameP1?.selectedItem.toString() //pobiera Imię i Nazwisko gracza
        var player2 = binding.autoNameP2?.selectedItem.toString()
        if (player1.isEmpty() || player2.isEmpty()) {
            Toast.makeText(this, "Don't leave empty fields.", Toast.LENGTH_SHORT).show()
            binding.layoutH2H.visibility = View.GONE
        } else if (player1 == player2) {
            Toast.makeText(
                this,
                "You chose the same player. Choose another opponent.",
                Toast.LENGTH_SHORT
            ).show()
            binding.layoutH2H.visibility = View.GONE
        } else {
            binding.layoutH2H.visibility = View.VISIBLE
            //czyszczenie listy przed kolejnym liczeniem
            finishedMatches.clear()
            finishedDates.clear()
            inProgressMatches.clear()
            inProgressDates.clear()
            fetchMatches()
        }
    }

    private fun setSpinners() {
        spinner1 = binding.autoNameP1
        val adapter1 = ArrayAdapter(this, R.layout.spinner_item_h2h_left_base, playersList)//playerList)
        adapter1.setDropDownViewResource(R.layout.spinner_item_h2h_left)
        spinner1.adapter = adapter1

        spinner2 = binding.autoNameP2
        val adapter2 = ArrayAdapter(this, R.layout.spinner_item_h2h_right_base, playersList)//playersList)
        adapter2.setDropDownViewResource(R.layout.spinner_item_h2h_right)
        spinner2.adapter = adapter2

        //lista rozwijana player1
        spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                setData()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Code to perform if nothing is selected

            }
        }
        //lista rozwijana player1

        //lista rozwijana player2
        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                setData()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Code to perform if nothing is selected
            }
        }
        //lista rozwijana player2
    }

    private fun fetchMatches() {
        var matches_won_pl1 = 0
        var matches_won_pl2 = 0

        val user = firebaseAuth.currentUser?.uid
        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(user.toString()).child("Matches")

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Loop through each match and process data
                for (matchSnapshot in dataSnapshot.children) {
                    // Convert snapshot to Match object
                    val match = matchSnapshot.getValue(Match::class.java)

                    match?.let {
                        if ((it.player1 == binding.autoNameP1.selectedItem && it.player2 == binding.autoNameP2.selectedItem)
                            || (it.player1 == binding.autoNameP2.selectedItem && it.player2 == binding.autoNameP1.selectedItem)) {

                            val isFirstPl1 = (it.player1 == binding.autoNameP1.selectedItem && it.player2 == binding.autoNameP2.selectedItem)
                            val formattedDate = formatDate(it.data)
                            val score = formatScore(it,isFirstPl1)
                            if (it.winner != null) {
                                // Finished match: display date and score
                                //finishedMatches.add("$formattedDate \t $score")
                                if(it.winner == binding.autoNameP1.selectedItem){
                                    matches_won_pl1++
                                    finishedMatches.add("◁ $formattedDate \t $score")
                                }
                                else{
                                    matches_won_pl2++
                                    finishedMatches.add("▶ $formattedDate \t $score")
                                }
                                finishedDates.add(it.data)
                            } else {
                                // In Progress: include points in score
                                val scoreWithPoints = if(isFirstPl1)
                                    { "$score (${it.pkt1}:${it.pkt2})" }
                                    else{ "$score (${it.pkt2}:${it.pkt1})" }
                                inProgressMatches.add("$formattedDate \t $scoreWithPoints")
                                inProgressDates.add(it.data)
                            }
                        }
                    }
                }
                setMatchesWonGraphs(matches_won_pl1, matches_won_pl2)
                //displayMatches()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@Head2HeadActivity,
                    "Failed to read matches from database",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setMatchesWonGraphs(matches_won_pl1: Int, matches_won_pl2: Int) {
        val scale = resources.displayMetrics.density
        binding.matchesWonPlayer1.text = matches_won_pl1.toString()
        binding.matchesWonPlayer2.text = matches_won_pl2.toString()

        val minBarWidth = (4 * scale + 0.5f).toInt()

        if (matches_won_pl1 == matches_won_pl2 && matches_won_pl1 == 0) {
            binding.graphMatchesWonPlayer1.layoutParams.width = 0
            binding.graphMatchesWonPlayer2.layoutParams.width = 0
        } else if (matches_won_pl1 > matches_won_pl2) {
            binding.graphMatchesWonPlayer1.layoutParams.width = (100 * scale + 0.5f).toInt()
            val proportionWidth = ((matches_won_pl2.toDouble() / matches_won_pl1.toDouble()) * 100 * scale + 0.5f).toInt()
            binding.graphMatchesWonPlayer2.layoutParams.width = maxOf(proportionWidth, minBarWidth)
        } else {
            binding.graphMatchesWonPlayer2.layoutParams.width = (100 * scale + 0.5f).toInt()
            val proportionWidth = ((matches_won_pl1.toDouble() / matches_won_pl2.toDouble()) * 100 * scale + 0.5f).toInt()
            binding.graphMatchesWonPlayer1.layoutParams.width = maxOf(proportionWidth, minBarWidth)
        }
        binding.graphMatchesWonPlayer1.requestLayout()
        binding.graphMatchesWonPlayer2.requestLayout()
        displayMatches()
    }

    private fun formatDate(dateInMillis: Long): String {
        // Format date from milliseconds to a readable date format
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date(dateInMillis))
    }

    private fun formatScore(match: Match,isFirstPl1: Boolean): String {
        if(isFirstPl1) {
            // Build score string (without points)
            if (match.set2p1 == "")
                return "${match.set1p1}:${match.set1p2}"
            if (match.set3p1 == "")
                return "${match.set1p1}:${match.set1p2} | ${match.set2p1}:${match.set2p2}"
            return "${match.set1p1}:${match.set1p2} | ${match.set2p1}:${match.set2p2} | ${match.set3p1}:${match.set3p2}"
        }
        if (match.set2p1 == "")
            return "${match.set1p2}:${match.set1p1}"
        if (match.set3p1 == "")
            return "${match.set1p2}:${match.set1p1} | ${match.set2p2}:${match.set2p1}"
        return "${match.set1p2}:${match.set1p1} | ${match.set2p2}:${match.set2p1} | ${match.set3p2}:${match.set3p1}"
    }

    private fun displayMatches() {
        // Combine finished and in-progress matches
        //val finished = listOf("Finished") + finishedMatches + listOf("In Progress") + inProgressMatches

        // Set up the ListView with ArrayAdapter
        val adapter1 = ArrayAdapter(this, android.R.layout.simple_list_item_1, finishedMatches)
        binding.finishedList.adapter = adapter1
        binding.finishedList.setOnItemClickListener { parent, view, position, id ->
            val dateInMillis = finishedDates.get(position)
            val ResumeOrStatsDialog = ResumeOrStatsDialogActivity(this)
            ResumeOrStatsDialog.show(
                dateInMillis
            )
        }

        val adapter2 = ArrayAdapter(this, android.R.layout.simple_list_item_1, inProgressMatches)
        binding.inProgressList.adapter = adapter2
        binding.inProgressList.setOnItemClickListener { parent, view, position, id ->
            val dateInMillis = inProgressDates.get(position)
            val ResumeOrStatsDialog = ResumeOrStatsDialogActivity(this)
            ResumeOrStatsDialog.show(
                dateInMillis
            )
        }
    }
}