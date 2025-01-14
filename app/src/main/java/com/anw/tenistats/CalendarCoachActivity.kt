package com.anw.tenistats

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
import com.anw.tenistats.databinding.ActivityCalendarCoachBinding
import com.anw.tenistats.dialog.SelectPlayersForCalendarDialog
import com.anw.tenistats.dialog.TournamentListDialog
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.anw.tenistats.stats.Player
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class CalendarCoachActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCalendarCoachBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private var isCoachChecked: Boolean = true
    private var selectedPlayers: ArrayList<Player> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCalendarCoachBinding.inflate(layoutInflater)
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
        backButton.setImageResource(R.drawable.icon_filter30)
        backButton.setOnClickListener {
            val playerListDialog = SelectPlayersForCalendarDialog(this@CalendarCoachActivity)
            playerListDialog.show()
        }
        val userEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        if(userEmail.isNotEmpty()) {
            headerView.findViewById<TextView>(R.id.textViewUserEmail).text = userEmail
        }
        else {
            findViewById<TextView>(R.id.textViewUserEmail).text = resources.getString(R.string.user_email)
        }
        //endregion

        selectedPlayers = intent.getParcelableArrayListExtra<Player>("selectedPlayers") ?: ArrayList()
        isCoachChecked = intent.getBooleanExtra("isCoachChecked", true)

        //TO DO
    }
}