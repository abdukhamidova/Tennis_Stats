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
import com.anw.tenistats.dialog.PlayNewOrAttachMatchDialog
import com.anw.tenistats.stats.ViewStatsActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class UpdateEditMatchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateEditMatchBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var user : String
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
    private lateinit var winner: Spinner

    private lateinit var buttonWalkover: Button
    private var buttonWalkoverClicked: Boolean = false
    private lateinit var buttonRetired: Button
    private var buttonRetiredClicked: Boolean = false
    private lateinit var buttonUnknown: Button
    private var buttonUnknownClicked: Boolean = false

    // Declare variables to store original values
    private var originalPlayer1Value: String? = null
    private var originalPlayer2Value: String? = null
    private var originalSet1p1Value: String? = null
    private var originalSet1p2Value: String? = null
    private var originalSet2p1Value: String? = null
    private var originalSet2p2Value: String? = null
    private var originalSet3p1Value: String? = null
    private var originalSet3p2Value: String? = null
    private var originalWinnerValue: String? = null

    //firstUpdate - globalna mapa pilnująca pierwszy update
    val keys = listOf("p1", "p2", "set1p1", "set1p2", "set2p1", "set2p2", "set3p1", "set3p2", "winner")
    val firstUpdate = keys.associateWith { false }.toMutableMap()
    private var isCreator: Boolean = false

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

        user = FirebaseAuth.getInstance().currentUser?.uid.toString()

        tournamentId = intent.getStringExtra("tournament_id").toString()
        matchNumber = intent.getStringExtra("match_number").toString()
        drawSize = intent.getStringExtra("draw_size").toString()

        FirebaseDatabase.getInstance(
            "https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("Tournaments").child(tournamentId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val creator = snapshot.child("creator").getValue(String::class.java)
                    isCreator = creator.equals(user)
                    firstUpdate.forEach { (key, _) ->
                        firstUpdate[key] = isCreator
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error: ${error.message}")
                }
            })

        database = FirebaseDatabase.getInstance(
            "https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/"
        ).getReference("Tournaments").child(tournamentId).child(matchNumber)
        databaseT=FirebaseDatabase.getInstance(
            "https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/"
        ).getReference("Tournaments").child(tournamentId)

        p1 = binding.editTextPlayer1U
        p2 = binding.editTextPlayer2U

        val layout: View = findViewById(R.id.main) // np. ConstraintLayout, RelativeLayout, itd.
        layout.setOnClickListener {
            if (p1.hasFocus()) p1.clearFocus()
            else if (p2.hasFocus()) p2.clearFocus()
        }
        p1.setOnFocusChangeListener { _, hasFocus ->
            winnersList()
        }
        p2.setOnFocusChangeListener { _, hasFocus ->
            winnersList()
        }

        set1p1 = binding.set1p1ScoreU
        set1p2 = binding.set1p2ScoreU
        set2p1 = binding.set2p1ScoreU
        set2p2 = binding.set2p2ScoreU
        set3p1 = binding.set3p1ScoreU
        set3p2 = binding.set3p2ScoreU
        winner = binding.spinnerWinner
        winner.isEnabled = false //spinner winnera do wybrania tylko gdy user wybierze walkover, retider lub scoreUnknown - inaczej niedostępny, bo z samego wyniku wyliczany jest winner ~u

        pN1 = binding.TextViewPlayer1
        pN2 = binding.TextViewPlayer2

        buttonWalkover = binding.buttonWalkover
        buttonRetired = binding.buttonRetired
        buttonUnknown = binding.buttonScoreUnknown

        submitChanges()
        // Ładowanie danych z Firebase do pól
        loadMatchData() //uzupełnia firstUpdate

        winnersList() //ustawianie w spinnerze odpowiednich nazwisk ~u
        pointsList()

        //obsluga przyciskow Walkover, Retider i ScoreUnknown ~u
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
                winnersList()
            }
        }
        buttonRetired.setOnClickListener {
            if(buttonRetiredClicked){ //odznaczono "Retired"
                buttonRetiredClicked = false //zmiana na odklikniety
                binding.linearLayoutWinner.visibility = View.GONE
                //zmiana kolorow buttona
                buttonRetired.setBackgroundResource(R.drawable.border_button)
                buttonRetired.setTextColor(ContextCompat.getColor(this, R.color.general_text_color))
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
                buttonRetired.setTextColor(ContextCompat.getColor(this, R.color.white))
                //odblokowanie spinnera winner dla usera
                winner.isEnabled = true
                winnersList()
            }
        }
        buttonUnknown.setOnClickListener {
            if(buttonUnknownClicked){ //odznaczono "Unknown"
                buttonUnknownClicked = false //zmiana na odklikniety
                binding.linearLayoutWinner.visibility = View.GONE
                //zmiana kolorow buttona
                buttonUnknown.setBackgroundResource(R.drawable.border_button)
                buttonUnknown.setTextColor(ContextCompat.getColor(this, R.color.general_text_color))
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
                buttonUnknown.setTextColor(ContextCompat.getColor(this, R.color.white))
                //ustawienie wynikow na "None" i zablokowanie wyniku
                set1p1.setSelection(0); set1p1.isEnabled = false
                set1p2.setSelection(0); set1p2.isEnabled = false
                set2p1.setSelection(0); set2p1.isEnabled = false
                set2p2.setSelection(0); set2p2.isEnabled = false
                set3p1.setSelection(0); set3p1.isEnabled = false
                set3p2.setSelection(0); set3p2.isEnabled = false
                //odblokowanie spinnera winner dla usera
                winner.isEnabled = true
                winnersList()
            }
        }
        
        submitButton = binding.buttonSubmitEdit
        //w submit włączany saveMatchData
        submitButton.setOnClickListener {
            //walidacja wprowadzonego wyniku
            if(p1.text.isNullOrEmpty() || p2.text.isNullOrEmpty()){
                Toast.makeText(this, "Don't leave empty fields of players name.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Zatrzymujemy dalsze przetwarzanie
            }
            val message = checkMatchScoreAndWinner()
            if(message != "OK"){
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Zatrzymujemy dalsze przetwarzanie
            }
            saveMatchData()
            val intent = Intent(this, GenerateDrawActivity::class.java)
            intent.putExtra("tournament_id", tournamentId)
            intent.putExtra("match_number", matchNumber)
            intent.putExtra("draw_size", drawSize)
            startActivity(intent)
        }

       findMatchId(tournamentId, matchNumber){matchId ->
           if(matchId == null){
               binding.buttonDeleteMatch.visibility = View.GONE
               binding.buttonAttachMatch.text = "Attach Match"
                //attach match
               binding.buttonAttachMatch.setOnClickListener{
                   val attachMatchDialog = PlayNewOrAttachMatchDialog(this, tournamentId, matchNumber)
                   attachMatchDialog.show()
               }
           }else{
               binding.buttonDeleteMatch.visibility = View.VISIBLE
               binding.buttonAttachMatch.text = "Show Match"

               binding.buttonAttachMatch.setOnClickListener {
                   fetchMatchDate(matchId){dateInMillis->
                       val intent = Intent(this, ViewStatsActivity::class.java).apply {
                           putExtra("matchID", matchId)
                           intent.putExtra("matchDateInMillis",dateInMillis)
                       }
                       startActivity(intent)
                   }
               }
               binding.buttonDeleteMatch.setOnClickListener(){
                   val dbRef = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                       .getReference(user)
                       .child("Matches")
                       .child(matchId)

                   // region ---usuwanie id_tournament---
                   dbRef.child("id_tournament").removeValue()
                       .addOnSuccessListener {
                           Log.d("Firebase", "id_tournament został usunięty.")
                       }
                       .addOnFailureListener { e ->
                           Log.e("Firebase", "Błąd podczas usuwania id_tournament: ${e.message}")
                       }
                   //endregion

                   // region ---usuwanie match_number---
                   dbRef.child("match_number").removeValue()
                       .addOnSuccessListener {
                           Log.d("Firebase", "matchNumber został usunięty.")
                       }
                       .addOnFailureListener { e ->
                           Log.e("Firebase", "Błąd podczas usuwania matchNumber: ${e.message}")
                       }
                   //endregion
                   finish()
                   Toast.makeText(this, "Match was deleted", Toast.LENGTH_SHORT).show()
               }
           }
       }
    }

    private fun getIndexOfOption(value: String?): Int {
        val options = listOf("None", "0", "1", "2", "3", "4", "5", "6","7")
        return options.indexOf(value)
    }
    //ustawienie listy rozwijanej z punktami
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

    //pobranie wartości z bazy
    private fun loadMatchData() {
        //wczytanie wartości pól z bazy danych
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
                val winnerValue = snapshot.child("winner").getValue(String::class.java)

                // ustawienie pierwszej edycji (potrzebne dla komunikatów)
                if (player1Value == null || player1Value == "") firstUpdate["p1"] = true
                if (player2Value == null || player2Value == "") firstUpdate["p2"] = true
                if (set1p1Value == "None" || set1p1Value == null) firstUpdate["set1p1"] = true
                if (set1p2Value == "None" || set1p2Value == null) firstUpdate["set1p2"] = true
                if (set2p1Value == "None" || set2p1Value == null) firstUpdate["set2p1"] = true
                if (set2p2Value == "None" || set2p2Value == null) firstUpdate["set2p2"] = true
                if (set3p1Value == "None" || set3p1Value == null) firstUpdate["set3p1"] = true
                if (set3p2Value == "None" || set3p2Value == null) firstUpdate["set3p2"] = true
                if (winnerValue == "" || winnerValue == null) firstUpdate["winner"] = true

                // Ustawienie pól w UI
                p1.setText(player1Value)
                p2.setText(player2Value)
                winnersList() //ustawianie w spinnerze odpowiednich nazwisk
                set1p1.setSelection(getIndexOfOption(set1p1Value))
                set1p2.setSelection(getIndexOfOption(set1p2Value))
                set2p1.setSelection(getIndexOfOption(set2p1Value))
                set2p2.setSelection(getIndexOfOption(set2p2Value))
                set3p1.setSelection(getIndexOfOption(set3p1Value))
                set3p2.setSelection(getIndexOfOption(set3p2Value))
                set3p2.setSelection(getIndexOfOption(set3p2Value))
                winner.setSelection(getIndexOfWinnersOption(winnerValue))

                /*//ustawienie imion w tabeli punktów
                //komentujemy, bo tabela się rozrzuca przy długich imionach
                pN1.text = player1Value
                pN2.text = player2Value*/

                //Dodatkowo robimy kopię oryginalnych
                //(potrzebne dla komunikatów o edycji)
                originalPlayer1Value = player1Value
                originalPlayer2Value = player2Value
                originalSet1p1Value = set1p1Value
                originalSet1p2Value = set1p2Value
                originalSet2p1Value = set2p1Value
                originalSet2p2Value = set2p2Value
                originalSet3p1Value = set3p1Value
                originalSet3p2Value = set3p2Value
                originalWinnerValue = winnerValue
            }
            //ta funckja musi zwrócić firstUpdate

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error: ${error.message}")
            }
        })
    }

    //zapis w bazie
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
        //zmiana z nazwiska na "player1" lub "player2" ze spinnera Winner
        var winnerValue: String
        if(winner.selectedItem?.toString() == p1.text.toString()) winnerValue = "player1"
        else if(winner.selectedItem?.toString() == p2.text.toString()) winnerValue = "player2"
        else winnerValue = "None"

        // Sprawdzenie, czy wartości zostały zmienione, jeśli tak to zapisujemy je w Firebase
        //pole zostało zmienione, zatem trzeba sprawdzić czy to firstUpdate
        //jeżeli tak to zapisać bez wysyłania komunikatu do właściciela
        //jezeli nie to zapisać w bazie w polu z Edit i wysłać komunikat do właściciela
        if (player1Value != originalPlayer1Value && player1Value.isNotEmpty()) {
            if(firstUpdate["p1"] == true){
                database.child("player1").setValue(player1Value)
                firstUpdate["p1"]=false
            }else {
                //zapis zmienionej wartości
                database.child("player1Edit").setValue(player1Value)
                //wysłanie komunikatu
                database.child("changes").setValue(true)
                databaseT.child("changes").get().addOnSuccessListener { snapshot ->
                    val currentChanges = snapshot.getValue(Int::class.java) ?: 0
                    val updatedChanges = currentChanges + 1

                    databaseT.child("changes").setValue(updatedChanges)
                }.addOnFailureListener {
                    Log.e("Firebase", "Error getting changes", it)
                }
            }
        }
        if (player2Value != originalPlayer2Value && player2Value.isNotEmpty()) {
            if(firstUpdate["p2"] == true){
                database.child("player2").setValue(player2Value)
                firstUpdate["p2"]=false
            }else {
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
        }
        if (set1p1Value != originalSet1p1Value && set1p1Value != "None") {
            if(firstUpdate["set1p1"] == true){
                database.child("set1p1").setValue(set1p1Value)
                firstUpdate["set1p1"]=false
            }else {
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
        }
        if (set1p2Value != originalSet1p2Value && set1p2Value != "None") {
            if(firstUpdate["set1p2"] == true){
                database.child("set1p2").setValue(set1p2Value)
                firstUpdate["set1p2"]=false
            }else {
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
        }
        if (set2p1Value != originalSet2p1Value && set2p1Value != "None") {
            if(firstUpdate["set2p1"] == true){
                database.child("set2p1").setValue(set2p1Value)
                firstUpdate["set2p1"]=false
            }else {
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
        }
        if (set2p2Value != originalSet2p2Value && set2p2Value != "None") {
            if(firstUpdate["set2p2"] == true){
                database.child("set2p2").setValue(set2p2Value)
                firstUpdate["set2p2"]=false
            }else {
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
        }
        if (set3p1Value != originalSet3p1Value && set3p1Value != "None") {
            if(firstUpdate["set3p1"] == true){
                database.child("set3p1").setValue(set3p1Value)
                firstUpdate["set3p1"]=false
            }else {
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
        }
        if (set3p2Value != originalSet3p2Value && set3p2Value != "None") {
            if(firstUpdate["set3p2"] == true){
                database.child("set3p2").setValue(set3p2Value)
                firstUpdate["set3p2"]=false
            }else {
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
        }
        if (winnerValue != originalWinnerValue && winnerValue != "None") {
            if(firstUpdate["winner"]==true) {
                database.child("winner").setValue(winnerValue)
                firstUpdate["winner"] = false
            }else {
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
        }

        // Informacja o zapisaniu danych
        Log.d("Firebase", "Dane zostały zapisane!")
    }

    //wysłanie do bazy/zapisanie zmian
    private fun submitChanges() {
        databaseT.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val creator=snapshot.child("creator").getValue(String::class.java)
                val changes=snapshot.child(matchNumber).child("changes").getValue(Boolean::class.java)
                val user=FirebaseAuth.getInstance().currentUser?.uid.toString()
                if(creator==user && changes==true)
                {
                    val dialog = ChangesDialog(
                        context = this@UpdateEditMatchActivity,
                        tournamentId = tournamentId,
                        matchNumber = matchNumber
                    )
                    dialog.show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error: ${error.message}")
            }
        })
    }

    //pobranie macthId
    private fun findMatchId(
        tournamentId: String,
        matchNumber: String,
        callback: (String?) -> Unit
    ) {
        val dbref = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user)
            .child("Matches")
        var matchId: String? = null

        val query = dbref.orderByChild("id_tournament").equalTo(tournamentId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (matchSnapshot in snapshot.children) {
                    val idTournament = matchSnapshot.child("id_tournament").getValue(String::class.java)
                    val matchNum = matchSnapshot.child("match_number").getValue(String::class.java)

                    if (idTournament == tournamentId && matchNum == matchNumber) {
                        matchId = matchSnapshot.key // Pobranie klucza (matchId)
                        break // Zakończenie pętli, jeśli znaleziono
                    }
                }
                // Wywołanie callbacka z wynikiem
                callback(matchId)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Błąd podczas pobierania danych: ${error.message}")
                callback(null) // W przypadku błędu zwróć null
            }
        })
    }
    //pobranie daty z matchId
    private fun fetchMatchDate(matchId: String, callback: (Long?) -> Unit) {
        FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user)
            .child("Matches")
            .child(matchId)
            .child("data").get().addOnSuccessListener { snapshot ->
            val matchDateInMillis = snapshot.getValue(Long::class.java)
            if (matchDateInMillis != null) {
                // Zwracamy wartość date w milisekundach
                callback(matchDateInMillis)
            } else {
                // Jeśli nie udało się pobrać wartości
                callback(null)
            }
        }.addOnFailureListener { error ->
            Log.e("FirebaseError", "Błąd podczas pobierania daty: ${error.message}")
            callback(null) // W przypadku błędu, zwróć null
        }
    }



    //ustawienie spinnera do Winnera
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

    //walidacja wyniku i wyliczanie winnera z wyniku
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
                (s1p2.toInt() == 6 && s1p2.toInt() - s1p1.toInt() > 1) || (s1p2.toInt() == 7 && (s1p1.toInt() == 6 || s1p1.toInt() == 5)))){ //nie skonczyl sie pierwszy set -> inne pola musza byc "None"
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

        if(s2p1 == "None" || s2p2 == "None") return "Values of 2nd set can not be empty"
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
            if(s3p1 == "None" || s3p2 == "None") return "Values of 3rd set can not be empty"
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
