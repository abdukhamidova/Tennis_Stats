package com.anw.tenistats.tournament

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
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
import com.anw.tenistats.adapter.PlayerAdapter
import com.anw.tenistats.databinding.ActivityAddPlayersToTournamentBinding
import com.anw.tenistats.dialog.AddPlayerToTournamentDialog
import com.anw.tenistats.dialog.AddTeamDialog
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.anw.tenistats.player.AddPlayerActivity
import com.anw.tenistats.player.PlayerDetailsActivity
import com.anw.tenistats.player.PlayerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class AddPlayersToTournamentActivity : AppCompatActivity(), PlayerAdapter.OnItemClickListener {
    private lateinit var binding: ActivityAddPlayersToTournamentBinding
    private lateinit var dbref: DatabaseReference
    private lateinit var playerRecyclerView: RecyclerView
    private lateinit var playerArrayList: ArrayList<PlayerView>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var adapter: PlayerAdapter
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private var isAdapterSet = false
    private lateinit var noPlayerFoundTextView: TextView
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

        /*binding.buttonAddPlayer.setOnClickListener {
            AddPlayerToTournamentDialog(this,tournamentId).show()
        }

        playerRecyclerView = binding.playerList
        playerRecyclerView.layoutManager = LinearLayoutManager(this)
        playerRecyclerView.setHasFixedSize(true)

        noPlayerFoundTextView = binding.textViewNotFound
        playerArrayList = arrayListOf<PlayerView>()

        getPlayerData()

        adapter = PlayerAdapter(playerArrayList, firebaseAuth)
        adapter.setOnItemClickListener(this)
        playerRecyclerView.adapter = adapter

        playerRecyclerView.isEnabled = true

        val searchEditText = findViewById<EditText>(R.id.searchPlayer)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filtruj dane w adapterze na podstawie wprowadzonego tekstu
                adapter.filter.filter(s)
            }
            override fun afterTextChanged(s: Editable?) {}
        })*/
    }

    private fun getPlayerData() {
        val user = firebaseAuth.currentUser?.uid
        dbref =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(user.toString())
                .child("Players")

        dbref.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                playerArrayList.clear() // Wyczyść listę, aby uniknąć duplikatów

                if (snapshot.exists()) {
                    var playersProcessed = 0 // Licznik przetworzonych graczy

                    for (playerSnapshot in snapshot.children) {
                        val player = playerSnapshot.getValue(PlayerView::class.java)
                        if (player != null) {
                            if (playerSnapshot.child("Tournaments").exists() && playerSnapshot.child("Tournaments").hasChild(tournamentId)) {
                                playerArrayList.add(player)
                                break // Exit the loop once found
                            }

                            // Pobieramy wartość "isFavorite" dla każdego gracza
                            dbref.child(player.player).child("isFavorite")
                                .get()
                                .addOnSuccessListener { isFavoriteSnapshot ->
                                    player.isFavorite =
                                        isFavoriteSnapshot.getValue(Boolean::class.java) ?: false
                                }
                                .addOnFailureListener {
                                    player.isFavorite =
                                        false // Domyślna wartość, jeśli pobieranie nie powiedzie się
                                }
                                .addOnCompleteListener {
                                    playersProcessed++
                                    if (playersProcessed == snapshot.childrenCount.toInt()) {
                                        // Posortuj listę: najpierw według isFavorite, potem alfabetycznie po firstName
                                        playerArrayList.sortWith(compareByDescending<PlayerView> { it.isFavorite }
                                            .thenBy { it.firstName.toLowerCase(Locale.getDefault()) })

                                        // Aktualizuj adapter dopiero po zakończeniu przetwarzania wszystkich danych
                                        if (!isAdapterSet) {
                                            adapter = PlayerAdapter(playerArrayList, firebaseAuth)
                                            playerRecyclerView.adapter = adapter
                                            adapter.setOnItemClickListener(this@AddPlayersToTournamentActivity)
                                            isAdapterSet = true
                                        } else {
                                            adapter.notifyDataSetChanged()
                                        }
                                    }
                                }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsłużenie błędu zapytania do bazy danych
            }
        })
    }

    override fun onItemClick(playerView: PlayerView) {
        val intent = Intent(this, PlayerDetailsActivity::class.java)
        intent.putExtra("playerId", playerView.player) // Przekazanie ID zawodnika
        // startActivity(intent) // Uruchomienie nowej aktywności
    }
}