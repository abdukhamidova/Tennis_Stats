package com.anw.tenistats.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anw.tenistats.stats.MatchViewClass
import com.anw.tenistats.R
import com.anw.tenistats.dialog.ResumeOrStatsDialog
import com.anw.tenistats.matchplay.getGoldenDrawable
import com.anw.tenistats.stats.ViewMatchesActivity
import com.anw.tenistats.tournament.AddRoundMatchActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class MatchAdapter(
    private val originalList: List<MatchViewClass>,
    private val contextType: Context,
    private val tournamentId: String?,
    private val matchNumber: String?
) :
    RecyclerView.Adapter<MatchAdapter.MyViewHolder>(), Filterable {
    private var filteredList: List<MatchViewClass> = originalList
    private lateinit var context: Context
    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        context = parent.context
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_match, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = filteredList[position]

        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        val date = Date(currentItem.data)
        val formattedDate = dateFormat.format(date)


        Log.d("MyAdapter", "onBindViewHolder - date: $formattedDate, player1: ${currentItem.player1}, player2: ${currentItem.player2}")

        holder.date.text = formattedDate
        holder.player1.text = currentItem.player1
        holder.player2.text = currentItem.player2
        holder.pkt1.text=currentItem.pkt1
        holder.pkt2.text=currentItem.pkt2
        holder.set1p1.text=currentItem.set1p1
        holder.set1p2.text=currentItem.set1p2
        holder.set2p1.text=currentItem.set2p1
        holder.set2p2.text=currentItem.set2p2
        holder.set3p1.text=currentItem.set3p1
        holder.set3p2.text=currentItem.set3p2

        if(currentItem.winner != "")
        {
            val goldenLaurel = getGoldenDrawable(holder.itemView.context, R.drawable.icon_laurel3)
            holder.iconPlayer1.setCompoundDrawablesWithIntrinsicBounds(goldenLaurel, null, null, null)
            holder.iconPlayer2.setCompoundDrawablesWithIntrinsicBounds(goldenLaurel, null, null, null)
            if(currentItem.winner == currentItem.player1)
            run{
                holder.iconPlayer1.visibility = View.VISIBLE
                holder.iconPlayer2.visibility = View.INVISIBLE
            }
            else if (currentItem.winner == currentItem.player2)
            run {
                holder.iconPlayer1.visibility = View.INVISIBLE
                holder.iconPlayer2.visibility = View.VISIBLE
            }
        }
        else if(currentItem.LastServePlayer != "")
        {
            holder.iconPlayer1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ball, 0, 0, 0)
            holder.iconPlayer2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ball, 0, 0, 0)
            if(currentItem.LastServePlayer==currentItem.player1)
            run{
                holder.iconPlayer1.visibility = View.VISIBLE
                holder.iconPlayer2.visibility = View.INVISIBLE
            }
            else if (currentItem.LastServePlayer==currentItem.player2)
            run {
                holder.iconPlayer1.visibility = View.INVISIBLE
                holder.iconPlayer2.visibility = View.VISIBLE
            }
        }
        else {
            holder.iconPlayer1.visibility = View.INVISIBLE
            holder.iconPlayer2.visibility = View.INVISIBLE
        }

        holder.itemView.setOnClickListener {
            val match = filteredList[position]
            listener?.onItemClick(match)
            // Pobierz datę meczu w formacie milisekund
            val dateInMillis = currentItem.data
            Log.d("MyAdapter", "Date in milliseconds: $dateInMillis")
            // Otwórz ViewHistoryActivity i przekaż datę meczu
            /*val intent = Intent(context, ViewHistoryActivity::class.java)
            intent.putExtra("matchDateInMillis", dateInMillis)
            context.startActivity(intent)*/

            if(contextType is ViewMatchesActivity) {// Otwórz ResumeOrStatsDialog i przekaż datę meczu
                val ResumeOrStatsDialog = ResumeOrStatsDialog(context)
                ResumeOrStatsDialog.show(
                    dateInMillis
                )
            }else if(contextType is AddRoundMatchActivity){
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
                                dbref.child(it).child("tournamentId").setValue(tournamentId)
                                dbref.child(it).child("matchNumber").setValue(matchNumber)
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

                contextType.finish()
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredResults = mutableListOf<MatchViewClass>()
                if (constraint.isNullOrEmpty()) {
                    filteredResults.addAll(originalList)
                } else {
                    val filterPattern = constraint.toString().toLowerCase(Locale.getDefault()).trim()
                    for (item in originalList) {
                        if (item.player1.toLowerCase(Locale.getDefault()).contains(filterPattern)
                            || item.player2.toLowerCase(Locale.getDefault()).contains(filterPattern)) {
                            filteredResults.add(item)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredResults
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<MatchViewClass> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.textviewDateVM)
        val player1: TextView = itemView.findViewById(R.id.textviewPlayer1VM)
        val player2: TextView = itemView.findViewById(R.id.textviewPlayer2VM)
        val pkt1: TextView = itemView.findViewById(R.id.textViewPlayer1PktStats)
        val pkt2: TextView = itemView.findViewById(R.id.textViewPlayer2PktStats)
        val set1p1: TextView = itemView.findViewById(R.id.textViewPlayer1Set1Match)
        val set1p2: TextView = itemView.findViewById(R.id.textViewPlayer2Set1Match)
        val set2p1: TextView = itemView.findViewById(R.id.textViewPlayer1Set2Match)
        val set2p2: TextView = itemView.findViewById(R.id.textViewPlayer2Set2Match)
        val set3p1: TextView = itemView.findViewById(R.id.textViewPlayer1Set3Match)
        val set3p2: TextView = itemView.findViewById(R.id.textViewPlayer2Set3Match)
        val iconPlayer1: TextView = itemView.findViewById(R.id.textViewWin1Match)
        val iconPlayer2: TextView = itemView.findViewById(R.id.textViewWin2Match)
    }

    interface OnItemClickListener {
        fun onItemClick(matchView: MatchViewClass)
    }
}
