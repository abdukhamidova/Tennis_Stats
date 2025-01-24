package com.anw.tenistats.dialog

// CalendarDayDialog.kt
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.anw.tenistats.CalendarEventActivity
import com.anw.tenistats.EventDataClass
import com.anw.tenistats.R
import com.anw.tenistats.tournament.TournamentDataClass
import com.anw.tenistats.tournament.UpdateEditMatchActivity
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

class CalendarDayDialog(
    private val context: Context,
    private val selectedPlayers: ArrayList<String>,
    private val isCoachChecked: Boolean,
    private val clickedDate: Long,
    private val occupied:Boolean
) {
    private lateinit var alertDialog: AlertDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userId: String

    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_calendar_day, null)
        alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val buttonCancel: Button = dialogView.findViewById(R.id.buttonCancel)
        val participantsContainer: LinearLayout = dialogView.findViewById(R.id.participantsContainer)
        val buttonAddEvent: Button = dialogView.findViewById(R.id.buttonAddEventCalendarDay)
        val textDate: TextView = dialogView.findViewById(R.id.textViewDate)

        textDate.text=formatDate(clickedDate,clickedDate)

        if (!occupied) {
            // Wyświetl wiadomość, że nie ma wydarzeń
            participantsContainer.removeAllViews()
            val noEventsView = LayoutInflater.from(context).inflate(R.layout.item_no_event_entry, participantsContainer, false)
            val textViewNoEvents: TextView = noEventsView.findViewById(R.id.textViewNoEventsMessage)
            textViewNoEvents.text = "No events or tournaments on this day."
            participantsContainer.addView(noEventsView)
        } else {
            // Normalne przetwarzanie wydarzeń
            firebaseAuth = FirebaseAuth.getInstance()
            userId = firebaseAuth.currentUser?.uid.toString()
            database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(userId)

            if (isCoachChecked) {
                fetchAndDisplayDataForPlayer("ME", participantsContainer, true)
            }
            for (player in selectedPlayers) {
                fetchAndDisplayDataForPlayer(player, participantsContainer, false)
            }
        }

        buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        buttonAddEvent.setOnClickListener {
            val intent = Intent(context, CalendarEventActivity::class.java)
            intent.putExtra("startDate", clickedDate)
            intent.putExtra("selectedPlayers", selectedPlayers)
            intent.putExtra("isCoachChecked", isCoachChecked)

            Log.d("EventClick", "Opening event with date: $clickedDate")
            context.startActivity(intent)
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun fetchAndDisplayDataForPlayer(player: String, container: LinearLayout, isCoach: Boolean) {
        val playerView = LayoutInflater.from(context).inflate(R.layout.item_player_day, container, false)
        val textViewPlayerName: TextView = playerView.findViewById(R.id.textViewPlayerName)
        val detailsContainer: LinearLayout = playerView.findViewById(R.id.detailsContainer)
        val eventsContainer: LinearLayout = playerView.findViewById(R.id.eventsContainer)
        val tournamentsContainer: LinearLayout = playerView.findViewById(R.id.tournamentsContainer)

        textViewPlayerName.text = if (isCoach) "ME" else player

        // Kliknięcie rozwijające/zwijające
        textViewPlayerName.setOnClickListener {
            detailsContainer.visibility = if (detailsContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        var hasEventsOrTournaments = false

        if (isCoach) {
            fetchEventsForCoach { eventList ->
                if (eventList.isNotEmpty()) {
                    hasEventsOrTournaments = true
                    displayEvents(eventsContainer, eventList)
                }
                if (hasEventsOrTournaments) container.addView(playerView)
            }
        } else {
            fetchEventsForPlayer(player) { eventList ->
                if (eventList.isNotEmpty()) {
                    hasEventsOrTournaments = true
                    displayEvents(eventsContainer, eventList)
                }

                fetchTournamentsForPlayer(player) { tournamentList ->
                    if (tournamentList.isNotEmpty()) {
                        hasEventsOrTournaments = true
                        displayTournaments(tournamentsContainer, tournamentList)
                    }
                    if (hasEventsOrTournaments) container.addView(playerView)
                }
            }
        }
    }


    private fun fetchEventsForCoach(callback: (List<EventDataClass>) -> Unit) {
        val eventsRef = database.child("Events")

        eventsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val eventList = mutableListOf<EventDataClass>()
                for (eventSnapshot in snapshot.children) {
                    val isCoachChecked = eventSnapshot.child("isCoachChecked").getValue(Boolean::class.java) ?: false
                    Log.d("FetchEventsForCoach", "Fetched raw isCoachChecked: $isCoachChecked")

                    if (isCoachChecked) {
                        // Ręcznie przypisz klucz do `id`
                        val event = eventSnapshot.getValue(EventDataClass::class.java)
                        event?.id = eventSnapshot.key // Przypisz klucz
                        if (event != null && isDateWithinRange(event.startDate, event.endDate, clickedDate)) {
                            Log.d("FetchEventsForCoach", "Added event for coach: ${event.name}")
                            eventList.add(event)
                        }
                    }
                }
                callback(eventList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchEventsForCoach", "Error fetching events: ${error.message}")
            }
        })
    }

    private fun fetchEventsForPlayer(player: String, callback: (List<EventDataClass>) -> Unit) {
        val eventsRef = database.child("Events")

        eventsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val eventList = mutableListOf<EventDataClass>()
                for (eventSnapshot in snapshot.children) {
                    val event = eventSnapshot.getValue(EventDataClass::class.java)
                    event?.id = eventSnapshot.key // Przypisz klucz do `id`

                    Log.d("FetchEventsForPlayer", "Fetched event: ${event?.name}, players: ${event?.players}, id: ${event?.id}")

                    // Sprawdź, czy gracz znajduje się na liście `players`
                    if (event != null && event.players.contains(player) && isDateWithinRange(event.startDate, event.endDate, clickedDate)) {
                        Log.d("FetchEventsForPlayer", "Added event for player: ${event.name}")
                        eventList.add(event)
                    }
                }
                callback(eventList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchEventsForPlayer", "Error fetching events: ${error.message}")
            }
        })
    }


    private fun fetchTournamentsForPlayer(player: String, callback: (List<TournamentDataClass>) -> Unit) {
        val playerRef = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(userId)
            .child("Players")
            .child(player)
            .child("tournaments")

        playerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tournamentIds = snapshot.children.map { it.value.toString() }
                fetchTournamentDetails(tournamentIds, callback)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CalendarDayDialog", "Error fetching player tournaments: ${error.message}")
            }
        })
    }

    private fun fetchTournamentDetails(tournamentIds: List<String>, callback: (List<TournamentDataClass>) -> Unit) {
        val tournamentsRef = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Tournaments")
        tournamentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tournamentList = mutableListOf<TournamentDataClass>()
                for (tournamentId in tournamentIds) {
                    val tournamentSnapshot = snapshot.child(tournamentId)
                    val tournament = tournamentSnapshot.getValue(TournamentDataClass::class.java)
                    if (tournament != null && isDateWithinRange(tournament.startDate, tournament.endDate, clickedDate)) {
                        tournamentList.add(tournament)
                    }
                }
                callback(tournamentList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CalendarDayDialog", "Error fetching tournaments: ${error.message}")
            }
        })
    }

    private fun displayEvents(container: LinearLayout, eventList: List<EventDataClass>) {
        container.removeAllViews() // Wyczyść poprzednie dane

        if (eventList.isEmpty()) {

            return
        }

        for (event in eventList) {
            val eventView = LayoutInflater.from(context).inflate(R.layout.item_event_entry, container, false)
            val textViewEventTitle: TextView = eventView.findViewById(R.id.textViewEventTitle)
            val textViewEventDates: TextView = eventView.findViewById(R.id.textViewEventDates)

            textViewEventTitle.text = event.name
            textViewEventDates.text = formatDate(event.startDate, event.endDate)

            eventView.setOnClickListener {
                val intent = Intent(context, CalendarEventActivity::class.java)
                intent.putExtra("eventId", event.id)
                intent.putExtra("startDate", clickedDate)
                intent.putExtra("selectedPlayers", selectedPlayers)
                intent.putExtra("isCoachChecked", isCoachChecked)

                Log.d("EventClick", "Opening event with ID: ${event.id}")

                context.startActivity(intent)
            }


            container.addView(eventView)
        }
    }


    private fun displayTournaments(container: LinearLayout, tournamentList: List<TournamentDataClass>) {
        container.removeAllViews()
        for (tournament in tournamentList) {
            val tournamentView = LayoutInflater.from(context).inflate(R.layout.item_event_entry, container, false)
            val textViewEventTitle: TextView = tournamentView.findViewById(R.id.textViewEventTitle)
            val textViewEventDates: TextView = tournamentView.findViewById(R.id.textViewEventDates)

            textViewEventTitle.text =   "Tournament: "+tournament.name
            textViewEventDates.text = formatDate(tournament.startDate, tournament.endDate)

            tournamentView.setOnClickListener {
                // Show tournament dialog
                val dialog = TournamentDialog(context)
                dialog.show(tournament.id)
            }

            container.addView(tournamentView)
        }
    }

    private fun isDateWithinRange(startDate: Long?, endDate: Long?, date: Long): Boolean {
        if (startDate == null || endDate == null) return false

        // Zaokrąglij clickedDate do początku dnia
        val roundedDate = roundToStartOfDay(date)

        return roundedDate in roundToStartOfDay(startDate)..roundToStartOfDay(endDate)
    }

    private fun roundToStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }



    private fun formatDate(startDate: Long?, endDate: Long?): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return if (startDate == endDate) {
            sdf.format(Date(startDate!!))
        } else {
            "${sdf.format(Date(startDate!!))} - ${sdf.format(Date(endDate!!))}"
        }
    }
}
