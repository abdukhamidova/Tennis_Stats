package com.anw.tenistats.com.anw.tenistats

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.anw.tenistats.ActivityStartPoint
import com.anw.tenistats.EndOfMatchActivity
import com.anw.tenistats.Point
import com.anw.tenistats.R
import com.anw.tenistats.Stats
import com.anw.tenistats.score
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue

class AddPointDialog(private val context: Context, private val openedFromStartPoint: Boolean = false) {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var alertDialog: AlertDialog
    val app = (context.applicationContext as? Stats)

    fun show(player1: TextView,player2: TextView,serve1: TextView,serve2: TextView,pkt1txt: TextView,
             pkt2txt: TextView,set1p1: TextView,set1p2: TextView,set2p1: TextView,set2p2: TextView,
             set3p1: TextView, set3p2:TextView, pkt1: String, pkt2: String, kto: String, co: String,
             gdzie: String, czym: String, matchId: String, gameId: String, setId: String){
        //przygotowanie zmiennych
        val dialogView = LayoutInflater.from(context).inflate(R.layout.add_dialog, null)
        alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()
        val collectedPoint = dialogView.findViewById<TextView>(R.id.textViewPointData)
        val btnAdd : Button = dialogView.findViewById(R.id.buttonAdd)
        val btnCancel : Button = dialogView.findViewById(R.id.buttonCancel)

        collectedPoint.text = "Player: $kto\n\n$co\n\n$gdzie\n\n$czym"

        btnAdd.setOnClickListener {
            // Dodaj punkt do bazy danych
            firebaseAuth=FirebaseAuth.getInstance()
            val user = firebaseAuth.currentUser?.uid
            database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(user.toString()).child("Matches").child(matchId)

            if (app != null) {
                //ustawienie wyniku w danym gemie
                var set1P1: String
                var set1P2: String
                var set2P1: String
                var set2P2: String
                var set3P1: String
                var set3P2: String

                database.child("set1p1").get().addOnSuccessListener { dataSnapshot ->
                    // Pobranie wartości "player1" z bazy danych
                    val set1p1Value = dataSnapshot.getValue(String::class.java).toString()
                    // Ustawienie wartości w TextView
                    set1P1 = set1p1Value
                    app.set1p1=set1P1
                    database.child(("set "+ setId)).child(("game "+gameId)).child("score").child("player1set1").setValue(set1P1)
                }.addOnFailureListener { exception ->
                    // Obsługa błędów
                }
                database.child("set2p1").get().addOnSuccessListener { dataSnapshot ->
                    // Pobranie wartości "player1" z bazy danych
                    val set2p1Value = dataSnapshot.getValue(String::class.java).toString()
                    // Ustawienie wartości w TextView
                    set2P1 = set2p1Value
                    app.set2p1=set2P1
                    database.child(("set "+ setId)).child(("game "+gameId)).child("score").child("player1set2").setValue(set2P1)
                }.addOnFailureListener { exception ->
                    // Obsługa błędów
                }
                database.child("set3p1").get().addOnSuccessListener { dataSnapshot ->
                    // Pobranie wartości "player1" z bazy danych
                    val set3p1Value = dataSnapshot.getValue(String::class.java).toString()
                    // Ustawienie wartości w TextView
                    set3P1 = set3p1Value
                    app.set3p1=set3P1
                    database.child(("set "+ setId)).child(("game "+gameId)).child("score").child("player1set3").setValue(set3P1)
                }.addOnFailureListener { exception ->
                    // Obsługa błędów
                }
                database.child("set1p2").get().addOnSuccessListener { dataSnapshot ->
                    // Pobranie wartości "player1" z bazy danych
                    val set1p2Value = dataSnapshot.getValue(String::class.java).toString()
                    // Ustawienie wartości w TextView
                    set1P2 = set1p2Value
                    app.set1p2=set1P2
                    database.child(("set "+ setId)).child(("game "+gameId)).child("score").child("player2set1").setValue(set1P2)
                }.addOnFailureListener { exception ->
                    // Obsługa błędów
                }
                database.child("set2p2").get().addOnSuccessListener { dataSnapshot ->
                    // Pobranie wartości "player1" z bazy danych
                    val set2p2Value = dataSnapshot.getValue(String::class.java).toString()
                    // Ustawienie wartości w TextView
                    set2P2 = set2p2Value
                    app.set2p2=set2P2
                    database.child(("set "+ setId)).child(("game "+gameId)).child("score").child("player2set2").setValue(set2P2)
                }.addOnFailureListener { exception ->
                    // Obsługa błędów
                }
                database.child("set3p2").get().addOnSuccessListener { dataSnapshot ->
                    // Pobranie wartości "player1" z bazy danych
                    val set3p2Value = dataSnapshot.getValue(String::class.java).toString()
                    // Ustawienie wartości w TextView
                    set3P2 = set3p2Value
                    app.set3p2=set3P2
                    database.child(("set "+ setId)).child(("game "+gameId)).child("score").child("player2set3").setValue(set3P2)
                }.addOnFailureListener { exception ->
                    // Obsługa błędów
                }

                /*database.child(("set "+ setId)).child(("game "+gameId)).child("score").child("player1set1").setValue(set1P1)
                database.child(("set "+ setId)).child(("game "+gameId)).child("score").child("player2set1").setValue(set1P2)
                database.child(("set "+ setId)).child(("game "+gameId)).child("score").child("player1set2").setValue(set2P1)
                database.child(("set "+ setId)).child(("game "+gameId)).child("score").child("player2set2").setValue(set2P2)
                database.child(("set "+ setId)).child(("game "+gameId)).child("score").child("player1set3").setValue(set3P1)
                database.child(("set "+ setId)).child(("game "+gameId)).child("score").child("player2set3").setValue(set3P2)*/

                //ustawienie osoby serwujacej w danym gemie
                val servePlayer: String
                servePlayer = if(app.serve1=="1"){
                    app.player1
                } else{
                    app.player2
                }

                database.child("pktCount").get().addOnSuccessListener { dataSnapshot ->
                    if (dataSnapshot.exists()) {
                        score(app,player1,serve1,serve2,pkt1txt,pkt2txt,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                            .getReference(user.toString()).child("Matches").child(matchId)

                        database.child("pkt1").setValue(app.pkt1)
                        database.child("pkt2").setValue(app.pkt2)
                        database.child("set1p1").setValue(app.set1p1)
                        database.child("set2p1").setValue(app.set2p1)
                        database.child("set3p1").setValue(app.set3p1)
                        database.child("set1p2").setValue(app.set1p2)
                        database.child("set2p2").setValue(app.set2p2)
                        database.child("set3p2").setValue(app.set3p2)
                        //ustawienie osoby serwujacej aktualnie (potrzebne do wznowienia meczu)
                        var lastServePlayer: String
                        if(app.serve1=="1"){
                            database.child("LastServePlayer").setValue(app.player1)
                            lastServePlayer=app.player1
                        }
                        else{
                            database.child("LastServePlayer").setValue(app.player2)
                            lastServePlayer=app.player2
                        }

                        val currentCount = dataSnapshot.getValue(Int::class.java) ?: 0
                        val count = addZeros(currentCount)
                        /*if(currentCount<10){
                            count = "point 0" + currentCount.toString()
                        }
                        else{
                            count = "point " + currentCount.toString()
                        }*/

                        // Zapis punktu do bazy danych
                        val pointDatabase =
                            database.child(("set " + setId)).child(("game " + gameId))
                                .child("point $count")
                        val point = Point(
                            pkt1, pkt2, kto,
                            co, gdzie, czym, app.serwis, servePlayer
                        )
                        pointDatabase.setValue(point)
                        pointDatabase.child("score").child("servePlayer").setValue(lastServePlayer)
                        pointDatabase.child("score").child("pkt1").setValue(app.pkt1)
                        pointDatabase.child("score").child("pkt2").setValue(app.pkt2)
                        pointDatabase.child("score").child("set1p1")
                            .setValue(app.set1p1)//.text.toString())
                        pointDatabase.child("score").child("set1p2")
                            .setValue(app.set1p2)//.text.toString())
                        pointDatabase.child("score").child("set2p1")
                            .setValue(app.set2p1)//.text.toString())
                        pointDatabase.child("score").child("set2p2")
                            .setValue(app.set2p2)//.text.toString())
                        pointDatabase.child("score").child("set3p1")
                            .setValue(app.set3p1)//.text.toString())
                        pointDatabase.child("score").child("set3p2")
                            .setValue(app.set3p2)//.text.toString())

                        // Inkrementacja i zapisanie wartości pktCount
                        val newCount = currentCount + 1
                        database.child("pktCount").setValue(newCount)
                    }
                }
                app.serwis=1

                if(!app.isEnd){
                    if(!openedFromStartPoint) {
                        val intent = Intent(context, ActivityStartPoint::class.java)
                        intent.putExtra("matchID", matchId)
                        (context as Activity).startActivity(intent)
                        context.finish()

                    }
                }else{
                    val intent = Intent(context, EndOfMatchActivity::class.java)
                    intent.putExtra("matchID", matchId)
                    (context as Activity).startActivity(intent)
                    context.finish()

                }
            }
            alertDialog.dismiss()
        }

        btnCancel.setOnClickListener {
            if(!openedFromStartPoint) {
                val intent = Intent(context, ActivityStartPoint::class.java)
                intent.putExtra("matchID", matchId)
                (context as Activity).startActivity(intent)
                context.finish()
            }
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun addZeros(a: Int) : String
    {
        var resultString: String
        if(a<10){
            //"00$a" jezeli dodane 100
            resultString = "0$a"
        }else resultString = a.toString()
        /*else if(a<100) {
            resultString = "0$a"
        }*/
        return resultString
    }
}