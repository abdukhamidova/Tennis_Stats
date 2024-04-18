package com.anw.tenistats

import MyAdapter
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewMatchesActivity : AppCompatActivity() {

    private lateinit var dbref : DatabaseReference
    private lateinit var matchRecyclerView: RecyclerView
    private lateinit var matchArrayList: ArrayList<MatchView>
    private lateinit var firebaseAuth: FirebaseAuth
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

        val playerView = findViewById<Button>(R.id.buttonPlayer)
        playerView.isEnabled=true
        playerView.setOnClickListener{
            startActivity(Intent(this,ViewPlayerActivity::class.java))
        }

        firebaseAuth = FirebaseAuth.getInstance()
        matchRecyclerView = findViewById(R.id.matchList)
        matchRecyclerView.layoutManager = LinearLayoutManager(this)
        matchRecyclerView.setHasFixedSize(true)

        matchArrayList = arrayListOf<MatchView>()
        adapter = MyAdapter(matchArrayList) // Inicjalizacja adaptera jako zmienna składowa klasy
        matchRecyclerView.adapter = adapter // Podpięcie adaptera do RecyclerView
        getMatchData()

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
        dbref= FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString())
            .child("Matches")

        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (matchSnapshot in snapshot.children) {
                        val match = matchSnapshot.getValue(MatchView::class.java)
                        if (match != null) {
                            matchArrayList.add(match)
                        }
                    }
                    // Poinformuj adapter o zmianach w danych
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Obsłużenie błędu zapytania do bazy danych
            }
        })
    }
}