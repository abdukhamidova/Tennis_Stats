package com.anw.tenistats

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.anw.tenistats.databinding.ActivityCalendarEventBinding
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarEventActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCalendarEventBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCalendarEventBinding.inflate(layoutInflater)
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
        findViewById<ImageButton>(R.id.buttonUndo).visibility = View.GONE
        /*//---- filtrowanie
        backButton.setImageResource(R.drawable.icon_filter30)
        backButton.setOnClickListener {
            val playerListDialog = SelectPlayersForCalendarDialog(this)
            playerListDialog.show()
        }
        //----*/
        val userEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        if(userEmail.isNotEmpty()) {
            headerView.findViewById<TextView>(R.id.textViewUserEmail).text = userEmail
        }
        else {
            findViewById<TextView>(R.id.textViewUserEmail).text = resources.getString(R.string.user_email)
        }
        //endregion

        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference(userId).child("Events")

        //region---Powiedzmy ze tutaj przyjmie jakies intenty, np. liste uczestinkow
        // Odbieranie danych z Intent
        val selectedPlayers = intent.getStringArrayListExtra("selectedPlayers") ?: arrayListOf()
        val isCoachChecked = intent.getBooleanExtra("isCoachChecked", false)
        val setStartDate = intent.getLongExtra("startDate", 0)

        //przystosowac nazwe intentu !!!!!
        val eventId = intent.getStringExtra("eventId")
        //endregion---

        binding.textViewParticipantsList.text = getParticipantsList(selectedPlayers, isCoachChecked)


        //niewiadomo czy setEventData wgl dziala
        if (!eventId.isNullOrEmpty()) {
            setEventData(eventId, database, setStartDate)
        } else {
            println("No eventId was passed through the intent.")
        }
        //region Data
        binding.editTextStartDate.setOnClickListener {
            val calendar = Calendar.getInstance()

            // Jeśli data była ustawiona wcześniej, wykorzystaj ją jako startową
            if (setStartDate != 0L) {
                calendar.timeInMillis = setStartDate
            }

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
                    binding.editTextStartDate.setText(selectedDate)
                    // Aktualizacja wartości milisekund przy edycji daty
                    val updatedCalendar = Calendar.getInstance()
                    updatedCalendar.set(year, monthOfYear, dayOfMonth)
                    //calendarDate = updatedCalendar.timeInMillis
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        binding.editTextEndDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
                    binding.editTextEndDate.setText(selectedDate)
                },
                year, month, day
            )
            datePickerDialog.show()
        }
        //endregion

        binding.buttonSubmit.setOnClickListener{
            if (binding.editTextName.text.isEmpty() ||
                binding.editTextStartDate.text.isEmpty() ||
                binding.editTextEndDate.text.isEmpty()) {
                Toast.makeText(this, "Don't leave empty fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Zatrzymujemy dalsze przetwarzanie
            }
            val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            if((dateFormat.parse(binding.editTextStartDate.text.toString())?.time!!) > (dateFormat.parse(binding.editTextEndDate.text.toString())?.time!!)){
                Toast.makeText(this, "Wrong dates", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Zatrzymujemy dalsze przetwarzanie
            }

            val name = binding.editTextName.text.toString()
            val note = binding.editTextNote.text.toString()
            val millisecondsStart = changeDateToLongFormat(binding.editTextStartDate.text.toString())
            val millisecondsEnd = changeDateToLongFormat(binding.editTextEndDate.text.toString())
            // Wywołanie funkcji do dodania eventu do bazy
            addEventToDataBase(selectedPlayers, name, millisecondsStart, millisecondsEnd, note, isCoachChecked)
            finish()
        }

    }
    // Funkcja do dodawania eventu do bazy danych
    fun addEventToDataBase(
        selectedPlayers: ArrayList<String>,
        name: String,
        milisecondsStart: Long?,
        milisecondsEnd: Long?,
        note: String,
        isCoachChecked: Boolean
    ) {
        val eventId = database.push().key // Generowanie unikalnego ID dla wydarzenia

        // Przekształcenie danych w mapę
        val eventData = hashMapOf<String, Any>(
            "name" to name,
            "startDate" to (milisecondsStart ?: 0L),  // Jeśli milisecondsStart jest null, użyj 0L
            "endDate" to (milisecondsEnd ?: 0L),      // Jeśli milisecondsEnd jest null, użyj 0L
            "note" to note,
            "players" to selectedPlayers,
            "isCoachChecked" to isCoachChecked
        )

        // Zapisanie danych w Firebase pod wygenerowanym ID
        if (eventId != null) {
            database.child(eventId).setValue(eventData)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Event added successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error adding event", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    fun changeDateToLongFormat(date: String): Long? {
        var resultDate: Long? = 0
        if (date.isNotEmpty()) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            resultDate = dateFormat.parse(date)?.time ?: 0 // Sparsowanie daty
        }
        return resultDate
    }

    fun changeLongToDateFormat(timestamp: Long?): String {
        return if (timestamp != null && timestamp > 0) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        } else {
            ""
        }
    }

    fun setEventData(eventId: String, database: DatabaseReference, setStartDate: Long) {
        database.child(eventId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Get values from the snapshot and assign them to binding fields
                    val name = snapshot.child("name").getValue(String::class.java) ?: ""
                    val startDateMillis = snapshot.child("startDate").getValue(Long::class.java) ?: 0L
                    val endDateMillis = snapshot.child("endDate").getValue(Long::class.java) ?: 0L
                    val note = snapshot.child("note").getValue(String::class.java) ?: ""

                    // Assign values to binding
                    binding.editTextName.setText(name)
                    if(setStartDate != 0L)
                        binding.editTextStartDate.setText(changeLongToDateFormat(setStartDate))
                    else binding.editTextStartDate.setText(changeLongToDateFormat(startDateMillis))
                    binding.editTextEndDate.setText(changeLongToDateFormat(endDateMillis))
                    binding.editTextNote.setText(note)
                } else {
                    println("Event not found.")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("Database error: ${error.message}")
            }
        })
    }

    fun getParticipantsList(selectedPlayers: ArrayList<String>, isCoachChecked: Boolean): String{
        if(isCoachChecked) return "ME, " + selectedPlayers.joinToString(", ")
        else return selectedPlayers.joinToString(", ")
    }
}