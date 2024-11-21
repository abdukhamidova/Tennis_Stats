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
import com.anw.tenistats.adapter.MatchAdapter
import com.anw.tenistats.adapter.PlayerAdapter
import com.anw.tenistats.adapter.TournamentAdapter
import com.anw.tenistats.databinding.ActivityViewTournamentsBinding
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.anw.tenistats.player.PlayerView
import com.anw.tenistats.stats.MatchViewClass
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewTournamentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewTournamentsBinding
    private lateinit var database: DatabaseReference
    private lateinit var tournamentlayerRecyclerView: RecyclerView
    private lateinit var tournamentArrayList: ArrayList<TournamentClass>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var adapter: TournamentAdapter
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private var isAdapterSet = false
    private lateinit var noTournamentsFoundTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityViewTournamentsBinding.inflate(layoutInflater);
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()

        //------------ MENU
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
        //------------ MENU

        binding.buttonAddTournament.setOnClickListener {
            startActivity(Intent(this, AddTournamentActivity::class.java))
        }

        tournamentlayerRecyclerView = binding.tournamentsList
        tournamentlayerRecyclerView.layoutManager = LinearLayoutManager(this)
        tournamentlayerRecyclerView.setHasFixedSize(true)

        tournamentArrayList = arrayListOf()
        adapter = TournamentAdapter(tournamentArrayList)
        tournamentlayerRecyclerView.adapter = adapter

        getTournamentData()
        tournamentlayerRecyclerView.isEnabled = true
    }

    private fun getTournamentData() {
        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Tournaments")

        database.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    tournamentArrayList.clear() // Wyczyść listę przed dodaniem nowych danych

                    for (tournamentSnapshot in snapshot.children.reversed()) { // Iteruj odwrotnie, aby najnowsze mecze były na początku
                        val tournament = tournamentSnapshot.getValue(TournamentClass::class.java)
                        if(tournament != null){
                            tournament?.id = tournamentSnapshot.key.toString()
                            tournamentArrayList.add(tournament)
                        }
                    }
                    if (tournamentArrayList.isEmpty()) {
                        binding.textViewNotFound.visibility = View.VISIBLE
                    } else {
                        binding.textViewNotFound.visibility = View.INVISIBLE
                    }

                    // Poinformuj adapter o zmianach w danych
                    adapter.notifyDataSetChanged()
                }else {
                    binding.textViewNotFound.visibility = View.VISIBLE
                }
            }


            override fun onCancelled(error: DatabaseError) {
                // Obsłużenie błędu zapytania do bazy danych
            }
        })
    }
}
