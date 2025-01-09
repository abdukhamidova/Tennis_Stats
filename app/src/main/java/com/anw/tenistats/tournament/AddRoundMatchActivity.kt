package com.anw.tenistats.tournament

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.anw.tenistats.adapter.MatchAdapter
import com.anw.tenistats.databinding.ActivityViewMatchesBinding
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.anw.tenistats.stats.MatchViewClass
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddRoundMatchActivity : AppCompatActivity(), MatchAdapter.OnItemClickListener{
    private lateinit var binding: ActivityViewMatchesBinding
    private lateinit var dbref: DatabaseReference
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var matchRecyclerView: RecyclerView
    private lateinit var matchArrayList: ArrayList<MatchViewClass>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var adapter: MatchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityViewMatchesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        //region ---MENU---
        drawerLayout = findViewById(R.id.drawer_layout)
        val menu = findViewById<ImageButton>(R.id.buttonMenu)
        val navigationView = findViewById<NavigationView>(R.id.navigationViewMenu)
        val headerView = navigationView.getHeaderView(0)
        menu.setOnClickListener{
            drawerLayout.open()
        }
        navigationDrawerHelper = NavigationDrawerHelper(this)
        navigationDrawerHelper.setupNavigationDrawer(drawerLayout, navigationView, firebaseAuth)
        val backButton = findViewById<ImageButton>(R.id.buttonUndo)
        backButton.visibility = View.GONE

        val userEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        if(userEmail.isNotEmpty()) {
            headerView.findViewById<TextView>(R.id.textViewUserEmail).text = userEmail
        }
        else {
            findViewById<TextView>(R.id.textViewUserEmail).text = resources.getString(R.string.user_email)
        }
        //endregion

        // Pobierz dane z Intentu
        var tournamentId = intent.getStringExtra("tournamentId")
        var matchNumber = intent.getStringExtra("matchNumber")

        firebaseAuth = FirebaseAuth.getInstance()

        matchRecyclerView = binding.matchList
        matchRecyclerView.layoutManager = LinearLayoutManager(this)
        matchRecyclerView.setHasFixedSize(true)

        matchArrayList = arrayListOf()

        adapter = MatchAdapter(matchArrayList, this, tournamentId, matchNumber)
        adapter.setOnItemClickListener(this)
        matchRecyclerView.adapter = adapter

        getMatchData(tournamentId)

        val searchEditText = binding.searchEditText
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun getMatchData(tournamentId: String?) {
        val user = firebaseAuth.currentUser?.uid
        dbref = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString())
            .child("Matches")

        //filt meczow
        getTournamentStartDate(tournamentId){tournamentStartDate ->
            dbref.addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        matchArrayList.clear()

                        for (matchSnapshot in snapshot.children.reversed()) {
                            // Pobieramy mecz do obiektu MatchViewClass
                            val match = matchSnapshot.getValue(MatchViewClass::class.java)
                            if (match != null) {

                                // Sprawdzamy, czy mecz nie ma podwęzłów "id_tournament" i "match_number"
                                val hasIdTournament = matchSnapshot.hasChild("id_tournament")
                                val hasMatchNumber = matchSnapshot.hasChild("match_number")
                                val matchDate = matchSnapshot.child("data").getValue(Long::class.java) ?: 0L

                                // Jeśli mecz ma te podwęzły lub data meczu jest wcześniejsza niż data turnieju, pomijamy go
                                if ((hasIdTournament || hasMatchNumber) || (matchDate < tournamentStartDate ?: Long.MAX_VALUE)) {
                                    continue
                                }

                                // Jeśli mecz nie ma tych podwęzłów, dodajemy go do listy
                                binding.textViewNotFound.visibility = View.INVISIBLE
                                matchArrayList.add(match)
                            } else {
                                binding.textViewNotFound.visibility = View.VISIBLE
                            }
                        }

                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

    }

    fun getTournamentStartDate(tournamentId: String?, callback: (Long?) -> Unit) {
        val database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("Tournaments")

        database.orderByChild("id").equalTo(tournamentId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (tournamentSnapshot in snapshot.children) {
                    val startDate = tournamentSnapshot.child("startDate").getValue(Long::class.java)
                    callback(startDate)
                    return
                }
                callback(null) // Gdy turniej o podanym ID nie zostanie znaleziony
            }

            override fun onCancelled(error: DatabaseError) {
                println("Database error: ${'$'}error.message")
                callback(null)
            }
        })
    }


    override fun onItemClick(matchView: MatchViewClass) {
        // Zmień zachowanie tutaj
    }

}