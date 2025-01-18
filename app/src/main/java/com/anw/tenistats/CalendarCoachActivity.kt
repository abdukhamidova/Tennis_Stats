package com.anw.tenistats

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColor
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.anw.tenistats.databinding.ActivityCalendarCoachBinding
import com.anw.tenistats.dialog.SelectPlayersForCalendarDialog
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.anw.tenistats.player.PlayerView
import com.anw.tenistats.stats.Player
import com.anw.tenistats.tournament.TournamentDataClass
import com.applandeo.materialcalendarview.CalendarDay
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarCoachActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCalendarCoachBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private var isCoachChecked: Boolean = true
    private var selectedPlayers: ArrayList<Player> = ArrayList()
    private val calendars: ArrayList<CalendarDay> = ArrayList()
    private lateinit var calendarView: CalendarView
    private var events: MutableMap<String,MutableMap<String,EventDataClass>> = mutableMapOf() //mapa eventów: klucz - dzień, wartość - mapa: klucz - id (zawodnika lub trenera), wartosc - event
    private var tournaments: MutableMap<String,MutableMap<String,TournamentDataClass>> = mutableMapOf() //mapa turniejow: klucz - dzień, wartość - mapa: klucz - id zawodnika, wartosc - turniejow (w sytuacji gdy 4 zawodnikow bierze udzial w tym samym tureniju - jednego do jednego dnia beda przypisane 4 mapy)
    private lateinit var userId: String
    private val calendar = Calendar.getInstance()

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
        //---- filtrowanie
        backButton.setImageResource(R.drawable.icon_filter30)
        backButton.setOnClickListener {
            val playerListDialog = SelectPlayersForCalendarDialog(this@CalendarCoachActivity,selectedPlayers,isCoachChecked)
            playerListDialog.show()
        }
        //----
        val userEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        if(userEmail.isNotEmpty()) {
            headerView.findViewById<TextView>(R.id.textViewUserEmail).text = userEmail
        }
        else {
            findViewById<TextView>(R.id.textViewUserEmail).text = resources.getString(R.string.user_email)
        }
        //endregion

        //INTENTY
        selectedPlayers = intent.getParcelableArrayListExtra<Player>("selectedPlayers") ?: ArrayList()
        isCoachChecked = intent.getBooleanExtra("isCoachChecked", true)

        //TO DO
        userId = firebaseAuth.currentUser?.uid.toString()
        calendarView = binding.calendar
        calendars.clear()

        //TODO: getEventsData() ~u
        getTournamentsData()

        binding.buttonAddEvent.setOnClickListener{
            //val intent = Intent(this, CalendarEventActivity::class.java)
            val intent = Intent(this, CalendarEventActivity::class.java).apply {
                putExtra("selectedPlayers", selectedPlayers)
                putExtra("isCoachChecked", isCoachChecked)
            }
            startActivity(intent)
        }

        calendarView.setOnCalendarDayClickListener(object: OnCalendarDayClickListener {
            override fun onClick(selectedDay: CalendarDay) {
                val day = String.format("%02d",selectedDay.calendar.get(Calendar.DAY_OF_MONTH))
                val month = String.format("%02d",selectedDay.calendar.get(Calendar.MONTH)+1)
                val year = selectedDay.calendar.get(Calendar.YEAR)
                //TODO
            }
        })
    }

    private fun getEventsData() {
        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Events")

        database.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (eventSnapshot in snapshot.children) {
                        val event = eventSnapshot.getValue(EventDataClass::class.java)
                        if(event != null){
                            event?.id = eventSnapshot.key.toString()
                            for(player in selectedPlayers){
                                val id = player.firstName
                                if(event.participants.contains(id)){
                                    setEvent(event,id,false)
                                    setPlayersIcon()
                                }
                            }
                            if(isCoachChecked){
                                if(event.participants.contains(userId)){
                                    setEvent(event,userId,true)
                                    setPlayersIcon()
                                }
                            }
                        }
                    }
                }
                //calendarView.setCalendarDays(calendars)
            }
            override fun onCancelled(error: DatabaseError) {
                // Obsłużenie błędu zapytania do bazy danych
            }
        })
    }

    private fun setEvent(event: EventDataClass, id: String?, isCoachId: Boolean) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val startDate = event.startDate?.let { Date(it) }
        val endDate = event.endDate?.let { Date(it) }

        if (startDate == null || endDate == null) return

        // Add the start date with a star icon
        val calendarStart = calendar.clone() as Calendar
        calendarStart.time = startDate
        val startDay = CalendarDay(calendarStart)
        if(isCoachId) //trener uczestniczy w evencie
        {
            startDay.labelColor = R.color.tournament_day
        }
        calendars.add(startDay)

        // Loop through all days from startDate to endDate
        val calendarToLoop = calendar.clone() as Calendar
        calendarToLoop.time = startDate
        calendarToLoop.add(Calendar.DAY_OF_MONTH, 1)
        while (calendarToLoop.time.before(endDate) || calendarToLoop.time == endDate) {
            val dayCalendar = calendarToLoop.clone() as Calendar

            val day = CalendarDay(dayCalendar)
            if(isCoachChecked) {
                day.labelColor = R.color.tournament_day
            }
            calendars.add(day)

            val dayKey = dateFormat.format(calendarToLoop.time)
            if (events.containsKey(dayKey)) {
                events[dayKey]?.put(id.toString(),event)
            } else {
                val innerMap: MutableMap<String, EventDataClass> = mutableMapOf()
                innerMap[id.toString()] = event
                events[dayKey] = innerMap
            }

            // Move to the next day
            calendarToLoop.add(Calendar.DAY_OF_MONTH, 1)
        }
        //calendarView.setCalendarDays(calendars)
    }

    private fun getTournamentsData() {
        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Tournaments")

        database.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (tournamentSnapshot in snapshot.children) {
                        val tournament = tournamentSnapshot.getValue(TournamentDataClass::class.java)
                        if(tournament != null){
                            tournament?.id = tournamentSnapshot.key.toString()
                            for(player in selectedPlayers){
                                val id = player.firstName
                                check(id, tournament?.id) { result ->
                                    if (result) {
                                        setTournament(tournament, id)
                                        setPlayersIcon()
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

    private fun check(id: String?, tournamentId: String?, callback: (Boolean) -> Unit) {
        val tournamentsRef = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference(userId).child("Players")
        tournamentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (playerSnapshot in snapshot.children) {
                        val playerView = playerSnapshot.getValue(PlayerView::class.java)
                        playerView?.let {
                            if (it.player == id && it.tournaments.contains(tournamentId)) {
                                callback(true)
                            }
                        }
                    }
                }
                callback(false)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false)
            }
        })
    }

    private fun setTournament(tournament: TournamentDataClass, id: String?) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val startDate = tournament.startDate?.let { Date(it) }
        val endDate = tournament.endDate?.let { Date(it) }

        if (startDate == null || endDate == null) return

        val calendarToLoop = calendar.clone() as Calendar
        calendarToLoop.time = startDate
        while (calendarToLoop.time.before(endDate) || calendarToLoop.time == endDate) {

            val dayKey = dateFormat.format(calendarToLoop.time)
            if (tournaments.containsKey(dayKey)) {
                tournaments[dayKey]?.put(id.toString(),tournament)
            } else {
                val innerMap: MutableMap<String, TournamentDataClass> = mutableMapOf()
                innerMap[id.toString()] = tournament
                tournaments[dayKey] = innerMap
            }

            // Move to the next day
            calendarToLoop.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun setPlayersIcon(){
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        for ((day, personsMap) in events) {
            // Exclude a specific ID and count the remaining persons
            val count = personsMap.filterKeys { it != userId }.size

            // Add the start date with a star icon
            calendar.time = dateFormat.parse(day)!!
            val eventDay = CalendarDay(calendar)
            // Add an icon to the calendar for the day based on the count
            when(count) {
                1 -> {
                    eventDay.imageResource = R.drawable.ic_dots1
                    calendars.add(eventDay)
                }
                2 -> {
                    eventDay.imageResource = R.drawable.ic_dots2
                    calendars.add(eventDay)
                }
                3 -> {
                    eventDay.imageResource = R.drawable.ic_dots3
                    calendars.add(eventDay)
                }
                4 -> {
                    eventDay.imageResource = R.drawable.ic_dots4
                    calendars.add(eventDay)
                }
            }
        }
        for ((day, personsMap) in tournaments) {
            // Exclude a specific ID and count the remaining persons
            val count = personsMap.filterKeys { it != userId }.size
            // Add the start date with a star icon
            val calendarToLoop = calendar.clone() as Calendar
            calendarToLoop.time = dateFormat.parse(day)!!
            val tournamentDay = CalendarDay(calendarToLoop)
            calendars.remove(tournamentDay)
            // Add an icon to the calendar for the day based on the count
            when(count) {
                1 -> {
                    tournamentDay.imageResource = R.drawable.ic_dots1
                    calendars.add(tournamentDay)
                }
                2 -> {
                    tournamentDay.imageResource = R.drawable.ic_dots2
                    calendars.add(tournamentDay)
                }
                3 -> {
                    tournamentDay.imageResource = R.drawable.ic_dots3
                    calendars.add(tournamentDay)
                }
                4 -> {
                    tournamentDay.imageResource = R.drawable.ic_dots4
                    calendars.add(tournamentDay)
                }
            }
            calendarView.setCalendarDays(calendars)
        }
    }
}