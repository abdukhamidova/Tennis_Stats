package com.anw.tenistats

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.anw.tenistats.ui.theme.NavigationDrawerHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class ActivityMenu : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)
        auth = FirebaseAuth.getInstance()
        val user = FirebaseAuth.getInstance().currentUser?.email.toString()

        //------------ MENU
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val menu = findViewById<ImageButton>(R.id.buttonMenu)
        val navigationView = findViewById<NavigationView>(R.id.navigationViewMenu)
        val headerView = navigationView.getHeaderView(0)

        menu.setOnClickListener{
            drawerLayout.open()
        }
        navigationDrawerHelper = NavigationDrawerHelper(this)
        navigationDrawerHelper.setupNavigationDrawer(drawerLayout, navigationView, auth)
        val backButton = findViewById<ImageButton>(R.id.buttonReturnUndo)
        backButton.visibility = View.GONE

        if(user.isNotEmpty()) {
            headerView.findViewById<TextView>(R.id.textViewUserEmail).text = user
        }
        else {
            findViewById<TextView>(R.id.textViewUserEmail).text = "user_email@smth.com"
        }
        //------------ MENU


        findViewById<Button>(R.id.buttonStartNewGame).setOnClickListener{
            startActivity(Intent(this,StartNewActivity::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViewById<Button>(R.id.buttonViewMatches).setOnClickListener{
            startActivity(Intent(this,ViewMatchesActivity::class.java))
        }
    }
}

