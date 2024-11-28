package com.anw.tenistats.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anw.tenistats.R
import com.anw.tenistats.databinding.ItemRoundBinding
import com.anw.tenistats.tournament.Round
import com.anw.tenistats.tournament.TournamentMatchDataClass

class TournamentRoundAdapter(
    private var rounds: List<Round>,
    private val onMatchClick: (TournamentMatchDataClass) -> Unit
) : RecyclerView.Adapter<TournamentRoundAdapter.RoundViewHolder>() {

    inner class RoundViewHolder(private val binding: ItemRoundBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(round: Round) {
            with(binding) {
                // Bind Match 1
                vsPlayers1.textViewItem1Player1.text = round.match1.player1Name
                vsPlayers1.textViewItem1Player2.text = round.match1.player2Name
                vsPlayers1.textViewItem1P1Set1.text = round.match1.player1Set1
                vsPlayers1.textViewItem1P2Set1.text = round.match1.player2Set1
                vsPlayers1.textViewItem1P1Set2.text = round.match1.player1Set2
                vsPlayers1.textViewItem1P2Set2.text = round.match1.player2Set2
                vsPlayers1.textViewItem1P1Set3.text = round.match1.player1Set3
                vsPlayers1.textViewItem1P2Set3.text = round.match1.player2Set3
                // Add listeners for Match 1
                root.findViewById<View>(R.id.vsPlayers1).setOnClickListener {
                    onMatchClick(round.match1)
                }

                // Bind Match 2
                vsPlayers2.textViewItem1Player1.text = round.match2.player1Name
                vsPlayers2.textViewItem1Player2.text = round.match2.player2Name
                vsPlayers2.textViewItem1P1Set1.text = round.match2.player1Set1
                vsPlayers2.textViewItem1P2Set1.text = round.match2.player2Set1
                vsPlayers2.textViewItem1P1Set2.text = round.match2.player1Set2
                vsPlayers2.textViewItem1P2Set2.text = round.match2.player2Set2
                vsPlayers2.textViewItem1P1Set3.text = round.match2.player1Set3
                vsPlayers2.textViewItem1P2Set3.text = round.match2.player2Set3
                // Add listeners for Match 2
                root.findViewById<View>(R.id.vsPlayers2).setOnClickListener {
                    onMatchClick(round.match2)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoundViewHolder {
        val binding = ItemRoundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoundViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoundViewHolder, position: Int) {
        holder.bind(rounds[position])
    }

    override fun getItemCount(): Int = rounds.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newRounds: List<Round>) {
        rounds = newRounds
        notifyDataSetChanged()
    }
}