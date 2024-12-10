package com.anw.tenistats.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.anw.tenistats.R
import com.anw.tenistats.dialog.AddTeamDialog
import com.anw.tenistats.dialog.DeleteTeamDialog
import com.anw.tenistats.dialog.EditTeamNameDialog
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

class ViewTeamActivity : AppCompatActivity(), EditTeamNameDialog.TeamUpdateListener {

    private lateinit var expandableListView: ExpandableListView
    lateinit var teamListAdapter: TeamExpandableListAdapter
    private var teamsLoaded = 0
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var teamList: MutableList<TeamView>
    private lateinit var playerMap: MutableMap<String, List<PlayerView>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_team)
        firebaseAuth = FirebaseAuth.getInstance()

        // MENU
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.navigationViewMenu)
        val menu = findViewById<ImageButton>(R.id.buttonMenu)
        val headerView = navigationView.getHeaderView(0)
        menu.setOnClickListener {
            drawerLayout.open()
        }
        navigationDrawerHelper = NavigationDrawerHelper(this)
        navigationDrawerHelper.setupNavigationDrawer(drawerLayout, navigationView, firebaseAuth, true)
        val backButton = findViewById<ImageButton>(R.id.buttonUndo)
        backButton.visibility = View.GONE

        val userEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        val userEmailView = headerView.findViewById<TextView>(R.id.textViewUserEmail)
        if (userEmail.isNotEmpty()) {
            userEmailView.text = userEmail
        } else {
            userEmailView.text = resources.getString(R.string.user_email)
        }
        // MENU

        expandableListView = findViewById(R.id.expandableListView)

        val searchEditText = findViewById<EditText>(R.id.searchTeam)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filter teams using the adapter
                teamListAdapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        val addTeam=findViewById<ImageButton>(R.id.buttonAddVP)
        addTeam.setOnClickListener {
            val addTeamDialog = AddTeamDialog(this)
            addTeamDialog.show()
        }

        setupData()
    }

    private fun setupData() {
        teamList = mutableListOf()
        playerMap = mutableMapOf() // Initialize the playerMap here
        val user = firebaseAuth.currentUser?.uid
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString())
            .child("Teams")

        val playersDatabase = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString())
            .child("Players")

        // Fetching teams and players
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { teamSnapshot ->
                    // Pobierz nazwę drużyny
                    val teamName = teamSnapshot.child("name").getValue(String::class.java) ?: ""
                    val teamId = teamSnapshot.key ?: ""

                    // Tworzymy TeamView i dodajemy ją do listy
                    val teamView = TeamView(teamName, ArrayList())
                    teamList.add(teamView)

                    // Pobieramy dane graczy w drużynie
                    val playerNamesSnapshot = teamSnapshot.child("players")
                    val playerNames = playerNamesSnapshot.getValue(object : GenericTypeIndicator<ArrayList<String>>() {}) ?: ArrayList()

                    // Sprawdzamy, czy są jacyś gracze
                    if (playerNames.isNotEmpty()) {
                        val playerViewList = mutableListOf<PlayerView>() // Lista do przechowywania graczy tej drużyny

                        // Pobierz szczegóły każdego gracza
                        playerNames.forEach { playerName ->
                            playersDatabase.child(playerName).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(playerSnapshot: DataSnapshot) {
                                    val tournaments = playerSnapshot.child("tournaments")
                                        .getValue(object : GenericTypeIndicator<List<String>>() {}) ?: listOf()

                                    val (firstName, lastName) = if (playerName.contains(" ")) {
                                        val nameParts = playerName.split(" ")
                                        Pair(nameParts[0], nameParts.getOrElse(1) { "" })
                                    } else {
                                        Pair(playerName, "")
                                    }

                                    // Tworzymy PlayerView z odpowiednimi danymi
                                    val playerView = PlayerView(playerName, firstName, lastName, true, listOf(teamId), false, tournaments)

                                    playerViewList.add(playerView) // Dodajemy gracza do tymczasowej listy

                                    // Jeśli wszyscy gracze zostali załadowani, aktualizujemy mapę
                                    if (playerViewList.size == playerNames.size) {
                                        playerMap[teamId] = playerViewList

                                        teamsLoaded++
                                        if (teamsLoaded == snapshot.childrenCount.toInt()) {
                                            setupListAdapter() // Wywołujemy metodę, gdy wszystko jest załadowane
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Obsługa błędów
                                }
                            })
                        }
                    }

                    // Aktualizujemy drużynę z listą graczy
                    teamView.players = playerNames
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsługa błędów
            }
        })
    }



    private fun setupListAdapter() {
        if (teamList.isEmpty()) {
            Log.e("ViewTeamActivity", "Team list is empty, cannot set adapter.")
        } else {
            teamListAdapter = TeamExpandableListAdapter(this, teamList, playerMap, this)
            expandableListView.setAdapter(teamListAdapter)
        }
    }

    override fun onTeamUpdated(updatedTeam: TeamView) {
        Log.d("ViewTeamActivity", "onTeamUpdated called for team: ${updatedTeam.name}")

        val position = teamList.indexOfFirst { it.name == updatedTeam.name }
        if (position >= 0) {
            teamList[position] = updatedTeam
            teamListAdapter.updateTeam(position, updatedTeam)
        }
    }

    fun onEditTeam(team: TeamView) {
        val editTeamNameDialog = EditTeamNameDialog(this, team)
        editTeamNameDialog.setTeamUpdateListener(this)
        editTeamNameDialog.show()
    }

    fun onDeleteTeam(team: TeamView) {
        val deleteTeamNameDialog = DeleteTeamDialog(this, team)
        deleteTeamNameDialog.show()
    }

    fun onAddPlayer(team: TeamView) {
        // Implement this method
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        registerReceiver(
            refreshReceiver,
            IntentFilter("com.anw.tenistats.ACTION_REFRESH_TEAM"),
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(refreshReceiver)
    }


    private val refreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Wywołanie metody odświeżającej dane w widoku, np.:
            loadTeamData()
        }
    }

    private fun loadTeamData() {
        teamList.clear() // Wyczyszczenie listy przed ponownym załadowaniem
        playerMap.clear() // Wyczyszczenie mapy zawodników

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { teamSnapshot ->
                    val teamName = teamSnapshot.child("name").getValue(String::class.java) ?: ""
                    val teamId = teamSnapshot.key ?: ""

                    val teamView = TeamView(teamName, ArrayList())
                    teamList.add(teamView)

                    val playerNamesSnapshot = teamSnapshot.child("players")
                    val playerNames = playerNamesSnapshot.getValue(object : GenericTypeIndicator<ArrayList<String>>() {}) ?: ArrayList()

                    if (playerNames.isNotEmpty()) {
                        val playerViewList = mutableListOf<PlayerView>() // Tymczasowa lista dla graczy

                        playerNames.forEach { playerName ->
                            val playersDatabase = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                                .getReference(firebaseAuth.currentUser?.uid.toString())
                                .child("Players")
                                .child(playerName)

                            // Pobieranie szczegółów zawodnika
                            playersDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(playerSnapshot: DataSnapshot) {
                                    val tournaments = playerSnapshot.child("tournaments")
                                        .getValue(object : GenericTypeIndicator<List<String>>() {}) ?: listOf()

                                    val (firstName, lastName) = if (playerName.contains(" ")) {
                                        val nameParts = playerName.split(" ")
                                        Pair(nameParts[0], nameParts.getOrElse(1) { "" })
                                    } else {
                                        Pair(playerName, "")
                                    }

                                    // Tworzenie PlayerView z turniejami
                                    val playerView = PlayerView(playerName, firstName, lastName, true, listOf(teamId), false, tournaments)
                                    playerViewList.add(playerView)

                                    // Jeśli załadowano wszystkich graczy, przypisz listę do mapy
                                    if (playerViewList.size == playerNames.size) {
                                        playerMap[teamId] = playerViewList
                                        setupListAdapterIfReady()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("ViewTeamActivity", "Database error: ${error.message}")
                                }
                            })
                        }
                        teamView.players = playerNames // Aktualizujemy listę graczy w drużynie
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewTeamActivity", "Database error: ${error.message}")
            }
        })
    }

    private fun setupListAdapterIfReady() {
        // Sprawdza, czy wszystkie drużyny są załadowane, i wtedy ustawia adapter
        if (teamList.size == playerMap.size) {
            setupListAdapter()
        }
    }



}
