package com.anw.tenistats.tournament

import android.annotation.SuppressLint
import android.content.Intent
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
import com.anw.tenistats.CalendarActivity
import com.anw.tenistats.R
import com.anw.tenistats.adapter.TournamentAdapter
import com.anw.tenistats.databinding.ActivityViewTournamentsBinding
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewTournamentsActivity : AppCompatActivity(), TournamentAdapter.OnItemClickListener {
    private lateinit var binding: ActivityViewTournamentsBinding
    private lateinit var database: DatabaseReference
    private lateinit var tournamentRecyclerView: RecyclerView
    private lateinit var tournamentArrayList: ArrayList<TournamentDataClass>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var adapter: TournamentAdapter
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var selectedVenueList: Array<String>
    private lateinit var selectedCountryList: Array<String>
    private var selectedDate: Long? = null
    private lateinit var selectedSurfaceList: Array<String>

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
        backButton.setImageResource(R.drawable.icon_calendar)
        backButton.setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }

        val userEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        if (userEmail.isNotEmpty()) {
            headerView.findViewById<TextView>(R.id.textViewUserEmail).text = userEmail
        } else {
            findViewById<TextView>(R.id.textViewUserEmail).text =
                resources.getString(R.string.user_email)
        }
        //endregion

        selectedVenueList = intent.getStringArrayExtra("venueFilter") ?: emptyArray()
        selectedCountryList = intent.getStringArrayExtra("countryFilter") ?: emptyArray()
        selectedDate = intent.getLongExtra("dateFilter", 0L)
        selectedSurfaceList = intent.getStringArrayExtra("surfaceFilter") ?: emptyArray()

        binding.buttonAddTournament.setOnClickListener {
            startActivity(Intent(this, AddTournamentActivity::class.java))
        }
        binding.buttonFilter.setOnClickListener {
            val intent = Intent(this@ViewTournamentsActivity, TournamentFilterActivity::class.java)
            intent.putExtra("venueFilter",selectedVenueList)
            intent.putExtra("countryFilter",selectedCountryList)
            intent.putExtra("dateFilter",selectedDate)
            intent.putExtra("surfaceFilter",selectedSurfaceList)
            startActivity(intent)
        }

        tournamentRecyclerView = binding.tournamentsList
        tournamentRecyclerView.layoutManager = LinearLayoutManager(this)
        tournamentRecyclerView.setHasFixedSize(true)

        tournamentArrayList = arrayListOf()
        adapter = TournamentAdapter(tournamentArrayList)
        adapter.setOnItemClickListener(this)
        tournamentRecyclerView.adapter = adapter

        binding.searchTornament.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filtruj dane w adapterze na podstawie wprowadzonego tekstu
                adapter.filter.filter(s)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        getTournamentData()
        tournamentRecyclerView.isEnabled = true
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
                        val tournament = tournamentSnapshot.getValue(TournamentDataClass::class.java)
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
                applyFilters(selectedVenueList, selectedCountryList, selectedDate, selectedSurfaceList)
            }


            override fun onCancelled(error: DatabaseError) {
                // Obsłużenie błędu zapytania do bazy danych
            }
        })
    }

    private fun applyFilters(selectedVenueList: Array<String>, selectedCountryList: Array<String>, selectedDate: Long?, selectedSurfaceList: Array<String>){
        val filteredList = tournamentArrayList.filter { tournament ->
            val venueMatch = if (selectedVenueList.isNotEmpty()) {
                tournament.place in selectedVenueList
            } else {
                true
            }

            // Check country filter (if selectedCountryList is not empty)
            val countryMatch = if (selectedCountryList.isNotEmpty()) {
                tournament.country in selectedCountryList
            } else {
                true
            }

            // Check date range filter (if start or end date is provided)
            val dateMatch = if (selectedDate != 0L) {
                val startMatch = selectedDate?.let { tournament.startDate!! <= it } ?: true
                val endMatch = selectedDate?.let { tournament.endDate!! >= it } ?: true
                startMatch && endMatch
            } else {
                true
            }

            // Check surface filter (if selectedSurfaceList is not empty)
            val surfaceMatch = if (selectedSurfaceList.isNotEmpty()) {
                tournament.surface in selectedSurfaceList
            } else {
                true
            }

            // Only include tournaments that match all conditions
            venueMatch && countryMatch && dateMatch && surfaceMatch
        }
        adapter = TournamentAdapter(filteredList)
        tournamentRecyclerView.adapter = adapter
    }

    override fun onItemClick(itemView: TournamentDataClass) {

    }
}
