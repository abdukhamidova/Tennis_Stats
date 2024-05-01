package com.anw.tenistats

import MyAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anw.tenistats.ui.theme.NavigationDrawerHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewMatchesActivity : AppCompatActivity(), MyAdapter.OnItemClickListener {

    private lateinit var dbref : DatabaseReference
    private lateinit var matchRecyclerView: RecyclerView
    private lateinit var matchArrayList: ArrayList<MatchView>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navigationDrawerHelper: NavigationDrawerHelper
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var adapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_matches)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val app = application as Stats
        app.matchId = ""

        firebaseAuth = FirebaseAuth.getInstance()

        //------------ MENU
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val menu = findViewById<ImageButton>(R.id.buttonMenu)
        val navigationView = findViewById<NavigationView>(R.id.navigationViewMenu)
        val headerView = navigationView.getHeaderView(0)

        menu.setOnClickListener{
            drawerLayout.open()
        }
        navigationDrawerHelper = NavigationDrawerHelper(this)
        navigationDrawerHelper.setupNavigationDrawer(drawerLayout, navigationView, firebaseAuth)
        val backButton = findViewById<ImageButton>(R.id.buttonReturnUndo)
        backButton.setOnClickListener{
            startActivity(Intent(this, ActivityMenu::class.java))
        }

        val userEmail = FirebaseAuth.getInstance().currentUser?.email.toString()
        if(userEmail.isNotEmpty()) {
            headerView.findViewById<TextView>(R.id.textViewUserEmail).text = userEmail
        }
        else {
            findViewById<TextView>(R.id.textViewUserEmail).text = resources.getString(R.string.user_email)
        }
        //------------ MENU

        matchRecyclerView = findViewById(R.id.matchList)
        matchRecyclerView.layoutManager = LinearLayoutManager(this)
        matchRecyclerView.setHasFixedSize(true)

        matchArrayList = arrayListOf<MatchView>()
        val adapter = MyAdapter(matchArrayList, firebaseAuth)
        // Inicjalizacja adaptera jako zmienna składowa klasy
        matchRecyclerView.adapter = adapter // Podpięcie adaptera do RecyclerView
        getMatchData()

        matchRecyclerView.isEnabled = true

        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filtruj dane w adapterze na podstawie wprowadzonego tekstu
                adapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun onTextClicked(view: View) {
        // Obsługa kliknięcia w tekście
        Toast.makeText(this, "Tekst został kliknięty!", Toast.LENGTH_SHORT).show()
    }

    private fun getMatchData() {
        val user = firebaseAuth.currentUser?.uid
        dbref = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString())
            .child("Matches")

        dbref.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (matchSnapshot in snapshot.children) {
                        val match = matchSnapshot.getValue(MatchView::class.java)
                        if (match != null) {
                            findViewById<TextView>(R.id.textViewNotFound).visibility = View.INVISIBLE
                            matchArrayList.add(match)
                        }else{
                            findViewById<TextView>(R.id.textViewNotFound).visibility = View.VISIBLE
                        }
                    }
                    // Inicjalizacja adaptera
                    adapter = MyAdapter(matchArrayList, firebaseAuth)
                    // Podpięcie adaptera do RecyclerView
                    matchRecyclerView.adapter = adapter
                    // Poinformuj adapter o zmianach w danych
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsłużenie błędu zapytania do bazy danych
            }
        })
    }
    override fun onItemClick(matchView: MatchView) {
        // Tu umieść logikę obsługi kliknięcia na element listy
        // Na przykład, możesz uruchomić nową aktywność lub wykonać inne czynności
        val intent = Intent(this, ViewHistoryActivity::class.java)
        startActivity(intent)
    }
}
