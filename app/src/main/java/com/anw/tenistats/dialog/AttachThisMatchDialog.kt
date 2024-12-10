package com.anw.tenistats.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import com.anw.tenistats.R
import com.anw.tenistats.matchplay.StartNewActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AttachThisMatchDialog(
    private val context: Context,
    private val dateInMillis: String) {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var alertDialog: AlertDialog

    fun show() {
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
            //spróbować pobrać id tournament i matchNumber z contextu
            //wtedy usunąć "" z viewMatches


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
                        /*matchId?.let {
                            dbref.child(it).child("id_tournament").setValue(tournamentId)
                            dbref.child(it).child("match_number").setValue(matchNumber)
                        }*/

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
            alertDialog.dismiss()
        }
        alertDialog.show()
    }
}