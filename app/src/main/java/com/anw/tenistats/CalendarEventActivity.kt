package com.anw.tenistats

import android.app.DatePickerDialog
import android.os.Bundle
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
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
        val backButton = findViewById<ImageButton>(R.id.buttonUndo)
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

        //region---Powiedzmy ze tutaj przyjmie jakies intenty, np. liste uczestinkow
        //Zakladajac, ze przyjmowanie intentu bedzie wygladac wlasnie tak:
        // Odbieranie danych z Intent
        val selectedPlayers = intent.getStringArrayListExtra("selectedPlayers") ?: arrayListOf()
        val isCoachChecked = intent.getBooleanExtra("isCoachChecked", false)
        //endregion---
        binding.textViewParticipantsList.text = selectedPlayers.joinToString(", ")

        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference(userId).child("Events")

        //region Data
        binding.editTextStartDate.setOnClickListener {
            val calendar = Calendar.getInstance()

            /*// Jeśli data była ustawiona wcześniej, wykorzystaj ją jako startową
            if (calendarDate != 0L) {
                calendar.timeInMillis = calendarDate
            }*/

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
            val startDate = binding.editTextStartDate.text.toString()
            val endDate = binding.editTextEndDate.text.toString()
            val note = binding.editTextNote.text.toString()

            // Wywołanie funkcji do dodania eventu do bazy
            addEventToDataBase(selectedPlayers, name, startDate, endDate, note)

        }

    }
    // Funkcja do dodawania eventu do bazy danych
    fun addEventToDataBase(selectedPlayers: ArrayList<String>, name: String, startDate: String, endDate: String, note: String) {

        val eventId = database.push().key // Generowanie unikalnego ID dla wydarzenia

        // Przekształcenie danych w mapę
        val eventData = hashMapOf<String, Any>(
            "name" to name,
            "startDate" to startDate,
            "endDate" to endDate,
            "note" to note,
            "players" to selectedPlayers
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

}