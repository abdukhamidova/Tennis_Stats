package com.anw.tenistats

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        var matchId = intent.getStringExtra("matchID")

        val app = application as Stats

        //podsumowanie co zaznaczylismy
        findViewById<TextView>(R.id.textViewAdd).text = "Player: " + intent.getStringExtra("Kto").toString() + "\n" + intent.getStringExtra("Co").toString() + "\n" + intent.getStringExtra("Gdzie").toString() + "\n" + intent.getStringExtra("Czym").toString()

        //zaakceptowanie podsumowania
        findViewById<Button>(R.id.buttonAccept).setOnClickListener {
            //adres meczu
            database =
                FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference(user.toString()).child("Matches").child(matchId.toString()).child(("set "+ intent.getStringExtra("setID"))).child(("game "+intent.getStringExtra("gameID"))).child(("point "+app.pktId))

            val point =  Point(
                intent.getStringExtra("Pkt1").toString(),
                intent.getStringExtra("Pkt2").toString(),
                intent.getStringExtra("Kto").toString(),
                intent.getStringExtra("Co").toString(),
                intent.getStringExtra("Gdzie").toString(),
                intent.getStringExtra("Czym").toString(),
                app.serwis
            )

            database.setValue(point)
            app.pktId++
            app.serwis=1
            if(!app.isEnd){
                val intent= Intent(this,ActivityStartPoint::class.java).also {
                    it.putExtra("matchID",matchId)
                    startActivity(it)
                }
            }
            else{
                val intent=Intent(this,EndOfMatchActivity::class.java).also{
                    it.putExtra("matchID",matchId)
                    startActivity(it)
                }
            }
        }
    }
}