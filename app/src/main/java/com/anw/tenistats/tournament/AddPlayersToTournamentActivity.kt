package com.anw.tenistats.tournament
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anw.tenistats.R
import com.anw.tenistats.databinding.ActivityAddPlayersToTournamentBinding
import com.anw.tenistats.dialog.AddPlayerToTournamentDialog
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.anw.tenistats.player.AddPlayerActivity
import com.anw.tenistats.player.PlayerDetailsActivity
import com.anw.tenistats.player.PlayerView
import com.anw.tenistats.tournament.RemovePlayerFromTournamentDialogActivity
import com.anw.tenistats.tournament.TournamentPlayerAdapter
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AddPlayersToTournamentActivity : AppCompatActivity(), TournamentPlayerAdapter.OnItemClickListener {

    private lateinit var playerRecyclerView: RecyclerView
    private lateinit var playerArrayList: ArrayList<PlayerView>
    private lateinit var filteredPlayerList: ArrayList<PlayerView>
    private lateinit var adapter: TournamentPlayerAdapter
    private lateinit var dbref: DatabaseReference
    private lateinit var searchPlayerEditText: EditText
    private lateinit var binding: ActivityAddPlayersToTournamentBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var tournamentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddPlayersToTournamentBinding.inflate(layoutInflater)
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
        playerRecyclerView = findViewById(R.id.playerList)
        searchPlayerEditText = findViewById(R.id.searchPlayer)
        val addPlayerButton = findViewById<ImageButton>(R.id.buttonAddPlayer)

        tournamentId = intent.getStringExtra("tournament_id").toString()
        val user = firebaseAuth.currentUser?.uid
        dbref = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference(user.toString()).child("Players")
        playerArrayList = ArrayList()
        filteredPlayerList = ArrayList()
        adapter = TournamentPlayerAdapter(filteredPlayerList, this)

        playerRecyclerView.layoutManager = LinearLayoutManager(this)
        playerRecyclerView.adapter = adapter

        // Pobieranie danych z Firebase
        fetchPlayers()

        // Obsługa wyszukiwania
        searchPlayerEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterPlayers(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Obsługa przycisku dodania zawodnika
        addPlayerButton.setOnClickListener {
            val dialog = AddPlayerToTournamentDialog(
                context = this,
                tournamentId = tournamentId
            )
            dialog.show()
        }

    }

    override fun onItemClick(playerView: PlayerView) {
        val intent = Intent(this, PlayerDetailsActivity::class.java)
        intent.putExtra("playerId", playerView.player)
        startActivity(intent)
    }

    override fun onDeletePlayerClicked(playerView: PlayerView) {
        val dialog = RemovePlayerFromTournamentDialogActivity(
            context = this,
            tournamentId = tournamentId,
            playerName = playerView.player
        )
        dialog.show()
    }
    private fun fetchPlayers() {
        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                playerArrayList.clear()
                for (playerSnapshot in snapshot.children) {
                    val player = playerSnapshot.getValue(PlayerView::class.java)
                    player?.let {
                        if (it.tournaments.contains(tournamentId)) {
                            playerArrayList.add(it)
                        }
                    }
                }
                filterPlayers(searchPlayerEditText.text.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsługa błędów
            }
        })
    }

    private fun filterPlayers(query: String) {
        filteredPlayerList.clear()
        filteredPlayerList.addAll(
            playerArrayList.filter {
                it.player.contains(query, ignoreCase = true)
            }
        )
        adapter.notifyDataSetChanged()
    }

}