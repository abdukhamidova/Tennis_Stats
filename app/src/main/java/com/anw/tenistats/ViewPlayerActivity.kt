package com.anw.tenistats

import MyAdapterPlayer
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
import com.anw.tenistats.ui.theme.NavigationDrawerHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewPlayerActivity : AppCompatActivity(), MyAdapterPlayer.OnItemClickListener {

    private lateinit var dbref : DatabaseReference
    private lateinit var playerRecyclerView: RecyclerView
    private lateinit var playerArrayList: ArrayList<PlayerView>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var adapter: MyAdapterPlayer
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_player)
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

        menu.setOnClickListener{
            drawerLayout.open()
        }
        navigationDrawerHelper = NavigationDrawerHelper(this)
        navigationDrawerHelper.setupNavigationDrawer(drawerLayout, navigationView, firebaseAuth)
        val backButton = findViewById<ImageButton>(R.id.buttonUndo)
        backButton.visibility = View.GONE
        val add = findViewById<ImageButton>(R.id.buttonAddVP)
        add.setOnClickListener {
            startActivity(Intent(this,AddPlayerActivity::class.java))
        }

        val userEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        if(userEmail.isNotEmpty()) {
            headerView.findViewById<TextView>(R.id.textViewUserEmail).text = userEmail
        }
        else {
            findViewById<TextView>(R.id.textViewUserEmail).text = resources.getString(R.string.user_email)
        }
        //------------ MENU

        playerRecyclerView = findViewById(R.id.playerList)
        playerRecyclerView.layoutManager = LinearLayoutManager(this)
        playerRecyclerView.setHasFixedSize(true)

        // Inicjalizacja pola playerArrayList pustą listą
        playerArrayList = arrayListOf<PlayerView>()


        // Inicjalizacja widoku RecyclerView
        adapter = MyAdapterPlayer(playerArrayList, firebaseAuth)
        adapter.setOnItemClickListener(this)
        playerRecyclerView.adapter = adapter
        getPlayerData()

        playerRecyclerView.isEnabled = true

        val searchEditText = findViewById<EditText>(R.id.searchPlayer)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filtruj dane w adapterze na podstawie wprowadzonego tekstu
                adapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun getPlayerData() {
        val user = firebaseAuth.currentUser?.uid
        dbref = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString())
            .child("Players")

        dbref.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (playerSnapshot in snapshot.children) {
                        val player = playerSnapshot.getValue(PlayerView::class.java)
                        if (player != null) {
                            findViewById<TextView>(R.id.textViewNotFound).visibility = View.INVISIBLE
                            playerArrayList.add(player)
                        }else{
                            findViewById<TextView>(R.id.textViewNotFound).visibility = View.VISIBLE
                        }
                    }
                    // Inicjalizacja adaptera
                    adapter = MyAdapterPlayer(playerArrayList, firebaseAuth)
                    // Podpięcie adaptera do RecyclerView
                   playerRecyclerView.adapter = adapter
                    // Poinformuj adapter o zmianach w danych
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsłużenie błędu zapytania do bazy danych
            }
        })
    }

    override fun onItemClick(playerView: PlayerView) {
        val intent = Intent(this, PlayerDetailsActivity::class.java)
        startActivity(intent)
    }
}