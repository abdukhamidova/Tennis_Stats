package com.anw.tenistats.dialog

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import com.anw.tenistats.CalendarCoachActivity
import com.anw.tenistats.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DeleteEventDialog(private val context: Context) {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var alertDialog: AlertDialog

    fun show(eventId: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete_event, null)
        alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val btnCancel: Button = dialogView.findViewById(R.id.buttonCancel)
        val btnDelete: Button = dialogView.findViewById(R.id.buttonDelete)

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnDelete.setOnClickListener {
            firebaseAuth = FirebaseAuth.getInstance()
            val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
            database =
                FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference(userId).child("Events")

            val eventReference = database.child(eventId)

            eventReference.removeValue()
                .addOnSuccessListener {
                    // Usunięcie zakończone sukcesem
                    Toast.makeText(context, "Event was deleted successfully.", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()

                    // Otwórz CalendarCoachActivity
                    val intent = Intent(context, CalendarCoachActivity::class.java)
                    context.startActivity(intent)

                    if (context is Activity) {
                        context.finish()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Event couldn't be deleted.", Toast.LENGTH_SHORT).show()
                }
        }
        alertDialog.show()
    }
}
