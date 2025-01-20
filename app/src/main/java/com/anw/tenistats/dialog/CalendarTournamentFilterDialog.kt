package com.anw.tenistats.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import com.anw.tenistats.CalendarTournamentActivity
import com.anw.tenistats.R
import com.anw.tenistats.player.ViewTeamActivity
import com.anw.tenistats.tournament.GenerateDrawActivity

class CalendarTournamentFilterDialog(
    private val context: Context,
    private var isOnlyMyTournamentsFilter: Boolean = true) { //1 - only my, 0 - all tournaments

    fun show() {
        // Tworzymy dialog
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_calendar_tournament_filter) // musisz stworzyć odpowiedni layout XML dla tego dialogu
        dialog.setTitle("Choose Filter")

        val radioOnlyMyTournaments = dialog.findViewById<RadioButton>(R.id.buttonMyTournaments)
        val radioAllTournaments = dialog.findViewById<RadioButton>(R.id.buttonAllTournaments)
        val buttonCancel = dialog.findViewById<Button>(R.id.buttonCancel)
        val buttonSubmit = dialog.findViewById<Button>(R.id.buttonSubmit)

        radioOnlyMyTournaments.isChecked = isOnlyMyTournamentsFilter
        radioAllTournaments.isChecked = !isOnlyMyTournamentsFilter

        buttonSubmit.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(context, CalendarTournamentActivity::class.java).apply {
                putExtra("isOnlyMyTournamentsFilter", radioOnlyMyTournaments.isChecked)
            }
            context.startActivity(intent)
        }
        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}