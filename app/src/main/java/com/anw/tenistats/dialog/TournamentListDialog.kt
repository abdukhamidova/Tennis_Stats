package com.anw.tenistats.dialog

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.anw.tenistats.R
import com.anw.tenistats.tournament.AddTournamentActivity
import com.anw.tenistats.tournament.TournamentDataClass
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TournamentListDialog(private val context: Context, private val tournamentList: ArrayList<TournamentDataClass>?, private val clicedDate:Long) {
    private lateinit var alertDialog: AlertDialog

    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_tournament_list, null)
        alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val buttonCancel: Button = dialogView.findViewById(R.id.buttonCancel)
        val buttonAddTournament: Button = dialogView.findViewById(R.id.buttonAddTournamentCalendar)
        val playersContainer: LinearLayout = dialogView.findViewById(R.id.playersContainer)
        val textDate: TextView = dialogView.findViewById(R.id.textViewDate)

        textDate.text=formatDate(clicedDate)

        // Jeśli lista jest pusta lub null, wyświetl komunikat
        if (tournamentList.isNullOrEmpty()) {
            val noTournamentTextView = TextView(context).apply {
                text = "No tournaments available for this day."
                textSize = 18f
                setTextColor(context.getColor(R.color.dialog_textTitle_color))
                setPadding(16, 16, 16, 16)
            }
            playersContainer.addView(noTournamentTextView)
        } else {
            // Iteracja po liście turniejów i dynamiczne dodawanie elementów
            for (tournament in tournamentList) {
                val tournamentView = LayoutInflater.from(context).inflate(R.layout.item_tournament_entry, playersContainer, false)

                val textViewTournamentName: TextView = tournamentView.findViewById(R.id.textViewTournamentName)
                val textViewTournamentDate: TextView = tournamentView.findViewById(R.id.textViewTournamentDate)

                fun formatDate(milliseconds: Long): String {
                    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    return sdf.format(Date(milliseconds))
                }

                textViewTournamentName.text = tournament.name
                textViewTournamentDate.text = "${tournament.startDate?.let { formatDate(it) }} - ${tournament.endDate?.let {
                    formatDate(
                        it
                    )
                }}"

                // Kliknięcie w turniej otwiera nową aktywność z przekazaniem nazwy turnieju
                tournamentView.setOnClickListener {
                    val TournamentDialog = TournamentDialog(context)
                    TournamentDialog.show(
                        tournament.id
                    )
                    alertDialog.dismiss()
                }

                playersContainer.addView(tournamentView)
            }
        }

        buttonCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        buttonAddTournament.setOnClickListener {
            val intent = Intent(context, AddTournamentActivity::class.java)
            intent.putExtra("startDate",clicedDate)
            context.startActivity(intent)
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
    fun formatDate(milliseconds: Long): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return sdf.format(Date(milliseconds))
    }
}
