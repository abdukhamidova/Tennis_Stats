package com.anw.tenistats.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.anw.tenistats.R
import com.anw.tenistats.matchplay.getDrawable
import com.anw.tenistats.matchplay.getGoldenDrawable
import com.anw.tenistats.tournament.TournamentClass
import com.anw.tenistats.tournament.TournamentDetailsActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TournamentAdapter(private val originalList: List<TournamentClass>) :
    RecyclerView.Adapter<TournamentAdapter.MyViewHolder>() {

    private var filteredList: List<TournamentClass> = originalList
    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.tournament_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = filteredList[position]

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val startDate = Date(currentItem.startDate)
        val endDate = Date(currentItem.endDate)
        val formattedDate = dateFormat.format(startDate) + " - " + dateFormat.format(endDate)

        holder.name.text = currentItem.name
        holder.city_country.text = "${currentItem.place}, ${currentItem.country}"
        holder.date.text = formattedDate
        when(currentItem.surface){
            "Hard" -> {
                val hardCourt = getDrawable(holder.itemView.context, R.drawable.icon_court, R.color.hard_court_color)
                holder.icon.setCompoundDrawablesWithIntrinsicBounds(hardCourt, null, null, null)
            }
            "Clay" -> {
                //holder.icon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.clay_court_color))
                val clayCourt = getDrawable(holder.itemView.context, R.drawable.icon_court, R.color.clay_court_color)
                holder.icon.setCompoundDrawablesWithIntrinsicBounds(clayCourt, null, null, null)
            }
            "Grass" -> {
                //holder.icon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.grass_court_color))
                val grassCourt = getDrawable(holder.itemView.context, R.drawable.icon_court, R.color.grass_court_color)
                holder.icon.setCompoundDrawablesWithIntrinsicBounds(grassCourt, null, null, null)
            }
            "Carpet" -> {
                //holder.icon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.carpet_court_color))
                val carpetCourt = getDrawable(holder.itemView.context, R.drawable.icon_court, R.color.carpet_court_color)
                holder.icon.setCompoundDrawablesWithIntrinsicBounds(carpetCourt, null, null, null)
            }
        }

        holder.itemView.setOnClickListener {
            listener?.onItemClick(currentItem)

            val intent = Intent(holder.itemView.context, TournamentDetailsActivity::class.java)
            intent.putExtra("tournament_id", currentItem.id)
            holder.itemView.context.startActivity(intent)
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.TextViewTournamentName)
        val city_country: TextView = itemView.findViewById(R.id.TextViewPlace)
        val date: TextView = itemView.findViewById(R.id.textviewDates)
        val icon: TextView = itemView.findViewById(R.id.ImageViewCourt)
    }

    interface OnItemClickListener {
        fun onItemClick(itemView: TournamentClass)
    }
}