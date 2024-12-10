package com.anw.tenistats.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat.startActivity
import com.anw.tenistats.R
import com.anw.tenistats.tournament.AddPlayersToTournamentActivity
import com.anw.tenistats.tournament.GenerateDrawActivity
import com.anw.tenistats.tournament.TournamentDetailsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TournamentDialog(private val context: Context, private val openedFromStartPoint: Boolean = false) {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var alertDialog: AlertDialog

    fun show(tournamentId: String?){
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_tournament, null)
        alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()
        firebaseAuth=FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("Tournaments").child(tournamentId!!)

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("MissingInflatedId")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val btnShowDraw : Button = dialogView.findViewById(R.id.buttonShowDraw)
                val btnAddPlayers : Button = dialogView.findViewById(R.id.buttonAddPlayers)
                val btnShowDetails : Button = dialogView.findViewById(R.id.buttonShowDetails)
                val btnCancel : Button = dialogView.findViewById(R.id.buttonCancel)
                val btnDelete : ImageView = dialogView.findViewById(R.id.buttonDelete)

                if(dataSnapshot.child("creator").value != user.toString()){
                    btnDelete.visibility = View.GONE
                }
                else{
                    btnDelete.visibility = View.VISIBLE
                }

                btnShowDraw.setOnClickListener {
                    val drawSize = dataSnapshot.child("drawSize").value.toString()
                    val intent = Intent(context, GenerateDrawActivity::class.java)
                    intent.putExtra("tournament_id", tournamentId)
                    intent.putExtra("draw_size", drawSize)
                    context.startActivity(intent)
                    alertDialog.dismiss()
                }

                btnAddPlayers.setOnClickListener {
                    val intent = Intent(context,AddPlayersToTournamentActivity::class.java)  //należy zmienić przejście do Stats
                    intent.putExtra("tournament_id", tournamentId)
                    context.startActivity(intent)
                }

                btnShowDetails.setOnClickListener {
                    val intent = Intent(context, TournamentDetailsActivity::class.java)
                    intent.putExtra("tournament_id", tournamentId)
                    context.startActivity(intent)
                    alertDialog.dismiss()
                }
                btnDelete.setOnClickListener {
                    val deleteTournamentDialog = DeleteTournamentDialog(context)
                    deleteTournamentDialog.show(tournamentId)
                    alertDialog.dismiss()
                }

                btnCancel.setOnClickListener {
                    alertDialog.dismiss()
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Obsługa błędu zapytania do bazy danych
            }
        })
        alertDialog.show()
    }
}