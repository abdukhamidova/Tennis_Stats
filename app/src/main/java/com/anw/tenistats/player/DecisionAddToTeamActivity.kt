package com.anw.tenistats.player

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import com.anw.tenistats.R

class DecisionAddToTeamActivity(private val context: Context) {

    private lateinit var alertDialog: AlertDialog

    fun show(playerName: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_decision_add_to_team, null)
        alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val btnNo: Button = dialogView.findViewById(R.id.buttonPlay)
        val btnYes: Button = dialogView.findViewById(R.id.buttonYes)
        val textPlayerName: TextView = dialogView.findViewById(R.id.textViewConfirm)

        textPlayerName.text = "Would you like to add $playerName to your group?"

        btnNo.setOnClickListener {
            alertDialog.dismiss()


            val intent = Intent(context, ViewPlayerActivity::class.java)
            context.startActivity(intent)
        }

        btnYes.setOnClickListener {

            val chooseTeamDialog = ChooseTeamDialog(context, playerName)
            chooseTeamDialog.show()
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
}
