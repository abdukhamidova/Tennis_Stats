package com.anw.tenistats

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.anw.tenistats.databinding.ActivityCalendarBinding
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.anw.tenistats.tournament.TournamentDataClass
import com.applandeo.materialcalendarview.CalendarDay
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener
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

class CalendarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCalendarBinding
    private lateinit var calendarView: CalendarView
    private var events: MutableMap<String,ArrayList<TournamentDataClass>> = mutableMapOf()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private val calendars: ArrayList<CalendarDay> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCalendarBinding.inflate(layoutInflater)
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
        backButton.visibility = View.GONE

        val userEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        if(userEmail.isNotEmpty()) {
            headerView.findViewById<TextView>(R.id.textViewUserEmail).text = userEmail
        }
        else {
            findViewById<TextView>(R.id.textViewUserEmail).text = resources.getString(R.string.user_email)
        }
        //endregion

        calendarView = binding.calendar
        calendars.clear()

        getTournamentData()

        //UWAGA! trzeba uwazac, bo miesiace sobie liczy od 0 baran)

        calendarView.setOnCalendarDayClickListener(object: OnCalendarDayClickListener{
            override fun onClick(selectedDay: CalendarDay) {
                val day = String.format("%02d",selectedDay.calendar.get(Calendar.DAY_OF_MONTH))
                val month = String.format("%02d",selectedDay.calendar.get(Calendar.MONTH))
                val year = selectedDay.calendar.get(Calendar.YEAR)
                //TODO: dialog z turniejami/eventami odbywajacymi sie tego dnia według {events.containsKey("$day-$month-$year"} lub "Nothing to show" gdy puste + przyciski "Add event" i "Cancel"
            }
        })
    }
    private fun getTournamentData() {
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
                            setEvent(tournament)
                        }
                    }
                }
                calendarView.setCalendarDays(calendars)
            }
            override fun onCancelled(error: DatabaseError) {
                // Obsłużenie błędu zapytania do bazy danych
            }
        })
    }

    private fun setEvent(tournament: TournamentDataClass) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val startDate = tournament.startDate?.let { Date(it) }
        val endDate = tournament.endDate?.let { Date(it) }

        if (startDate == null || endDate == null) return

        val calendar = Calendar.getInstance()

        // Add the start date with a star icon
        calendar.time = startDate
        val startDay = CalendarDay(calendar)
        startDay.labelColor = R.color.tournament_day
        startDay.imageResource = R.drawable.icon_star30gold
        calendars.add(startDay)

        val startKey = dateFormat.format(startDate)
        if (events.containsKey(startKey)) {
            events[startKey]?.add(tournament)
        } else {
            events[startKey] = arrayListOf(tournament)
        }

        // Loop through all days from startDate to endDate
        val calendarToLoop = calendar.clone() as Calendar
        calendarToLoop.time = startDate
        calendarToLoop.add(Calendar.DAY_OF_MONTH, 1)
        while (calendarToLoop.time.before(endDate) || calendarToLoop.time == endDate) {
            val dayCalendar = calendarToLoop.clone() as Calendar

            val day = CalendarDay(dayCalendar)
            day.labelColor = R.color.tournament_day
            calendars.add(day)

            val dayKey = dateFormat.format(calendarToLoop.time)
            if (events.containsKey(dayKey)) {
                events[dayKey]?.add(tournament)
            } else {
                events[dayKey] = arrayListOf(tournament)
            }

            // Move to the next day
            calendarToLoop.add(Calendar.DAY_OF_MONTH, 1)
        }
    }
}