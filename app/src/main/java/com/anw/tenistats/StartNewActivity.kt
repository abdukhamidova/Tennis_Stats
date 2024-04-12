package com.anw.tenistats

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.anw.tenistats.databinding.ActivityStartNewBinding
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
    private val playersList = mutableListOf<String>()
    var matchId: String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize Firebase Auth
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStartNewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Weronika 23 marca ~ zapisywanie danych do bazy

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //11.04 ~u
        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(user.toString()).child("Players")

        //to skomentowalam dla bezpieczenstwa, na 99,(9)% mozna usunac ~u
        /*firebaseAuth = FirebaseAuth.getInstance()
        database =
            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Player")*/

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

            // Sprawdzenie istnienia gracza dla player1 & jego dodanie
            checkPlayerExistence(player1, player1FirstName, player1LastName, btn1) { updatedPlayerName ->
                player1 = updatedPlayerName
            }

            // Sprawdzenie istnienia gracza dla player2 & jego dodanie
            checkPlayerExistence(player2, player2FirstName, player2LastName, btn2){ updatedPlayerName ->
                player2 = updatedPlayerName
            }
            //jeżeli zawodnik istnieje w bazie i nie jest oznaczony jako do duplikacji to nic się z nim nie dzieje
            Toast.makeText(this, "Player1 = $player1", Toast.LENGTH_SHORT).show()
            Toast.makeText(this, "Player2 = $player2", Toast.LENGTH_SHORT).show()
            //~u
            // Generowanie unikalnego identyfikatora dla meczu
            matchId = database.parent?.child("Matches")?.push()?.key
            // Pobieranie bieżącego czasu
            val currentDate = Calendar.getInstance().timeInMillis
            // Tworzenie danych meczu
            val matchData = mapOf<String, Any>(
                "data" to currentDate,
                "player1" to player1,
                "player2" to player2
            )
            // Zapisywanie danych meczu do bazy danych pod unikalnym identyfikatorem meczu
            database.parent?.child("Matches")?.child(matchId!!)?.setValue(matchData)

            callActivity()
        }
    }

    private fun setupAutoCompleteTextViews() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, playersList)

        binding.autoNamePlayer1.apply {
            setAdapter(adapter)
            threshold = 0
            onFocusChangeListener = View.OnFocusChangeListener { view, b -> if (b) showDropDown() }
        }

        binding.autoNamePlayer2.apply {
            setAdapter(adapter)
            threshold = 0
            onFocusChangeListener = View.OnFocusChangeListener { view, b -> if (b) showDropDown() }
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
         //dana atomowa: numer duplikacji
    ){
        var playerToReturn = playerName
        var updated = false
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
                                    playerToReturn =  playerName + newDuplicate.toString()
                                    updated = true
                                    val player = Player(
                                        firstName,
                                        lastName,
                                        newDuplicate
                                    )
                                    // dodanie gracza do bazy (jako obiekt klasy Player)
                                    database.child(playerToReturn).setValue(player)
                                        .addOnSuccessListener {
                                            if (playerName == binding.autoNamePlayer1.text.toString()) {
                                                binding.autoNamePlayer1.text.clear()
                                            } else {
                                                binding.autoNamePlayer2.text.clear()
                                            }
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
                }
            } else {
                //jeżeli player nie istnieje, stwórz obiekt klasy Player
                val player = Player(firstName, lastName, 1)
                //dodaj taki węzeł
                database.child(playerName).setValue(player)
                    .addOnSuccessListener {
                        if (playerName == binding.autoNamePlayer1.text.toString()) {
                            binding.autoNamePlayer1.text.clear()
                        } else {
                            binding.autoNamePlayer2.text.clear()
                        }
                        Toast.makeText(
                            this,
                            "Player $playerName Successfully Saved",
                            Toast.LENGTH_SHORT
                        ).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to save player $playerName", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }.addOnFailureListener{
            Toast.makeText(this, "Failed to get player $playerName", Toast.LENGTH_SHORT)
                .show()
        }.addOnCompleteListener {
            if (!updated) { // Jeśli zmienna nie została zaktualizowana, zwróć początkową wartość
                callback(playerName)
            }
        }
    }

    fun callActivity() {
        val player1 = binding.autoNamePlayer1.text.toString()
        val player2 = binding.autoNamePlayer2.text.toString()

        if (player1.isEmpty() || player2.isEmpty()) {
            Toast.makeText(this, "Don't leave empty fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ActivityServe::class.java).apply {
            putExtra("DanePlayer1", player1)
            putExtra("DanePlayer2", player2)
            //~u
            putExtra("matchID",matchId)
        }
        startActivity(intent)
    }
}
