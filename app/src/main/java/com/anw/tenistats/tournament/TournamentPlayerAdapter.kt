package com.anw.tenistats.tournament

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anw.tenistats.R
import com.anw.tenistats.player.PlayerView

class TournamentPlayerAdapter(
    private val playerList: List<PlayerView>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<TournamentPlayerAdapter.PlayerViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(playerView: PlayerView)
        fun onDeletePlayerClicked(playerView: PlayerView)
    }

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerName: TextView = itemView.findViewById(R.id.TextViewTournamentPlayer)
        val deletePlayer: ImageView = itemView.findViewById(R.id.ImageViewDeletePlayer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tournament_player, parent, false)
        return PlayerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = playerList[position]
        holder.playerName.text = player.player
        holder.itemView.setOnClickListener {
            listener.onItemClick(player)
        }
        holder.deletePlayer.setOnClickListener {
            listener.onDeletePlayerClicked(player)
        }
    }

    override fun getItemCount(): Int = playerList.size
}
