package com.anw.tenistats

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.anw.tenistats.databinding.ActivityStartNewBinding
import com.anw.tenistats.ui.theme.NavigationDrawerHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class StartNewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartNewBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private val playersList = mutableListOf<String>()
    private var matchId: String?=null

    /*val dateString = "01/01/1900" // format daty: "dd/MM/yyyy"
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date = dateFormat.parse(dateString) // Sparsowanie daty

    val milliseconds = date?.time ?: 0*/

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize Firebase Auth
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStartNewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()

        //MENU
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.navigationViewMenu)
        val menu = findViewById<ImageButton>(R.id.buttonMenu)
        val headerView = navigationView.getHeaderView(0)
        menu.setOnClickListener{
            drawerLayout.open()
        }
        navigationDrawerHelper = NavigationDrawerHelper(this)
        navigationDrawerHelper.setupNavigationDrawer(drawerLayout, navigationView, firebaseAuth)
        val backButton = findViewById<ImageButton>(R.id.buttonReturnUndo)
        backButton.visibility = View.GONE

        val userEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        val userEmailView = headerView.findViewById<TextView>(R.id.textViewUserEmail)
        if(userEmail.isNotEmpty()) {
            userEmailView.text = userEmail
        }else {
            userEmailView.text = "user_email@smth.com"
        }
        //MENU

        //Weronika 23 marca ~ zapisywanie danych playera do bazy
        //11.04 ~u
        val user = firebaseAuth.currentUser?.uid
        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(user.toString()).child("Players")

        // Pobierz listę graczy z bazy danych
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                playersList.clear()
                for (playerSnapshot in snapshot.children) {
                    val playerName = playerSnapshot.key
                    playerName?.let { playersList.add(it) }
                }
                setupAutoCompleteTextViews()
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsługa błędów odczytu danych z bazy danych
                Toast.makeText(
                    this@StartNewActivity,
                    "Failed to read players from database",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // Obsługa przycisku rozpoczęcia gry
        binding.buttonStartGame.setOnClickListener {
            var player1 = binding.autoNamePlayer1.text.toString().trimEnd() //pobiera Imię i Nazwisko gracza
            var player2 = binding.autoNamePlayer2.text.toString().trimEnd()
            val (player1FirstName, player1LastName) = splitNameToFirstAndLastName(player1)  //zwraca Imię i Nazwisko jako odzielne gracza
            val (player2FirstName, player2LastName) = splitNameToFirstAndLastName(player2)
            val btn1 = binding.checkBoxPlayer1New
            val btn2 = binding.checkBoxPlayer2New
            //~ru 14.04 optymalizacja i poprawa dodawania playera do bazy
            //tak, aby w kolejnym activity obowiązywało nazewnictwo z bazy
            if (player1.isEmpty() || player2.isEmpty()) {
                Toast.makeText(this, "Don't leave empty fields.", Toast.LENGTH_SHORT).show()
            }else{
                //sprawdzenie istnienia player1, jego ewentualne dodanie/duplikowanie
                checkPlayerExistence(player1,player1FirstName, player1LastName, btn1) {updatedPlayer1 ->
                    player1 = updatedPlayer1
                    //sprawdzenie istnienia player2, jego ewentualne dodanie/duplikowanie
                    checkPlayerExistence(player2, player2FirstName, player2LastName, btn2) { updatedPlayer2 ->
                        player2 = updatedPlayer2
                        //zapisanie meczu do bazy
                        createAndSaveMatchData(player1, player2)
                        binding.autoNamePlayer1.text.clear()
                        binding.autoNamePlayer2.text.clear()
                        //w nastepnym Activity zachowuje się nazewnictwo z bazy :)
                        callActivity(player1, player2)
                    }
                }
            }
        }
    }

    private fun createAndSaveMatchData(player1: String, player2: String) {
        //~u
        // Generowanie unikalnego identyfikatora dla meczu
        matchId = database.parent?.child("Matches")?.push()?.key
        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        // Pobieranie bieżącego czasu
        val currentDate = Calendar.getInstance().timeInMillis
        // Tworzenie danych meczu
        val matchData = mapOf<String, Any>(
            "data" to currentDate,
            "player1" to player1,
            "player2" to player2,
            "set1p1" to "0",
            "set1p2" to "0",
            "pkt1" to "0",
            "pkt2" to "0"
        )
        // Zapisywanie danych meczu do bazy danych pod unikalnym identyfikatorem meczu
        database.parent?.child("Matches")?.child(matchId!!)?.setValue(matchData)
        //zapisywanie aktualnie rozgrywanego meczu
        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(user.toString()).child("Current match")
        database.setValue(matchId.toString())
        //kasowanie usawien
        val app = application as Stats
        clearScore(app)
    }

    private fun setupAutoCompleteTextViews() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, playersList)

        binding.autoNamePlayer1.apply {
            setAdapter(adapter)
            threshold = 0
            onFocusChangeListener = View.OnFocusChangeListener { _, b -> if (b) showDropDown() }
        }

        binding.autoNamePlayer2.apply {
            setAdapter(adapter)
            threshold = 0
            onFocusChangeListener = View.OnFocusChangeListener { _, b -> if (b) showDropDown() }
        }
    }

    private fun splitNameToFirstAndLastName(playerName: String): Pair<String?, String?> {
        var firstName: String? = null
        var lastName: String? = null

        val splitName = playerName.split(" ")

        if (splitName.size >= 2) {
            firstName = splitName[0]
            lastName = splitName.subList(1, splitName.size).joinToString("").trimEnd()
        } else if (splitName.size == 1) {
            firstName = splitName[0]
        }

        return Pair(firstName, lastName)
    }
    //optymalizacja funkcji ~ru 12.04
    private fun checkPlayerExistence(
        playerName: String, //nazwa węzła zawodnika, jedo unikalne id w obrębie użytkownika (Imię Nazwisko, z ewentualnym numerem duplikacji)
        firstName: String?, //dana atomowa: Imię
        lastName: String?, //dana atomowa: Nazwisko
        btn: CheckBox,
        callback: (String) -> Unit
    ){
        database.child(playerName).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                if (btn.isChecked) {
                    //przygtowanie numeru duplikatu:
                    database.child(playerName).child("duplicate").get()
                        .addOnSuccessListener { duplicateSnapshot ->
                            //pobranie liczby ilości duplikatów od gracza "bez numerka"
                            val currentDuplicate = duplicateSnapshot.getValue(Int::class.java) ?: 0
                            val newDuplicate = currentDuplicate + 1 //zwiększemie liczby duplikatów o jeden
                            database.child(playerName).child("duplicate").setValue(newDuplicate)
                                .addOnSuccessListener {
                                    //dodanie numeru duplikatu do Nazwiska
                                    val playerToReturn =  playerName + newDuplicate.toString()

                                    val player = Player(
                                        firstName,
                                        lastName,
                                        newDuplicate,
                                        "",
                                        null,
                                        null,
                                        "",
                                        ""
                                    )
                                    // dodanie gracza do bazy (jako obiekt klasy Player)
                                    database.child(playerToReturn).setValue(player)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                this,
                                                "Player $playerName Successfully Saved",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            callback(playerToReturn)
                                        }.addOnFailureListener {
                                            Toast.makeText(
                                                this,
                                                "Failed to save player $playerName",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                        }.addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Failed to get duplicate from player",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    btn.isChecked=false
                } else { callback(playerName) }
            } else {
                //jeżeli player nie istnieje, stwórz obiekt klasy Player
                val player = Player(
                    firstName,
                    lastName,
                    1,
                    "",
                    null,
                    null,
                    "",
                    ""
                )
                //dodaj taki węzeł
                database.child(playerName).setValue(player)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Player $playerName Successfully Saved",
                            Toast.LENGTH_SHORT
                        ).show()
                        callback(playerName)
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to save player $playerName", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }.addOnFailureListener{
            Toast.makeText(this, "Failed to get player $playerName", Toast.LENGTH_SHORT)
                .show()
        }
    }


    private fun callActivity(player1:String, player2:String) {
        val intent = Intent(this, ActivityServe::class.java).apply {
            putExtra("DanePlayer1", player1)
            putExtra("DanePlayer2", player2)
            //~u //hej ~w //hej hej ~u
            putExtra("matchID",matchId)
        }
        startActivity(intent)
    }
}