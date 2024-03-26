package com.anw.tenistats

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
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
import com.google.firebase.ktx.Firebase

class StartNewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartNewBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private  lateinit var database: DatabaseReference
    private val playersList = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize Firebase Auth
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStartNewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Weronika 23 marca ~ zapisywanie danych do bazy
        //setContentView(R.layout.activity_start_new)



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //Rushana to Twoje?? ~W
        //ActivityStartNewBin=binding
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Player")

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
                Toast.makeText(this@StartNewActivity, "Failed to read players from database", Toast.LENGTH_SHORT).show()
            }
        })

        // Obsługa przycisku rozpoczęcia gry
        binding.buttonStartGame.setOnClickListener {
            val player1 = binding.autoNamePlayer1.text.toString().trimEnd()
            val player2 = binding.autoNamePlayer2.text.toString().trimEnd()
            val (player1FirstName, player1LastName) = splitNameToFirstAndLastName(player1)
            val (player2FirstName, player2LastName) = splitNameToFirstAndLastName(player2)

            // Sprawdzenie istnienia gracza dla player1
            checkPlayerExistence(player1, player1FirstName, player1LastName)

            // Sprawdzenie istnienia gracza dla player2
            checkPlayerExistence(player2, player2FirstName, player2LastName)

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

    private fun checkPlayerExistence(playerName: String, firstName: String?, lastName: String?) {
        database.child(playerName).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                binding.autoNamePlayer1.text.clear()
                binding.autoNamePlayer2.text.clear()
                Toast.makeText(this, "Player $playerName already exists", Toast.LENGTH_SHORT).show()
            } else {
                val player = if (playerName == binding.autoNamePlayer1.text.toString()) {
                    Player1(firstName, lastName)
                } else {
                    Player2(firstName, lastName)
                }
                database.child(playerName).setValue(player).addOnSuccessListener {
                    if (playerName == binding.autoNamePlayer1.text.toString()) {
                        binding.autoNamePlayer1.text.clear()
                    } else {
                        binding.autoNamePlayer2.text.clear()
                    }
                    Toast.makeText(this, "Player $playerName Successfully Saved", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to save player $playerName", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun callActivity() {
        val player1 = binding.autoNamePlayer1.text.toString()
        val player2 = binding.autoNamePlayer2.text.toString()

        val intent = Intent(this, ActivityServe::class.java).apply {
            putExtra("DanePlayer1", player1)
            putExtra("DanePlayer2", player2)
        }
        if(player1.isNotEmpty() && player2.isNotEmpty()) {
            startActivity(intent)
        }else {
            Toast.makeText(this,"Don't leave empty fields.",Toast.LENGTH_SHORT).show()
        }
        //nie jestem pewno co do komentowania tego ~ru
        //startActivity(intent)
    }
}