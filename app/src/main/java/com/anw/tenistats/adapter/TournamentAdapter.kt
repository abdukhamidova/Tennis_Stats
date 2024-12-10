package com.anw.tenistats.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anw.tenistats.R
import com.anw.tenistats.dialog.TournamentDialog
import com.anw.tenistats.tournament.TournamentDataClass
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TournamentAdapter(
    private val originalList: List<TournamentDataClass>
) : RecyclerView.Adapter<TournamentAdapter.MyViewHolder>(), Filterable {

    private var filteredList: List<TournamentDataClass> = originalList.toMutableList()
    private var listener: OnItemClickListener? = null
    private lateinit var context: Context

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        context = parent.context
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_tournament, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = filteredList[position]

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val startDate = currentItem.startDate?.let { Date(it) }
        val endDate = currentItem.endDate?.let { Date(it) }
        val formattedDate = dateFormat.format(startDate) + " - " + dateFormat.format(endDate)

        holder.name.text = currentItem.name
        holder.city_country.text = "${currentItem.place}, ${currentItem.country}"
        holder.date.text = formattedDate
        when(currentItem.surface){
            "Hard" -> {
                holder.icon.setBackgroundResource(R.color.hard_court_color)
            }
            "Clay" -> {
                holder.icon.setBackgroundResource(R.color.clay_court_color)
            }
            "Grass" -> {
                holder.icon.setBackgroundResource(R.color.grass_court_color)
            }
            "Carpet" -> {
                holder.icon.setBackgroundResource(R.color.carpet_court_color)
            }
        }

        // Set visibility of the notification icon based on the 'changes' value
        holder.notificationIcon.visibility = if (currentItem.changes>0 && currentItem.creator== FirebaseAuth.getInstance().currentUser?.uid.toString()) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            listener?.onItemClick(currentItem)

            val TournamentDialog = TournamentDialog(context)
            TournamentDialog.show(
                currentItem.id
            )
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.TextViewTournamentName)
        val city_country: TextView = itemView.findViewById(R.id.TextViewPlace)
        val date: TextView = itemView.findViewById(R.id.textviewDates)
        val icon: ImageView = itemView.findViewById(R.id.ImageViewCourt)
        val notificationIcon: ImageView = itemView.findViewById(R.id.notificationIcon)
    }

    interface OnItemClickListener {
        fun onItemClick(itemView: TournamentDataClass)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase(Locale.getDefault()) ?: ""
                val filtered = if (query.isEmpty()) {
                    originalList
                } else {
                    originalList.filter {
                        it.name?.lowercase(Locale.getDefault())?.contains(query) == true
                    }
                }

                val results = FilterResults()
                results.values = filtered
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<TournamentDataClass> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }
}