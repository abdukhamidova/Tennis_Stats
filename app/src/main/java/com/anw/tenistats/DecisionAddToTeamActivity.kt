package com.anw.tenistats

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView

class DecisionAddToTeamActivity(private val context: Context) {

    private lateinit var alertDialog: AlertDialog

    fun show(playerName: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.decision_add_to_team_dialog, null)
        alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val btnNo: Button = dialogView.findViewById(R.id.buttonNo)
        val btnYes: Button = dialogView.findViewById(R.id.buttonYes)
        val textPlayerName: TextView = dialogView.findViewById(R.id.textViewConfirm)

        textPlayerName.text = "Would you like to add $playerName to your team?"

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
