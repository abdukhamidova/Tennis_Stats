package com.anw.tenistats.adapter

import android.annotation.SuppressLint
import android.util.Log
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
                vsPlayers1.textViewItem1Player1.text = round.match1.player1
                vsPlayers1.textViewItem1Player2.text = round.match1.player2
                vsPlayers1.textViewItem1P1Set1.text = round.match1.set1p1
                vsPlayers1.textViewItem1P2Set1.text = round.match1.set1p2
                vsPlayers1.textViewItem1P1Set2.text = round.match1.set2p1
                vsPlayers1.textViewItem1P2Set2.text = round.match1.set2p2
                vsPlayers1.textViewItem1P1Set3.text = round.match1.set3p1
                vsPlayers1.textViewItem1P2Set3.text = round.match1.set3p2

                if (round.match1.set3p1.isNotEmpty() && round.match1.set3p2.isNotEmpty()) {
                    vsPlayers1.textViewItem1P1Set3.visibility = View.VISIBLE
                    vsPlayers1.textViewItem1P2Set3.visibility = View.VISIBLE
                    vsPlayers1.textViewItem1P1Set3.text = round.match1.set3p1
                    vsPlayers1.textViewItem1P2Set3.text = round.match1.set3p2
                } else {
                    vsPlayers1.textViewItem1P1Set3.visibility = View.GONE
                    vsPlayers1.textViewItem1P2Set3.visibility = View.GONE
                }

                // If it's the final round, hide match 2
                if (round.match1 != round.match2) {
                    vsPlayers2.root.visibility = View.VISIBLE
                    viewTopLine.visibility = View.VISIBLE
                    viewVerticalLine.visibility = View.VISIBLE
                    viewButtomLine.visibility = View.VISIBLE
                    viewLittleLine.visibility = View.VISIBLE
                    vsPlayers2.textViewItem1Player1.text = round.match2.player1
                    vsPlayers2.textViewItem1Player2.text = round.match2.player2
                    vsPlayers2.textViewItem1P1Set1.text = round.match2.set1p1
                    vsPlayers2.textViewItem1P2Set1.text = round.match2.set1p2
                    vsPlayers2.textViewItem1P1Set2.text = round.match2.set2p1
                    vsPlayers2.textViewItem1P2Set2.text = round.match2.set2p2
                    vsPlayers2.textViewItem1P1Set3.text = round.match2.set3p1
                    vsPlayers2.textViewItem1P2Set3.text = round.match2.set3p2

                    if (round.match2.set3p1.isNotEmpty() && round.match2.set3p2.isNotEmpty()) {
                        vsPlayers2.textViewItem1P1Set3.visibility = View.VISIBLE
                        vsPlayers2.textViewItem1P2Set3.visibility = View.VISIBLE
                        vsPlayers2.textViewItem1P1Set3.text = round.match2.set3p1
                        vsPlayers2.textViewItem1P2Set3.text = round.match2.set3p2
                    } else {
                        vsPlayers2.textViewItem1P1Set3.visibility = View.GONE
                        vsPlayers2.textViewItem1P2Set3.visibility = View.GONE
                    }
                } else {
                    vsPlayers2.root.visibility = View.GONE
                    viewTopLine.visibility = View.GONE
                    viewVerticalLine.visibility = View.GONE
                    viewButtomLine.visibility = View.GONE
                    viewLittleLine.visibility = View.GONE
                }

                // Add listeners for Match 1
                root.findViewById<View>(R.id.vsPlayers1).setOnClickListener {
                    onMatchClick(round.match1)
                }

                // Add listeners for Match 2 (if visible)
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
        Log.d("Adapter", "Updating adapter with ${newRounds.size} rounds")
        rounds = newRounds
        notifyDataSetChanged()
    }
}
