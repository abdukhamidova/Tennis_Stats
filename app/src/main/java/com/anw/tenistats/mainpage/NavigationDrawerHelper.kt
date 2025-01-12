package com.anw.tenistats.mainpage

import android.content.Intent
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.anw.tenistats.CalendarCoachActivity
import com.anw.tenistats.CalendarTournamentActivity
import com.anw.tenistats.matchplay.StartPointActivity
import com.anw.tenistats.stats.Head2HeadActivity
import com.anw.tenistats.R
import com.anw.tenistats.matchplay.StartNewActivity
import com.anw.tenistats.stats.ViewMatchesActivity
import com.anw.tenistats.player.ViewPlayerActivity
import com.anw.tenistats.player.ViewTeamActivity
import com.anw.tenistats.tournament.ViewTournamentsActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class NavigationDrawerHelper(private val activity: AppCompatActivity) {
    fun setupNavigationDrawer(drawerLayout: DrawerLayout, navigationView: NavigationView, auth: FirebaseAuth, fromGame : Boolean = false) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            if(fromGame) {
                handleNavigationItemSelectedGame(menuItem, auth, drawerLayout)
            }else {
                handleNavigationItemSelected(menuItem, auth, drawerLayout)
            }
            true
        }
    }

    private fun handleNavigationItemSelected(menuItem: MenuItem, auth: FirebaseAuth, drawerLayout: DrawerLayout) {
        val activity = activity as AppCompatActivity
        val intent = when (menuItem.itemId) {
            R.id.nav_home -> Intent(activity, MenuActivity::class.java)
            R.id.nav_startNew -> Intent(activity, StartNewActivity::class.java)
            R.id.nav_viewMatch -> Intent(activity, ViewMatchesActivity::class.java)
            R.id.nav_myPlayer -> Intent(activity, ViewPlayerActivity::class.java)
            R.id.nav_h2h -> Intent(activity, Head2HeadActivity::class.java)
            R.id.nav_viewTeam -> Intent(activity,ViewTeamActivity::class.java)
            R.id.nav_return -> Intent(activity, StartPointActivity::class.java)
            R.id.nav_viewTournaments -> Intent(activity, ViewTournamentsActivity::class.java)
            R.id.nav_viewCalendar -> Intent(activity, CalendarCoachActivity::class.java)
            R.id.buttonLogOut -> {
                auth.signOut()
                activity.finish()
                Intent(activity, MainActivity::class.java)
            }
            else -> null
        }
        intent?.let {
            drawerLayout.closeDrawers()
            activity.startActivity(it)
        }
    }
     private fun handleNavigationItemSelectedGame(menuItem: MenuItem, auth: FirebaseAuth,drawerLayout: DrawerLayout) {
        val activity = activity as AppCompatActivity
        val intent = when (menuItem.itemId) {
            R.id.nav_home -> Intent(activity, MenuActivity::class.java)
            R.id.nav_startNew -> Intent(activity, StartNewActivity::class.java)
            R.id.nav_viewMatch -> Intent(activity, ViewMatchesActivity::class.java)
            R.id.nav_myPlayer -> Intent(activity, ViewPlayerActivity::class.java)
            R.id.nav_h2h -> Intent(activity, Head2HeadActivity::class.java)
            R.id.nav_viewTeam -> Intent(activity,ViewTeamActivity::class.java)
            R.id.nav_viewTournaments -> Intent(activity, ViewTournamentsActivity::class.java)
            R.id.nav_viewCalendar -> Intent(activity, CalendarCoachActivity::class.java)
            R.id.buttonLogOut -> {
                auth.signOut()
                drawerLayout.closeDrawers()
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
