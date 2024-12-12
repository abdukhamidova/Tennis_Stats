package com.anw.tenistats.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import com.anw.tenistats.R
import com.anw.tenistats.stats.ViewStatsActivity
import com.anw.tenistats.tournament.AddRoundMatchActivity
import com.anw.tenistats.tournament.UpdateEditMatchActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DecisionAttachMatchDialog (
    private val context: Context,
    private val tournamentID : String?,
    private val matchNumber : String?) {
        private lateinit var alertDialog: AlertDialog

        fun show(dateInMillis: Long) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_decision_attach_match, null)
            alertDialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

            val btnCancel: Button = dialogView.findViewById(R.id.buttonCancelAttachment)
            val btnAttach: Button = dialogView.findViewById(R.id.buttonAttach)
            btnCancel.setOnClickListener {
                alertDialog.dismiss()
            }

            btnAttach.setOnClickListener {
                val user = FirebaseAuth.getInstance().currentUser?.uid
                val dbref = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference(user.toString())
                    .child("Matches")

                var matchId: String? = null // Deklaracja zmiennej
                val query = dbref.orderByChild("data").equalTo(dateInMillis.toDouble())
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var matchFound = false
                        if (dataSnapshot.exists()) {
                            // Pobranie pierwszego pasującego meczu
                            val matchSnapshot = dataSnapshot.children.first()

                            // Pobranie ID meczu
                            matchId = matchSnapshot.key ?: ""
                            matchFound = true

                            // Dodajemy tournamentId i matchNumber do tego meczu
                            matchId?.let {
                                dbref.child(it).child("id_tournament").setValue(tournamentID)
                                dbref.child(it).child("match_number").setValue(matchNumber)
                            }

                            Log.d("Firebase", "Mecz zaktualizowany z tournamentId i matchNumber.")
                        }
                        if (!matchFound) {
                            Log.d("Firebase", "Nie znaleziono meczu z podaną datą.")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Błąd zapytania: ${error.message}")
                    }
                })
               /* val intent = Intent(context, UpdateEditMatchActivity::class.java).apply {
                    putExtra("tournamentId", tournamentID)
                    putExtra("matchNumber", matchNumber)
                }
                context.startActivity(intent)*/
                alertDialog.dismiss()
                if(context is AddRoundMatchActivity) context.finish()
            }
            alertDialog.show()
        }
}