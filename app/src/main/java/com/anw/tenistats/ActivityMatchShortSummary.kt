package com.anw.tenistats

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.anw.tenistats.databinding.ActivityMatchShortSummaryBinding
import com.anw.tenistats.mainpage.NavigationDrawerHelper
import com.anw.tenistats.matchplay.getGoldenDrawable
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ActivityMatchShortSummary : AppCompatActivity() {
    private lateinit var binding: ActivityMatchShortSummaryBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private var matchId = ""
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMatchShortSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //MENU
        firebaseAuth = FirebaseAuth.getInstance()
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.navigationViewMenu)
        val headerView = navigationView.getHeaderView(0)
        navigationDrawerHelper = NavigationDrawerHelper(this)
        navigationDrawerHelper.setupNavigationDrawer(drawerLayout, navigationView, firebaseAuth)


        val userEmail = firebaseAuth.currentUser?.email.toString()
        val userEmailView = headerView.findViewById<TextView>(R.id.textViewUserEmail)
        if (userEmail.isNotEmpty()) {
            userEmailView.text = userEmail
        } else {
            userEmailView.text = resources.getString(R.string.user_email)
        }
        //MENU

        // Odbierz datę meczu w formacie milisekund z poprzedniej aktywności
        val matchDateInMillis = intent.getLongExtra("matchDateInMillis", 0L)

        // Pobierz mecz na podstawie daty z bazy danych
        //fetchMatchByDate(matchDateInMillis)

    }

