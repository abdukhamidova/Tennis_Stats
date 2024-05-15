package com.anw.tenistats.com.anw.tenistats

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.anw.tenistats.ActivityStartPoint
import com.anw.tenistats.DeleteMatchDialogActivity
import com.anw.tenistats.R
import com.anw.tenistats.Stats
import com.anw.tenistats.ViewHistoryActivity
import com.anw.tenistats.ViewMatchesActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ResumeOrStatsDialogActivity(private val context: Context,private val openedFromStartPoint: Boolean = false) {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var alertDialog: AlertDialog
    val app = (context.applicationContext as? Stats)
    @SuppressLint("InflateParams")
    val dialogView = LayoutInflater.from(context).inflate(R.layout.resume_or_stats_dialog, null)
    val matchId: TextView = dialogView.findViewById(R.id.textViewMatchIdRoS)

    @SuppressLint("MissingInflatedId")
    fun show(milliseconds: Long){
        val dialogView = LayoutInflater.from(context).inflate(R.layout.resume_or_stats_dialog, null)
        alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()
        firebaseAuth=FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString()).child("Matches")


        val query = database.orderByChild("data").equalTo(milliseconds.toDouble())

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Pobranie pierwszego pasującego meczu
                    val matchSnapshot = dataSnapshot.children.first()

                    // Pobranie ID meczu
                    matchId.text = matchSnapshot.key ?: ""

                    if(app != null){
                        app.matchId = matchId.text.toString()
                    }
                    //ustawienie wyniku w tabelce
                    /*val player1: TextView = dialogView.findViewById<TextView>(R.id.textviewPlayer1RoS)
                    val player2: TextView = dialogView.findViewById<TextView>(R.id.textviewPlayer2RoS)
                    val serve1: TextView = dialogView.findViewById<TextView>(R.id.textViewServe1RoS)
                    val serve2: TextView = dialogView.findViewById<TextView>(R.id.textViewServe2RoS)
                    val set1p1: TextView = dialogView.findViewById<TextView>(R.id.textViewPlayer1Set1RoS)
                    val set2p1: TextView = dialogView.findViewById<TextView>(R.id.textViewPlayer1Set2RoS)
                    val set3p1: TextView = dialogView.findViewById<TextView>(R.id.textViewPlayer1Set3RoS)
                    val set1p2: TextView = dialogView.findViewById<TextView>(R.id.textViewPlayer2Set1RoS)
                    val set2p2: TextView = dialogView.findViewById<TextView>(R.id.textViewPlayer2Set2RoS)
                    val set3p2: TextView = dialogView.findViewById<TextView>(R.id.textViewPlayer2Set3RoS)
                    val pkt1: TextView = dialogView.findViewById<TextView>(R.id.textViewPlayer1PktRoS)
                    val pkt2: TextView = dialogView.findViewById<TextView>(R.id.textViewPlayer2PktRoS)*/

                    val btnResume : Button = dialogView.findViewById(R.id.buttonResumeRoS)
                    //setscore(btnResume,player1,player2,serve1,serve2,set1p1,set2p1,set3p1,set1p2,set2p2,set3p2,pkt1,pkt2)

                    val btnViewStats : Button = dialogView.findViewById(R.id.buttonViewStatsRoS)
                    val btnCancel : Button = dialogView.findViewById(R.id.buttonCancelRoS)

                    val btnDelete : ImageView = dialogView.findViewById(R.id.buttonDelete)

                    btnResume.setOnClickListener {
                        //zapisanie wyniku w zmiennych globalnych
                        /*if (app != null){
                            app.player1=player1.text.toString()
                            app.player2=player2.text.toString()
                            app.pkt1 = pkt1.text.toString()
                            app.pkt2 = pkt2.text.toString()
                            app.set1p1 = set1p1.text.toString()
                            app.set1p2 = set1p2.text.toString()
                            app.set2p1 = set2p1.text.toString()
                            app.set2p2 = set2p2.text.toString()
                            app.set3p1 = set3p1.text.toString()
                            app.set3p2 = set3p2.text.toString()
                            if(serve1.visibility == View.VISIBLE){
                                app.serve1 = "1"
                                app.serve2 = ""
                            }
                            else{
                                app.serve1 = ""
                                app.serve2 = "1"
                            }
                        }*/

                        val intent = Intent(context, ActivityStartPoint::class.java)
                        intent.putExtra("matchID", matchId.text.toString())
                        database =
                            FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                                .getReference(user.toString()).child("Current match")
                        database.setValue(matchId.text.toString())

                        //setScoreInApp()

                        context.startActivity(intent)
                        alertDialog.dismiss()
                    }

                    btnViewStats.setOnClickListener {
                        val intent = Intent(context, ViewHistoryActivity::class.java)
                        intent.putExtra("matchDateInMillis", milliseconds)
                        context.startActivity(intent)
                        alertDialog.dismiss()
                    }

                    btnDelete.setOnClickListener {
                        val deleteMatchDialog = DeleteMatchDialogActivity(context)
                        deleteMatchDialog.show(matchId.text.toString())
                        alertDialog.dismiss()
                    }

                    btnCancel.setOnClickListener {
                        val intent = Intent(context, ViewMatchesActivity::class.java)
                        intent.putExtra("matchID", matchId.text)
                        context.startActivity(intent)
                        alertDialog.dismiss()
                    }

                    } else {
                    // Obsługa, gdy dane nie istnieją w bazie danych
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Obsługa błędu zapytania do bazy danych
            }
        })
        alertDialog.show()
    }

    fun setScoreInApp()
    {
        if(app!=null) {
            val user = firebaseAuth.currentUser?.uid
            database =
                FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference(user.toString()).child("Matches").child(matchId.toString())
            database.child("player1").get().addOnSuccessListener { dataSnapshot ->
                app.player1 = dataSnapshot.child("player1").getValue(String::class.java).toString()
            }.addOnFailureListener { exception ->
                // Obsługa błędów
            }
            database.child("player2").get().addOnSuccessListener { dataSnapshot ->
                app.player2 = dataSnapshot.child("player2").getValue(String::class.java).toString()
            }.addOnFailureListener { exception ->
                // Obsługa błędów
            }
            database.child("pkt1").get().addOnSuccessListener { dataSnapshot ->
                app.pkt1 = dataSnapshot.child("pkt1").getValue(String::class.java).toString()
            }.addOnFailureListener { exception ->
                // Obsługa błędów
            }
            database.child("pkt2").get().addOnSuccessListener { dataSnapshot ->
                app.pkt2 = dataSnapshot.child("pkt2").getValue(String::class.java).toString()
            }.addOnFailureListener { exception ->
                // Obsługa błędów
            }
            database.child("set1p1").get().addOnSuccessListener { dataSnapshot ->
                app.set1p1 = dataSnapshot.child("set1p1").getValue(String::class.java).toString()
            }.addOnFailureListener { exception ->
                // Obsługa błędów
            }
            database.child("set1p2").get().addOnSuccessListener { dataSnapshot ->
                app.set1p2 = dataSnapshot.child("set1p2").getValue(String::class.java).toString()
            }.addOnFailureListener { exception ->
                // Obsługa błędów
            }
            database.child("set2p1").get().addOnSuccessListener { dataSnapshot ->
                app.set2p1 = dataSnapshot.child("set2p1").getValue(String::class.java).toString()
            }.addOnFailureListener { exception ->
                // Obsługa błędów
            }
            database.child("set2p2").get().addOnSuccessListener { dataSnapshot ->
                app.set2p2 = dataSnapshot.child("set2p2").getValue(String::class.java).toString()
            }.addOnFailureListener { exception ->
                // Obsługa błędów
            }
            database.child("set3p1").get().addOnSuccessListener { dataSnapshot ->
                app.set3p1 = dataSnapshot.child("set3p1").getValue(String::class.java).toString()
            }.addOnFailureListener { exception ->
                // Obsługa błędów
            }
            database.child("set3p2").get().addOnSuccessListener { dataSnapshot ->
                app.set3p2 = dataSnapshot.child("set3p2").getValue(String::class.java).toString()
            }.addOnFailureListener { exception ->
                // Obsługa błędów
            }
        }
    }

    fun setscore(btnResume : Button,player1: TextView,player2: TextView,serve1: TextView,serve2: TextView,set1p1: TextView,set2p1: TextView,set3p1: TextView,set1p2: TextView,set2p2: TextView,set3p2: TextView,pkt1: TextView,pkt2: TextView)
    {
        val user = FirebaseAuth.getInstance().currentUser?.uid
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString()).child("Matches").child(matchId.text.toString())

        database.child("player1").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val player1Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            player1.text = player1Value.toString()
        }.addOnFailureListener { exception ->
            // Obsługa błędów
        }

        database.child("player2").get().addOnSuccessListener { dataSnapshot ->
            // Pobranie wartości "player1" z bazy danych
            val player2Value = dataSnapshot.getValue(String::class.java)
            // Ustawienie wartości w TextView
            player2.text = player2Value.toString()
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
                btnResume.isEnabled=false
                // Pobranie wartości "player1" z bazy danych
                val winner = dataSnapshot.getValue(String::class.java)
                serve1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_laurel3, 0, 0, 0)
                serve2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_laurel3, 0, 0, 0)
                // Ustawienie wartości w TextView
                if(winner==player1.text){
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
                    if(lastserve==player1.text){
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
    }
}

