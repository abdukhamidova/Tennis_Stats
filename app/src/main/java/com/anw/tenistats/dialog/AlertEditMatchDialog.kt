package com.anw.tenistats.dialog

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.Button
import com.anw.tenistats.R
import com.anw.tenistats.matchplay.EndOfMatchActivity
import com.anw.tenistats.tournament.GenerateDrawActivity

class AlertEditMatchDialog(
    private val context: Context
) {
    private lateinit var alertDialog: AlertDialog
    fun show(tournamentId: String, matchNumber: String, drawSize: String, ) {
        val dialogView =
            LayoutInflater.from(context).inflate(R.layout.dialog_alert_edit_match, null)
        alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()
        val btnOk: Button = dialogView.findViewById(R.id.buttonOk)
        btnOk.setOnClickListener {
            val intent = Intent(context, GenerateDrawActivity::class.java).apply {
                putExtra("tournament_id", tournamentId)
                putExtra("match_number", matchNumber)
                putExtra("draw_size", drawSize)
            }
            context.startActivity(intent)
            (context as Activity).finish()
            alertDialog.dismiss()
        }
        alertDialog.show()
    }
}