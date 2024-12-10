package com.anw.tenistats.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.Button
import com.anw.tenistats.R
import com.anw.tenistats.matchplay.StartNewActivity
import com.anw.tenistats.tournament.AddRoundMatchActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class PlayNewOrAttachMatchDialog(
    private val context: Context,
    private val tournamentId: String,
    private val matchNumber: String) {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var alertDialog: AlertDialog

    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_attach_match, null)
        alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val btnCancel: Button = dialogView.findViewById(R.id.buttonCancel)
        val btnAdd: Button = dialogView.findViewById(R.id.buttonAdd)
        val btnPlay: Button = dialogView.findViewById(R.id.buttonPlay)
        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnPlay.setOnClickListener {
            val intent = Intent(context, StartNewActivity::class.java).apply {
                putExtra("tournamentId", tournamentId)
                putExtra("matchNumber", matchNumber)
            }
            context.startActivity(intent)
            alertDialog.dismiss()
        }
        btnAdd.setOnClickListener {
            val intent = Intent(context, AddRoundMatchActivity::class.java).apply {
                putExtra("tournamentId", tournamentId)
                putExtra("matchNumber", matchNumber)
            }
            context.startActivity(intent)
            alertDialog.dismiss()
        }
        alertDialog.show()
    }
}