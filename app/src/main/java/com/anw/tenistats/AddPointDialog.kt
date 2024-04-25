package com.anw.tenistats.com.anw.tenistats

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import com.anw.tenistats.ActivityStartPoint
import com.anw.tenistats.EndOfMatchActivity
import com.anw.tenistats.Point
import com.anw.tenistats.R
import com.anw.tenistats.Stats
import com.anw.tenistats.score
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddPointDialog(private val context: Context, private val openedFromStartPoint: Boolean = false) {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var alertDialog: AlertDialog
    val app = (context.applicationContext as? Stats)

    fun show(player1: TextView,player2: TextView,serve1: TextView,serve2: TextView,pkt1txt: TextView,pkt2txt: TextView,set1p1: TextView,set1p2: TextView,set2p1: TextView,set2p2: TextView,set3p1: TextView, set3p2:TextView, pkt1: String, pkt2: String, kto: String, co: String, gdzie: String, czym: String, matchId: String, gameId: String, setId: String){
        //przygotowanie zmiennych
        val dialogView = LayoutInflater.from(context).inflate(R.layout.add_dialog, null)
        alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()
        val collectedPoint : TextView = dialogView.findViewById(R.id.textViewPointData)
        val btnAdd : Button = dialogView.findViewById(R.id.buttonAdd)
        val btnCancel : Button = dialogView.findViewById(R.id.buttonCancel)

        collectedPoint.text = "Player: $kto\n$co\n$gdzie\n$czym"

        btnAdd.setOnClickListener {
            // Dodaj punkt do bazy danych
            firebaseAuth=FirebaseAuth.getInstance()
            val user = firebaseAuth.currentUser?.uid

            if (app != null) {
                score(app,player1,player2,serve1,serve2,pkt1txt,pkt2txt,set1p1,set1p2,set2p1,set2p2,set3p1,set3p2)
                database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference(user.toString()).child("Matches").child(matchId).child(("set "+ setId)).child(("game "+gameId)).child(("point "+app.pktId))

                val point =  Point(
                    pkt1, pkt2, kto,
                    co, gdzie, czym, app.serwis
                )
                database.setValue(point)
                app.pktId++
                app.serwis=1

                if(!app.isEnd){
                    if(!openedFromStartPoint) {
                        val intent = Intent(context, ActivityStartPoint::class.java)
                        intent.putExtra("matchID", matchId)
                        context.startActivity(intent)
                    }
                }else{
                    val intent = Intent(context, EndOfMatchActivity::class.java)
                    intent.putExtra("matchID", matchId)
                    context.startActivity(intent)
                }
            }
            alertDialog.dismiss()
        }

        btnCancel.setOnClickListener {
            val intent = Intent(context, ActivityStartPoint::class.java)
            intent.putExtra("matchID", matchId)
            context.startActivity(intent)
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

}