/*    private fun fetchMatchByDate(matchDateInMillis: Long) {
        val user = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString()).child("Matches")

        // Query w bazie danych Firebase do znalezienia meczu o podanej dacie
        val query = database.orderByChild("data").equalTo(matchDateInMillis.toDouble())

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Pobranie pierwszego pasującego meczu
                    val matchSnapshot = dataSnapshot.children.first()

                    // Pobranie ID meczu
                    var matchId = matchSnapshot.key ?: ""

                    // Sprawdzenie czy istnieje trzeci set
                    val thirdSetExists = matchSnapshot.child("set 3").exists()

                    // Pobierz punkty dla tego meczu

                    val buttonSet1 = findViewById<Button>(R.id.buttonSet1His)
                    val buttonSet2 = findViewById<Button>(R.id.buttonSet2His)
                    val buttonSet3 = findViewById<Button>(R.id.buttonSet3His)
                    val buttonAll = findViewById<Button>(R.id.buttonAllHis)

                    val player1 = findViewById<TextView>(R.id.textviewPlayer1His)
                    val player2 = findViewById<TextView>(R.id.textviewPlayer2His)
                    val serve1 = findViewById<TextView>(R.id.textViewServe1His)
                    val serve2 = findViewById<TextView>(R.id.textViewServe2His)
                    val set1p1 = findViewById<TextView>(R.id.textViewP1Set1His)
                    val set2p1 = findViewById<TextView>(R.id.textViewP1Set2His)
                    val set3p1 = findViewById<TextView>(R.id.textViewP1Set3His)
                    val set1p2 = findViewById<TextView>(R.id.textViewP2Set1His)
                    val set2p2 = findViewById<TextView>(R.id.textViewP2Set2His)
                    val set3p2 = findViewById<TextView>(R.id.textViewP2Set3His)
                    val pkt1 = findViewById<TextView>(R.id.textViewPlayer1PktHis)
                    val pkt2 = findViewById<TextView>(R.id.textViewPlayer2PktHis)

                    buttonAll.setOnClickListener {
                        buttonAll.setBackgroundResource(R.drawable.rectangle_button)
                        buttonAll.setTextColor(
                            ContextCompat.getColor(this@ViewHistoryActivity,
                            R.color.white
                        ))
                        buttonSet1.setBackgroundResource(R.drawable.rec_btn_not_selected)
                        buttonSet1.setTextColor(
                            ContextCompat.getColor(this@ViewHistoryActivity,
                            R.color.general_text_color
                        ))
                        buttonSet2.setBackgroundResource(R.drawable.rec_btn_not_selected)
                        buttonSet2.setTextColor(
                            ContextCompat.getColor(this@ViewHistoryActivity,
                            R.color.general_text_color
                        ))
                        buttonSet3.setBackgroundResource(R.drawable.rec_btn_not_selected)
                        buttonSet3.setTextColor(
                            ContextCompat.getColor(this@ViewHistoryActivity,
                            R.color.general_text_color
                        ))
                        fetchMatchPoints(matchId)

                    }
                    buttonSet1.setOnClickListener {
                        buttonAll.setBackgroundResource(R.drawable.rec_btn_not_selected)
                        buttonAll.setTextColor(
                            ContextCompat.getColor(this@ViewHistoryActivity,
                            R.color.general_text_color
                        ))
                        buttonSet1.setBackgroundResource(R.drawable.rectangle_button)
                        buttonSet1.setTextColor(
                            ContextCompat.getColor(this@ViewHistoryActivity,
                            R.color.white
                        ))
                        buttonSet2.setBackgroundResource(R.drawable.rec_btn_not_selected)
                        buttonSet2.setTextColor(
                            ContextCompat.getColor(this@ViewHistoryActivity,
                            R.color.general_text_color
                        ))
                        buttonSet3.setBackgroundResource(R.drawable.rec_btn_not_selected)
                        buttonSet3.setTextColor(
                            ContextCompat.getColor(this@ViewHistoryActivity,
                            R.color.general_text_color
                        ))
                        // Wywołanie funkcji fetchMatchPoints tylko dla seta 1
                        fetchMatchPoints(matchId, "set 1")
                    }

                    buttonSet2.setOnClickListener {
                        if (set2p1.text != "") {
                            buttonAll.setBackgroundResource(R.drawable.rec_btn_not_selected)
                            buttonAll.setTextColor(
                                ContextCompat.getColor(this@ViewHistoryActivity,
                                R.color.general_text_color
                            ))
                            buttonSet1.setBackgroundResource(R.drawable.rec_btn_not_selected)
                            buttonSet1.setTextColor(
                                ContextCompat.getColor(this@ViewHistoryActivity,
                                R.color.general_text_color
                            ))
                            buttonSet2.setBackgroundResource(R.drawable.rectangle_button)
                            buttonSet2.setTextColor(
                                ContextCompat.getColor(this@ViewHistoryActivity,
                                R.color.white
                            ))
                            buttonSet3.setBackgroundResource(R.drawable.rec_btn_not_selected)
                            buttonSet3.setTextColor(
                                ContextCompat.getColor(this@ViewHistoryActivity,
                                R.color.general_text_color
                            ))
                            // Wywołanie funkcji fetchMatchPoints tylko dla seta 2
                            fetchMatchPoints(matchId, "set 2")
                        }else{
                            Toast.makeText(this@ViewHistoryActivity, "Set2 does not exist", Toast.LENGTH_SHORT).show()
                        }
                    }
                    //zrobilam zamiast ukrywania przyciku sprawdzenie czy dany set istnieje i zmienilam przycisk danego seta na nieaktywny ~u
                    // Ukryj lub pokaż przycisk dla trzeciego seta w zależności od jego istnienia
                    //if (thirdSetExists) {
                    //  buttonSet3.visibility = View.VISIBLE
                    buttonSet3.setOnClickListener {
                        if (set3p1.text != "") {
                            buttonAll.setBackgroundResource(R.drawable.rec_btn_not_selected)
                            buttonAll.setTextColor(
                                ContextCompat.getColor(this@ViewHistoryActivity,
                                R.color.general_text_color
                            ))
                            buttonSet1.setBackgroundResource(R.drawable.rec_btn_not_selected)
                            buttonSet1.setTextColor(
                                ContextCompat.getColor(this@ViewHistoryActivity,
                                R.color.general_text_color
                            ))
                            buttonSet2.setBackgroundResource(R.drawable.rec_btn_not_selected)
                            buttonSet2.setTextColor(
                                ContextCompat.getColor(this@ViewHistoryActivity,
                                R.color.general_text_color
                            ))
                            buttonSet3.setBackgroundResource(R.drawable.rectangle_button)
                            buttonSet3.setTextColor(
                                ContextCompat.getColor(this@ViewHistoryActivity,
                                R.color.white
                            ))
                            // Wywołanie funkcji fetchMatchPoints tylko dla seta 3
                            fetchMatchPoints(matchId, "set 3")
                        }else{
                            Toast.makeText(this@ViewHistoryActivity, "Set3 does not exist", Toast.LENGTH_SHORT).show()
                        }
                    }
                    //} else {
                    //textViewSet3.visibility = View.GONE
                    //  buttonSet3.isClickable=false
                    //}

                    setscore(player1,player2,serve1,serve2,set1p1,set2p1,set3p1,set1p2,set2p2,set3p2,pkt1,pkt2)
                } else {
                    // Obsługa, gdy dane nie istnieją w bazie danych
                    Toast.makeText(this@ViewHistoryActivity, "Nie znaleziono danych", Toast.LENGTH_SHORT).show()
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Obsługa błędu zapytania do bazy danych
                Toast.makeText(this@ViewHistoryActivity, "Błąd zapytania do bazy danych", Toast.LENGTH_SHORT).show()
            }
        })
    }
    fun setscore(player1: TextView,player2: TextView,serve1: TextView,serve2: TextView,set1p1: TextView,set2p1: TextView,set3p1: TextView,set1p2: TextView,set2p2: TextView,set3p2: TextView,pkt1: TextView,pkt2: TextView)
    {
        val user = FirebaseAuth.getInstance().currentUser?.uid
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString()).child("Matches").child(matchId)
        database.child("player1").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val player1Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            player1.text = player1Value
            pl1 = player1Value.toString()
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }

        database.child("player2").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val player2Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            player2.text = player2Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }

        database.child("set1p1").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val set1p1Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            set1p1.text = set1p1Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("set2p1").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val set2p1Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            set2p1.text = set2p1Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("set3p1").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val set3p1Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            set3p1.text = set3p1Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("set1p2").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val set1p2Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            set1p2.text = set1p2Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("set2p2").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val set2p2Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            set2p2.text = set2p2Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("set3p2").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val set3p2Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            set3p2.text = set3p2Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("pkt1").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val pkt1Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            pkt1.text = pkt1Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("pkt2").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val pkt2Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            pkt2.text = pkt2Value
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
        database.child("winner").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            if(dataSnapshot.exists()){
                // Pobranie wartości "player1" z bazy danych
                val winner = dataSnapshot.getValue(String::class.java)
                val goldenLaurel = getGoldenDrawable(applicationContext, R.drawable.icon_laurel3)
                serve1.setCompoundDrawablesWithIntrinsicBounds(goldenLaurel, null, null, null)
                serve2.setCompoundDrawablesWithIntrinsicBounds(goldenLaurel, null, null, null)

                // Ustawienie wartości w TextView
                if(winner==pl1){
                    serve1.visibility = View.VISIBLE
                    serve2.visibility = View.INVISIBLE
                }
                else{
                    serve1.visibility = View.INVISIBLE
                    serve2.visibility = View.VISIBLE
                }
            }
            else{
                database.child("LastServePlayer").get().addOnSuccessListener { dataSnapshot ->
                    // Pobranie wartości "player1" z bazy danych
                    val lastserve = dataSnapshot.getValue(String::class.java)
                    serve1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ball, 0, 0, 0)
                    serve2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ball, 0, 0, 0)
                    // Ustawienie wartości w TextView
                    if(lastserve==pl1){
                        serve1.visibility = View.VISIBLE
                        serve2.visibility = View.INVISIBLE
                    }
                    else{
                        serve1.visibility = View.INVISIBLE
                        serve2.visibility = View.VISIBLE
                    }
                }.addOnFailureListener { exception ->
                    // Obsługa błędów
                }
            }
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }
    }*/
}