package com.anw.tenistats.tournament

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.anw.tenistats.R
import com.anw.tenistats.databinding.ActivityUpdateEditMatchBinding
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class UpdateEditMatchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateEditMatchBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var databaseT: DatabaseReference
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var tournamentId : String
    private lateinit var matchNumber : String
    private lateinit var drawSize : String

    private lateinit var p1 : EditText
    private lateinit var p2 : EditText
    private lateinit var set1p2 : Spinner
    private lateinit var set1p1 : Spinner
    private lateinit var set2p1 : Spinner
    private lateinit var set2p2 : Spinner
    private lateinit var set3p1 : Spinner
    private lateinit var set3p2 : Spinner
    private lateinit var pN1 : TextView
    private lateinit var pN2 : TextView
    private lateinit var submitButton : Button
    private lateinit var winner: Spinner //TODO: ~u

    //TODO: przyciski: Walkover, Retired (krecz) i ScoreUnknown ~u
    private lateinit var buttonWalkover: Button
    private var buttonWalkoverClicked: Boolean = false
    private lateinit var buttonRetired: Button
    private var buttonRetiredClicked: Boolean = false
    private lateinit var buttonUnknown: Button
    private var buttonUnknownClicked: Boolean = false
    //TODO: przyciski: Walkover, Retired (krecz) i ScoreUnknown ~u

    // Declare variables to store original values
    private var originalPlayer1Value: String? = null
    private var originalPlayer2Value: String? = null
    private var originalSet1p1Value: String? = null
    private var originalSet1p2Value: String? = null
    private var originalSet2p1Value: String? = null
    private var originalSet2p2Value: String? = null
    private var originalSet3p1Value: String? = null
    private var originalSet3p2Value: String? = null
    private var originalWinnerValue: String? = null //TODO: ~u

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUpdateEditMatchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()

        //region ---MENU---
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val menu = findViewById<ImageButton>(R.id.buttonMenu)
        val navigationView = findViewById<NavigationView>(R.id.navigationViewMenu)
        val headerView = navigationView.getHeaderView(0)

        menu.setOnClickListener {
            drawerLayout.open()
        }
        navigationDrawerHelper = NavigationDrawerHelper(this)
        navigationDrawerHelper.setupNavigationDrawer(drawerLayout, navigationView, firebaseAuth)
        val backButton = findViewById<ImageButton>(R.id.buttonUndo)
        backButton.visibility = View.GONE

        val userEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        if (userEmail.isNotEmpty()) {
            headerView.findViewById<TextView>(R.id.textViewUserEmail).text = userEmail
        } else {
            findViewById<TextView>(R.id.textViewUserEmail).text =
                resources.getString(R.string.user_email)
        }
        //endregion

        tournamentId = intent.getStringExtra("tournament_id").toString()
        matchNumber = intent.getStringExtra("match_number").toString()
        drawSize = intent.getStringExtra("draw_size").toString()

        database = FirebaseDatabase.getInstance(
            "https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/"
        ).getReference("Tournaments").child(tournamentId).child(matchNumber)
        databaseT=FirebaseDatabase.getInstance(
            "https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/"
        ).getReference("Tournaments").child(tournamentId)

        p1 = binding.editTextPlayer1U
        p2 = binding.editTextPlayer2U

        set1p1 = binding.set1p1ScoreU
        set1p2 = binding.set1p2ScoreU
        set2p1 = binding.set2p1ScoreU
        set2p2 = binding.set2p2ScoreU
        set3p1 = binding.set3p1ScoreU
        set3p2 = binding.set3p2ScoreU
        winner = binding.spinnerWinner //TODO: ~u
        winner.isEnabled = false ////TODO: spinner winnera do wybrania tylko gdy user wybierze walkover, retider lub scoreUnknown - inaczej niedostępny, bo z samego wyniku wyliczany jest winner ~u

        pN1 = binding.TextViewPlayer1
        pN2 = binding.TextViewPlayer2

        //TODO: ~u
        buttonWalkover = binding.buttonWalkover
        buttonRetired = binding.buttonRetired
        buttonUnknown = binding.buttonScoreUnknown
        //TODO: ~u

        // Ładowanie danych z Firebase do pól
        loadMatchData()
        winnersList() //TODO: ustawianie w spinnerze odpowiednich nazwisk ~u
        pointsList()

        //TODO: obsluga przyciskow Walkover, Retider i ScoreUnknown ~u
        buttonWalkover.setOnClickListener { 
            if(buttonWalkoverClicked){ //odznaczono "walkover"
                buttonWalkoverClicked = false //zmiana na odklikniety
                binding.linearLayoutWinner.visibility = View.GONE
                //zmiana kolorow buttona
                buttonWalkover.setBackgroundResource(R.drawable.border_button)
                buttonWalkover.setTextColor(ContextCompat.getColor(this, R.color.general_text_color))
                //odblokowanie spinnerow do wyniku
                set1p1.isEnabled = true
                set1p2.isEnabled = true
                set2p1.isEnabled = true
                set2p2.isEnabled = true
                set3p1.isEnabled = true
                set3p2.isEnabled = true
                //zablokowanie spinnera winner dla usera
                winner.isEnabled = false
            }
            else{ //zaznaczono "walkover"
                buttonWalkoverClicked = true //zmiana na klikniety
                //jesli inne buttony klikniete to odkliknij
                if(buttonRetiredClicked)
                    buttonRetired.performClick()
                if(buttonUnknownClicked)
                    buttonUnknown.performClick()
                binding.linearLayoutWinner.visibility = View.VISIBLE
                //zmiana kolorow
                buttonWalkover.setBackgroundResource(R.drawable.rounded_button)
                buttonWalkover.setTextColor(ContextCompat.getColor(this, R.color.white))
                //ustawienie wynikow na "None" i zablokowanie wyniku
                set1p1.setSelection(0); set1p1.isEnabled = false
                set1p2.setSelection(0); set1p2.isEnabled = false
                set2p1.setSelection(0); set2p1.isEnabled = false
                set2p2.setSelection(0); set2p2.isEnabled = false
                set3p1.setSelection(0); set3p1.isEnabled = false
                set3p2.setSelection(0); set3p2.isEnabled = false
                //odblokowanie spinnera winner dla usera
                winner.isEnabled = true
            }
        }
        buttonRetired.setOnClickListener {
            if(buttonRetiredClicked){ //odznaczono "Retired"
                buttonRetiredClicked = false //zmiana na odklikniety
                binding.linearLayoutWinner.visibility = View.GONE
                //zmiana kolorow buttona
                buttonRetired.setBackgroundResource(R.drawable.border_button)
                buttonWalkover.setTextColor(ContextCompat.getColor(this, R.color.general_text_color))
                //zablokowanie spinnera winner dla usera
                winner.isEnabled = false
            }
            else{ //zaznaczono "Retired"
                buttonRetiredClicked = true //zmiana na klikniety
                //jesli inne buttony klikniete to odkliknij
                if(buttonWalkoverClicked)
                    buttonWalkover.performClick()
                if(buttonUnknownClicked)
                    buttonUnknown.performClick()
                binding.linearLayoutWinner.visibility = View.VISIBLE
                //zmiana kolorow
                buttonRetired.setBackgroundResource(R.drawable.rounded_button)
                buttonWalkover.setTextColor(ContextCompat.getColor(this, R.color.white))
                //odblokowanie spinnera winner dla usera
                winner.isEnabled = true
            }
        }
        buttonUnknown.setOnClickListener {
            if(buttonUnknownClicked){ //odznaczono "Unknown"
                buttonUnknownClicked = false //zmiana na odklikniety
                binding.linearLayoutWinner.visibility = View.GONE
                //zmiana kolorow buttona
                buttonUnknown.setBackgroundResource(R.drawable.border_button)
                buttonWalkover.setTextColor(ContextCompat.getColor(this, R.color.general_text_color))
                //odblokowanie spinnerow do wyniku
                set1p1.isEnabled = true
                set1p2.isEnabled = true
                set2p1.isEnabled = true
                set2p2.isEnabled = true
                set3p1.isEnabled = true
                set3p2.isEnabled = true
                //zablokowanie spinnera winner dla usera
                winner.isEnabled = false
            }
            else{ //zaznaczono "Unknown"
                buttonUnknownClicked = true //zmiana na klikniety
                //jesli inne buttony klikniete to odkliknij
                if(buttonWalkoverClicked)
                    buttonWalkover.performClick()
                if(buttonRetiredClicked)
                    buttonRetired.performClick()
                binding.linearLayoutWinner.visibility = View.VISIBLE
                //zmiana kolorow
                buttonUnknown.setBackgroundResource(R.drawable.rounded_button)
                buttonWalkover.setTextColor(ContextCompat.getColor(this, R.color.white))
                //ustawienie wynikow na "None" i zablokowanie wyniku
                set1p1.setSelection(0); set1p1.isEnabled = false
                set1p2.setSelection(0); set1p2.isEnabled = false
                set2p1.setSelection(0); set2p1.isEnabled = false
                set2p2.setSelection(0); set2p2.isEnabled = false
                set3p1.setSelection(0); set3p1.isEnabled = false
                set3p2.setSelection(0); set3p2.isEnabled = false
                //odblokowanie spinnera winner dla usera
                winner.isEnabled = true
            }
        }
        //TODO: obsluga przyciskow Walkover, Retider i ScoreUnknown ~u
        
        submitButton = binding.buttonSubmitEdit
        submitButton.setOnClickListener {
            //TODO: walidacja wprowadzonego wyniku ~u
            if(p1.text.isNullOrEmpty() || p2.text.isNullOrEmpty()){
                Toast.makeText(this, "Don't leave empty fields of players name.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Zatrzymujemy dalsze przetwarzanie
            }
            val message = checkMatchScoreAndWinner()
            if(message != "OK"){
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Zatrzymujemy dalsze przetwarzanie
            }
            //TODO: walidacja wprowadzonego wyniku ~u
            saveMatchData()
            val intent = Intent(this, GenerateDrawActivity::class.java)
            intent.putExtra("tournament_id", tournamentId)
            intent.putExtra("match_number", matchNumber)
            intent.putExtra("draw_size", drawSize)
            startActivity(intent)
        }
    }

    private fun loadMatchData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Pobieramy wartości z bazy danych
                val player1Value = snapshot.child("player1").getValue(String::class.java)
                val player2Value = snapshot.child("player2").getValue(String::class.java)
                val set1p1Value = snapshot.child("set1p1").getValue(String::class.java)
                val set1p2Value = snapshot.child("set1p2").getValue(String::class.java)
                val set2p1Value = snapshot.child("set2p1").getValue(String::class.java)
                val set2p2Value = snapshot.child("set2p2").getValue(String::class.java)
                val set3p1Value = snapshot.child("set3p1").getValue(String::class.java)
                val set3p2Value = snapshot.child("set3p2").getValue(String::class.java)
                val winnerValue = snapshot.child("winner").getValue(String::class.java) //TODO: ~u

                // Ustawienie pól w UI
                p1.setText(player1Value)
                p2.setText(player2Value)
                winnersList() //TODO: ustawianie w spinnerze odpowiednich nazwisk  ~u
                set1p1.setSelection(getIndexOfOption(set1p1Value))
                set1p2.setSelection(getIndexOfOption(set1p2Value))
                set2p1.setSelection(getIndexOfOption(set2p1Value))
                set2p2.setSelection(getIndexOfOption(set2p2Value))
                set3p1.setSelection(getIndexOfOption(set3p1Value))
                set3p2.setSelection(getIndexOfOption(set3p2Value))
                set3p2.setSelection(getIndexOfOption(set3p2Value))
                winner.setSelection(getIndexOfWinnersOption(winnerValue)) //TODO: ~u

                pN1.text = player1Value
                pN2.text = player2Value

                // Save original values for comparison later
                originalPlayer1Value = player1Value
                originalPlayer2Value = player2Value
                originalSet1p1Value = set1p1Value
                originalSet1p2Value = set1p2Value
                originalSet2p1Value = set2p1Value
                originalSet2p2Value = set2p2Value
                originalSet3p1Value = set3p1Value
                originalSet3p2Value = set3p2Value
                originalWinnerValue = winnerValue //TODO: ~u
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error: ${error.message}")
            }
        })
    }

    //TODO: dodolam "7" do spinnera gemów i zmieniłam kolor spinnera ~u
    private fun getIndexOfOption(value: String?): Int {
        val options = listOf("None", "0", "1", "2", "3", "4", "5", "6","7")
        return options.indexOf(value)
    }
    private fun pointsList() {
        val options = listOf("None", "0", "1", "2", "3", "4", "5", "6","7")
        val adapter = ArrayAdapter(
            this,
            R.layout.spinner_item_stats_right, // domyślny układ elementu //zmienilam na ten customowy w kolorze bg ~u
            options // dane do wyświetlenia
        )

        binding.set1p1ScoreU.adapter = adapter
        binding.set1p2ScoreU.adapter = adapter
        binding.set2p1ScoreU.adapter = adapter
        binding.set2p2ScoreU.adapter = adapter
        binding.set3p1ScoreU.adapter = adapter
        binding.set3p2ScoreU.adapter = adapter
    }
    //TODO: dodolam "7" do spinnera gemów i zmieniłam kolor spinnera ~u

    //TODO: ustawienie spinnera do Winnera ~u
    private fun getIndexOfWinnersOption(value: String?): Int {
        val winnersOptions = listOf("None",p1.text.toString(),p2.text.toString())
        when(value){
            "player1" -> return winnersOptions.indexOf(p1.text.toString())
            "player2" -> return winnersOptions.indexOf(p2.text.toString())
            else -> return winnersOptions.indexOf("None")
        }
    }
    private fun winnersList(){
        val winnersOptions = listOf("None",p1.text.toString(),p2.text.toString())
        val adapter = ArrayAdapter(
            this,
            R.layout.spinner_item_stats_right,
            winnersOptions
        )
        binding.spinnerWinner.adapter = adapter
    }
    //TODO: ustawienie spinnera do Winnera ~u

    private fun saveMatchData() {
        // Zbieranie danych z pól
        val player1Value = p1.text?.toString() ?: ""
        val player2Value = p2.text?.toString() ?: ""
        val set1p1Value = set1p1.selectedItem?.toString() ?: "None"
        val set1p2Value = set1p2.selectedItem?.toString() ?: "None"
        val set2p1Value = set2p1.selectedItem?.toString() ?: "None"
        val set2p2Value = set2p2.selectedItem?.toString() ?: "None"
        val set3p1Value = set3p1.selectedItem?.toString() ?: "None"
        val set3p2Value = set3p2.selectedItem?.toString() ?: "None"
        //TODO: zmiana z nazwiska na "player1" lub "player2" ze spinnera Winner ~u
        var winnerValue: String
        if(winner.selectedItem?.toString() == p1.text.toString()) winnerValue = "player1"
        else if(winner.selectedItem?.toString() == p2.text.toString()) winnerValue = "player2"
        else winnerValue = "None"
        //TODO: zmiana z nazwiska na "player1" lub "player2" ze spinnera Winner ~u

        // Sprawdzenie, czy wartości zostały zmienione, jeśli tak to zapisujemy je w Firebase
        if (player1Value != originalPlayer1Value && player1Value.isNotEmpty()) {
            database.child("player1Edit").setValue(player1Value)
            database.child("changes").setValue(true)
            databaseT.child("changes").get().addOnSuccessListener { snapshot ->
                val currentChanges = snapshot.getValue(Int::class.java) ?: 0
                val updatedChanges = currentChanges + 1

                databaseT.child("changes").setValue(updatedChanges)
            }.addOnFailureListener {
                Log.e("Firebase", "Error getting changes", it)
            }
        }
        if (player2Value != originalPlayer2Value && player2Value.isNotEmpty()) {
            database.child("player2Edit").setValue(player2Value)
            database.child("changes").setValue(true)
            databaseT.child("changes").get().addOnSuccessListener { snapshot ->
                val currentChanges = snapshot.getValue(Int::class.java) ?: 0
                val updatedChanges = currentChanges + 1

                databaseT.child("changes").setValue(updatedChanges)
            }.addOnFailureListener {
                Log.e("Firebase", "Error getting changes", it)
            }
        }
        if (set1p1Value != originalSet1p1Value && set1p1Value != "None") {
            database.child("set1p1Edit").setValue(set1p1Value)
            database.child("changes").setValue(true)
            databaseT.child("changes").get().addOnSuccessListener { snapshot ->
                val currentChanges = snapshot.getValue(Int::class.java) ?: 0
                val updatedChanges = currentChanges + 1

                databaseT.child("changes").setValue(updatedChanges)
            }.addOnFailureListener {
                Log.e("Firebase", "Error getting changes", it)
            }
        }
        if (set1p2Value != originalSet1p2Value && set1p2Value != "None") {
            database.child("set1p2Edit").setValue(set1p2Value)
            database.child("changes").setValue(true)
            databaseT.child("changes").get().addOnSuccessListener { snapshot ->
                val currentChanges = snapshot.getValue(Int::class.java) ?: 0
                val updatedChanges = currentChanges + 1

                databaseT.child("changes").setValue(updatedChanges)
            }.addOnFailureListener {
                Log.e("Firebase", "Error getting changes", it)
            }
        }
        if (set2p1Value != originalSet2p1Value && set2p1Value != "None") {
            database.child("set2p1Edit").setValue(set2p1Value)
            database.child("changes").setValue(true)
            databaseT.child("changes").get().addOnSuccessListener { snapshot ->
                val currentChanges = snapshot.getValue(Int::class.java) ?: 0
                val updatedChanges = currentChanges + 1

                databaseT.child("changes").setValue(updatedChanges)
            }.addOnFailureListener {
                Log.e("Firebase", "Error getting changes", it)
            }
        }
        if (set2p2Value != originalSet2p2Value && set2p2Value != "None") {
            database.child("set2p2Edit").setValue(set2p2Value)
            database.child("changes").setValue(true)
            databaseT.child("changes").get().addOnSuccessListener { snapshot ->
                val currentChanges = snapshot.getValue(Int::class.java) ?: 0
                val updatedChanges = currentChanges + 1

                databaseT.child("changes").setValue(updatedChanges)
            }.addOnFailureListener {
                Log.e("Firebase", "Error getting changes", it)
            }
        }
        if (set3p1Value != originalSet3p1Value && set3p1Value != "None") {
            database.child("set3p1Edit").setValue(set3p1Value)
            database.child("changes").setValue(true)
            databaseT.child("changes").get().addOnSuccessListener { snapshot ->
                val currentChanges = snapshot.getValue(Int::class.java) ?: 0
                val updatedChanges = currentChanges + 1

                databaseT.child("changes").setValue(updatedChanges)
            }.addOnFailureListener {
                Log.e("Firebase", "Error getting changes", it)
            }
        }
        if (set3p2Value != originalSet3p2Value && set3p2Value != "None") {
            database.child("set3p2Edit").setValue(set3p2Value)
            database.child("changes").setValue(true)
            databaseT.child("changes").get().addOnSuccessListener { snapshot ->
                val currentChanges = snapshot.getValue(Int::class.java) ?: 0
                val updatedChanges = currentChanges + 1

                databaseT.child("changes").setValue(updatedChanges)
            }.addOnFailureListener {
                Log.e("Firebase", "Error getting changes", it)
            }
        }
        //TODO: ~u
        if (winnerValue != originalWinnerValue && winnerValue != "None") {
            database.child("winnerEdit").setValue(winnerValue)
            database.child("changes").setValue(true)
            databaseT.child("changes").get().addOnSuccessListener { snapshot ->
                val currentChanges = snapshot.getValue(Int::class.java) ?: 0
                val updatedChanges = currentChanges + 1

                databaseT.child("changes").setValue(updatedChanges)
            }.addOnFailureListener {
                Log.e("Firebase", "Error getting changes", it)
            }
        }
        //TODO: ~u

        // Informacja o zapisaniu danych
        Log.d("Firebase", "Dane zostały zapisane!")
    }

    //TODO: walidacja wyniku i wyliczanie winnera z wyniku
    private fun checkMatchScoreAndWinner() : String{
        val s1p1: String = binding.set1p1ScoreU.selectedItem.toString()
        val s1p2: String = binding.set1p2ScoreU.selectedItem.toString()
        val s2p1: String = binding.set2p1ScoreU.selectedItem.toString()
        val s2p2: String = binding.set2p2ScoreU.selectedItem.toString()
        val s3p1: String = binding.set3p1ScoreU.selectedItem.toString()
        val s3p2: String = binding.set3p2ScoreU.selectedItem.toString()

        var set1Winner: String = ""
        var set2Winner: String = ""
        var set3Winner: String = ""

        if(buttonWalkoverClicked){
            if(winner.selectedItem.toString() == "None") return "Choose winner of this match"
            return "OK"
        }
        if(buttonRetiredClicked){
            if(winner.selectedItem.toString() == "None") return "Choose winner of this match"
            if(s1p1.equals("None") || s1p2.equals("None")) return "Values of 1st set can not be empty"
            if(!((s1p1.toInt() == 6 && s1p1.toInt() - s1p2.toInt() > 1) || (s1p1.toInt() == 7 && (s1p2.toInt() == 6 || s1p2.toInt() == 5)) ||
                (s1p2.toInt() == 6 && s1p2.toInt() - s1p1.toInt() > 1) || (s1p2.toInt() == 7 && (s1p1.toInt() == 6 || s1p1.toInt() == 5)))){ //nie skonczyl sie pierwszy set -> inne pola musza nyc "None"
                if(!(s2p1.equals("None") && s2p2.equals("None")))
                    return "Incorrect score in 2nd set - 1st set is not finished"
                if(!(s3p1.equals("None") && s3p2.equals("None")))
                    return "Incorrect score in 3rd set - 1st set is not finished"
                return "OK"
            }
            if(!((s2p1.toInt() == 6 && s2p1.toInt() - s2p2.toInt() > 1) || (s2p1.toInt() == 7 && (s2p2.toInt() == 6 || s2p2.toInt() == 5)) ||
                        (s2p2.toInt() == 6 && s2p2.toInt() - s2p1.toInt() > 1) || (s2p2.toInt() == 7 && (s2p1.toInt() == 6 || s2p1.toInt() == 5)))){ //nie skonczyl sie drugi set -> inne pola musza nyc "None"
                if(!(s3p1.equals("None") && s3p2.equals("None")))
                    return "Incorrect score in 3rd set - 2nd set is not finished"
                return "OK"
            }
            if((s1p1 > s1p2 && s2p1 > s2p2) || (s1p2 > s1p1 && s2p2 > s2p1)) return "The match is finished - change the score or uncheck retired button"
            if((s3p1.toInt() == 6 && s3p1.toInt() - s3p2.toInt() > 1) || (s3p1.toInt() == 7 && (s3p2.toInt() == 6 || s3p2.toInt() == 5)) ||
                (s3p2.toInt() == 6 && s3p2.toInt() - s3p1.toInt() > 1) || (s3p2.toInt() == 7 && (s3p1.toInt() == 6 || s3p1.toInt() == 5))){ //mecz sie skonczyl - czyli nie powinien byc krecz
                return "The match is finished - change the score or uncheck retired button"
            }
            return "OK"
        }
        if(buttonUnknownClicked){
            if(winner.selectedItem.toString() == "None") return "Choose winner of this match"
            return "OK"
        }

        if(s1p1 == "None" || s1p2 == "None") return "Values of 1st set can not be empty"
        if(s1p1.toInt() > s1p2.toInt()) {
            if ((s1p1.toInt() == 6 && s1p1.toInt() - s1p2.toInt() > 1) || (s1p1.toInt() == 7 && (s1p2.toInt() == 6 || s1p2.toInt() == 5)))
                set1Winner = "player1"
            else{ //zle podany wynik
                return "Incorrect score in 1st set"
            }
        }
        else if(s1p1.toInt() < s1p2.toInt()){
            if ((s1p2.toInt() == 6 && s1p2.toInt() - s1p1.toInt() > 1) || (s1p2.toInt() == 7 && (s1p1.toInt() == 6 || s1p1.toInt() == 5)))
                set1Winner = "player2"
            else{ //zle podany wynik
                return "Incorrect score in 1st set"
            }
        }
        else{
            return "Incorrect score in 1st set"
        }

        if(s2p1.toInt() > s2p2.toInt()) {
            if ((s2p1.toInt() == 6 && s2p1.toInt() - s2p2.toInt() > 1) || (s2p1.toInt() == 7 && (s2p2.toInt() == 6 || s2p2.toInt() == 5)))
                set2Winner = "player1"
            else{ //zle podany wynik
                return "Incorrect score in 2nd set"
            }
        }
        else if(s2p1.toInt() < s2p2.toInt()){
            if ((s2p2.toInt() == 6 && s2p2.toInt() - s2p1.toInt() > 1) || (s2p2.toInt() == 7 && (s2p1.toInt() == 6 || s2p1.toInt() == 5)))
                set2Winner = "player2"
            else{ //zle podany wynik
                return "Incorrect score in 2nd set"
            }
        }
        else {
            return "Incorrect score in 2nd set"
        }
        
        if(set1Winner.equals(set2Winner)){
            if(s3p1 == "None" && s3p2 == "None"){
                winner.setSelection(getIndexOfWinnersOption(set1Winner))
                return "OK"
            }
            else{
                return "Incorrect score in 3rd set"
            }
        }
        else{
            if(s3p1.toInt() > s3p2.toInt()) {
                if ((s3p1.toInt() == 6 && s3p1.toInt() - s3p2.toInt() > 1) || (s3p1.toInt() == 7 && s3p2.toInt() == 6))
                    set3Winner = "player1"
                else{ //zle podany wynik
                    return "Incorrect score in 3rd set"
                }
            }
            else if(s3p1.toInt() < s3p2.toInt()){
                if ((s3p2.toInt() == 6 && s3p2.toInt() - s3p1.toInt() > 1) || (s3p2.toInt() == 7 && s3p1.toInt() == 6))
                    set3Winner = "player2"
                else{ //zle podany wynik
                    return "Incorrect score in 3rd set"
                }
            }
            else {
                return "Incorrect score in 3rd set"
            }
        }
        winner.setSelection(getIndexOfWinnersOption(set3Winner))
        return "OK"
    }
}
