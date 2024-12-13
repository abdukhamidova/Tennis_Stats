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
import com.google.firebase.auth.FirebaseAuth
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.content.ContextCompat
class TournamentRoundAdapter(
    private var rounds: List<Round>,
    private var creator: String,
    private val onMatchClick: (TournamentMatchDataClass) -> Unit
) : RecyclerView.Adapter<TournamentRoundAdapter.RoundViewHolder>() {

    inner class RoundViewHolder(private val binding: ItemRoundBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(round: Round) {
            with(binding) {
                // Reset the visibility and drawable states for Match 1
                vsPlayers1.textViewWin1Match.visibility = View.INVISIBLE
                vsPlayers1.textViewWin1Match.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                vsPlayers1.textViewWin2Match.visibility = View.INVISIBLE
                vsPlayers1.textViewWin2Match.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

                // Reset the visibility and drawable states for Match 2
                vsPlayers2.textViewWin1Match.visibility = View.INVISIBLE
                vsPlayers2.textViewWin1Match.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                vsPlayers2.textViewWin2Match.visibility = View.INVISIBLE
                vsPlayers2.textViewWin2Match.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

                // Bind Match 1
                vsPlayers1.textViewItem1Player1.text = round.match1.player1
                vsPlayers1.textViewItem1Player2.text = round.match1.player2
                vsPlayers1.textViewItem1P1Set1.text = round.match1.set1p1
                vsPlayers1.textViewItem1P2Set1.text = round.match1.set1p2
                vsPlayers1.textViewItem1P1Set2.text = round.match1.set2p1
                vsPlayers1.textViewItem1P2Set2.text = round.match1.set2p2
                vsPlayers1.textViewItem1P1Set3.text = round.match1.set3p1
                vsPlayers1.textViewItem1P2Set3.text = round.match1.set3p2

                // Ensure set3 visibility
                if (round.match1.set3p1.isNotEmpty() && round.match1.set3p2.isNotEmpty()) {
                    vsPlayers1.textViewItem1P1Set3.visibility = View.VISIBLE
                    vsPlayers1.textViewItem1P2Set3.visibility = View.VISIBLE
                } else {
                    vsPlayers1.textViewItem1P1Set3.visibility = View.GONE
                    vsPlayers1.textViewItem1P2Set3.visibility = View.GONE
                }
                vsPlayers1.notificationIcon.visibility = if (round.match1.changes && creator == FirebaseAuth.getInstance().currentUser?.uid.toString()) View.VISIBLE else View.GONE

                // Set laurel icons for Match 1
                if (round.match1.winner.isNotEmpty()) {
                    val drawable = ContextCompat.getDrawable(itemView.context, R.drawable.icon_laurel3)
                    val tintedDrawable = DrawableCompat.wrap(drawable!!)
                    DrawableCompat.setTint(tintedDrawable, ContextCompat.getColor(itemView.context, R.color.gold))

                    if (round.match1.winner == "player1") {
                        vsPlayers1.textViewWin1Match.setCompoundDrawablesWithIntrinsicBounds(tintedDrawable, null, null, null)
                        vsPlayers1.textViewWin1Match.visibility = View.VISIBLE
                    } else if (round.match1.winner == "player2") {
                        vsPlayers1.textViewWin2Match.setCompoundDrawablesWithIntrinsicBounds(tintedDrawable, null, null, null)
                        vsPlayers1.textViewWin2Match.visibility = View.VISIBLE
                    }
                }

                // Similar logic for Match 2
                if (round.match1 != round.match2) {
                    vsPlayers2.root.visibility = View.VISIBLE
                    viewTopLine.visibility = View.VISIBLE
                    viewVerticalLine.visibility = View.VISIBLE
                    viewBottomLine.visibility = View.VISIBLE
                    viewLittleLine.visibility = View.VISIBLE
                    vsPlayers2.textViewItem1Player1.text = round.match2.player1
                    vsPlayers2.textViewItem1Player2.text = round.match2.player2
                    vsPlayers2.textViewItem1P1Set1.text = round.match2.set1p1
                    vsPlayers2.textViewItem1P2Set1.text = round.match2.set1p2
                    vsPlayers2.textViewItem1P1Set2.text = round.match2.set2p1
                    vsPlayers2.textViewItem1P2Set2.text = round.match2.set2p2
                    vsPlayers2.textViewItem1P1Set3.text = round.match2.set3p1
                    vsPlayers2.textViewItem1P2Set3.text = round.match2.set3p2

                    // Ensure set3 visibility
                    if (round.match2.set3p1.isNotEmpty() && round.match2.set3p2.isNotEmpty()) {
                        vsPlayers2.textViewItem1P1Set3.visibility = View.VISIBLE
                        vsPlayers2.textViewItem1P2Set3.visibility = View.VISIBLE
                    } else {
                        vsPlayers2.textViewItem1P1Set3.visibility = View.GONE
                        vsPlayers2.textViewItem1P2Set3.visibility = View.GONE
                    }

                    // Set laurel icons for Match 2
                    if (round.match2.winner.isNotEmpty()) {
                        val drawable = ContextCompat.getDrawable(itemView.context, R.drawable.icon_laurel3)
                        val tintedDrawable = DrawableCompat.wrap(drawable!!)
                        DrawableCompat.setTint(tintedDrawable, ContextCompat.getColor(itemView.context, R.color.gold))

                        if (round.match2.winner == "player1") {
                            vsPlayers2.textViewWin1Match.setCompoundDrawablesWithIntrinsicBounds(tintedDrawable, null, null, null)
                            vsPlayers2.textViewWin1Match.visibility = View.VISIBLE
                        } else if (round.match2.winner == "player2") {
                            vsPlayers2.textViewWin2Match.setCompoundDrawablesWithIntrinsicBounds(tintedDrawable, null, null, null)
                            vsPlayers2.textViewWin2Match.visibility = View.VISIBLE
                        }
                    }
                } else {
                    vsPlayers2.root.visibility = View.GONE
                    viewTopLine.visibility = View.GONE
                    viewVerticalLine.visibility = View.GONE
                    viewBottomLine.visibility = View.GONE
                    viewLittleLine.visibility = View.GONE
                }
                vsPlayers2.notificationIcon.visibility = if (round.match2.changes && creator == FirebaseAuth.getInstance().currentUser?.uid.toString()) View.VISIBLE else View.GONE
                // Add click listeners for matches
                root.findViewById<View>(R.id.vsPlayers1).setOnClickListener {
                    onMatchClick(round.match1)
                }
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
