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
            firebaseAuth = FirebaseAuth.getInstance()
            val user = firebaseAuth.currentUser?.uid ?: return@setOnClickListener

            // Database reference setup
            val database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference(user).child("Matches").child(matchId)

            if (app != null) {
                val gameString = addZeros(gameId.toInt())

                // Helper function to fetch a value and update UI or set in database
                fun updateScore(setKey: String, targetKey: String, updateAppField: (String) -> Unit) {
                    database.child(setKey).get().addOnSuccessListener { snapshot ->
                        snapshot.getValue(String::class.java)?.let { value ->
                            updateAppField(value)
                            database.child("set $setId").child("game $gameString").child("score").child(targetKey).setValue(value)
                        }
                    }
                }

                // Fetching scores and updating database with the helper function
                updateScore("set1p1", "player1set1") { app.set1p1 = it }
                updateScore("set2p1", "player1set2") { app.set2p1 = it }
                updateScore("set3p1", "player1set3") { app.set3p1 = it }
                updateScore("set1p2", "player2set1") { app.set1p2 = it }
                updateScore("set2p2", "player2set2") { app.set2p2 = it }
                updateScore("set3p2", "player2set3") { app.set3p2 = it }

                // Determine the serving player
                val servePlayer = if ((app.player1 == player1.text && app.serve1 == "1") ||
                    (app.player2 == player1.text && app.serve1 != "1")
                ) app.player1 else app.player2

                // Updating scores in the database
                database.child("pktCount").get().addOnSuccessListener { dataSnapshot ->
                    val currentCount = dataSnapshot.getValue(Int::class.java) ?: 0
                    score(app, player1, serve1, serve2, pkt1txt, pkt2txt, set1p1, set1p2, set2p1, set2p2, set3p1, set3p2)

                    val scoreUpdates = mapOf(
                        "pkt1" to app.pkt1,
                        "pkt2" to app.pkt2,
                        "set1p1" to app.set1p1,
                        "set2p1" to app.set2p1,
                        "set3p1" to app.set3p1,
                        "set1p2" to app.set1p2,
                        "set2p2" to app.set2p2,
                        "set3p2" to app.set3p2,
                        "LastServePlayer" to servePlayer
                    )
                    database.updateChildren(scoreUpdates)

                    // Point entry update
                    val pointDatabase = database.child("set $setId").child("game $gameString")
                        .child("point ${addZeros(currentCount)}")
                    val point = Point(pkt1, pkt2, kto, co, gdzie, czym, app.serwis, servePlayer)
                    pointDatabase.setValue(point).addOnSuccessListener {
                        pointDatabase.child("score").updateChildren(
                            mapOf(
                                "servePlayer" to servePlayer,
                                "pkt1" to app.pkt1,
                                "pkt2" to app.pkt2,
                                "set1p1" to app.set1p1,
                                "set1p2" to app.set1p2,
                                "set2p1" to app.set2p1,
                                "set2p2" to app.set2p2,
                                "set3p1" to app.set3p1,
                                "set3p2" to app.set3p2
                            )
                        )
                    }

                    // Incrementing the point count
                    database.child("pktCount").setValue(currentCount + 1)

                    // Check if the match has ended
                    if (!app.isEnd) {
                        if (!openedFromStartPoint) {
                            val intent = Intent(context, ActivityStartPoint::class.java).apply {
                                putExtra("matchID", matchId)
                            }
                            context.startActivity(intent)
                            (context as Activity).finish()
                        }
                    } else {
                        val intent = Intent(context, EndOfMatchActivity::class.java).apply {
                            putExtra("matchID", matchId)
                        }
                        context.startActivity(intent)
                        (context as Activity).finish()
                    }
                }

                app.serwis = 1
                alertDialog.dismiss()
            }
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
            resultString = "00$a"
        }
        else if(a<100) {
            resultString = "0$a"
        }else resultString = a.toString()
        return resultString
    }
}