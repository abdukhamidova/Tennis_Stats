package com.anw.tenistats.ui.theme

import android.content.Intent
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.anw.tenistats.ActivityMenu
import com.anw.tenistats.ActivityStartPoint
import com.anw.tenistats.MainActivity
import com.anw.tenistats.PlayerDetailsActivity
import com.anw.tenistats.R
import com.anw.tenistats.StartNewActivity
import com.anw.tenistats.ViewMatchesActivity
import com.anw.tenistats.ViewPlayerActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class NavigationDrawerHelper(private val activity: AppCompatActivity) {
    fun setupNavigationDrawer(drawerLayout: DrawerLayout, navigationView: NavigationView, auth: FirebaseAuth, fromGame : Boolean = false) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            if(fromGame) {
                handleNavigationItemSelectedGame(menuItem, auth)
            }else {
                handleNavigationItemSelected(menuItem, auth)
            }
            true
        }
    }

    private fun handleNavigationItemSelected(menuItem: MenuItem, auth: FirebaseAuth) {
        val activity = activity as AppCompatActivity
        val intent = when (menuItem.itemId) {
            R.id.nav_home -> Intent(activity, ActivityMenu::class.java)
            R.id.nav_startNew -> Intent(activity, StartNewActivity::class.java)
            R.id.nav_viewMatch -> Intent(activity, ViewMatchesActivity::class.java)
            //ustawione do testów ~W
            R.id.nav_myPlayer -> Intent(activity, ViewPlayerActivity::class.java)
            R.id.nav_return -> Intent(activity,ActivityStartPoint::class.java)
            R.id.buttonLogOut -> {
                auth.signOut()
                Intent(activity, MainActivity::class.java)
            }
            else -> null
        }
        intent?.let {
            activity.startActivity(it)
        }
    }
     private fun handleNavigationItemSelectedGame(menuItem: MenuItem, auth: FirebaseAuth) {
        val activity = activity as AppCompatActivity
        val intent = when (menuItem.itemId) {
            R.id.nav_home -> Intent(activity, ActivityMenu::class.java)
            R.id.nav_startNew -> Intent(activity, StartNewActivity::class.java)
            R.id.nav_viewMatch -> Intent(activity, ViewMatchesActivity::class.java)
            R.id.nav_myPlayer -> Intent(activity, ViewPlayerActivity::class.java)
            R.id.buttonLogOut -> {
                auth.signOut()
                Intent(activity, MainActivity::class.java)
            }
            else -> null
        }
        intent?.let {
            activity.startActivity(it)
            activity.finish()
        }
    }

}
