package com.anw.tenistats.player

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import com.anw.tenistats.R
import android.widget.ExpandableListView
import android.widget.Filter
import android.widget.Filterable
import java.util.Locale


class TeamExpandableListAdapter(
    private val context: Context,
    private val teamList: List<TeamView>,
    private val playerMap: Map<String, List<PlayerView>>,
    private val actionListener: ViewTeamActivity
) : BaseExpandableListAdapter(), Filterable {

    private val mutableTeamList: MutableList<TeamView> = teamList.toMutableList()
    private val originalTeamList: List<TeamView> = teamList // Dla przechowania oryginalnej listy
    private val expandableListView: ExpandableListView = (context as Activity).findViewById(R.id.expandableListView)

    override fun getGroupCount(): Int = mutableTeamList.size

    override fun getChildrenCount(groupPosition: Int): Int {
        val teamId = mutableTeamList[groupPosition].name
        return playerMap[teamId]?.size ?: 0
    }

    override fun getGroup(groupPosition: Int): Any = mutableTeamList[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        val teamId = mutableTeamList[groupPosition].name
        return playerMap[teamId]?.get(childPosition) ?: PlayerView()
    }

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun hasStableIds(): Boolean = true

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val childView = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_player_list, parent, false)
        val playerName = childView.findViewById<TextView>(R.id.tvPlayerName)
        val player = getChild(groupPosition, childPosition) as PlayerView

        playerName.text = "${player.firstName} ${player.lastName}"

        childView.setOnClickListener {
            val intent = Intent(context, PlayerDetailsActivity::class.java)
            intent.putExtra("playerId", player.player)
            context.startActivity(intent)
        }

        val ivDeletePlayer = childView.findViewById<ImageView>(R.id.ivDeletePlayer)
        ivDeletePlayer.setOnClickListener {
            val deletePlayerDialog = DeletePlayerFromTeamDialog(
                context,
                (getGroup(groupPosition) as TeamView).name,
                playerName.text.toString()
            )
            deletePlayerDialog.show()
        }

        return childView
    }


    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val groupView = convertView ?: LayoutInflater.from(context).inflate(R.layout.team_item, parent, false)
        val teamName = groupView.findViewById<TextView>(R.id.tvTeamName)
        val editButton = groupView.findViewById<ImageView>(R.id.ivEditTeam)
        val deleteButton = groupView.findViewById<ImageView>(R.id.ivDeleteTeam)
        val addPlayerButton = groupView.findViewById<ImageView>(R.id.ivAddPlayer)

        val team = getGroup(groupPosition) as TeamView
        teamName.text = team.name

        if (team.name == "Favorites") {
            editButton.visibility = View.GONE
            deleteButton.visibility = View.GONE
            addPlayerButton.visibility = View.GONE
        } else {
            editButton.visibility = View.VISIBLE
            deleteButton.visibility = View.VISIBLE
            addPlayerButton.visibility = View.VISIBLE

            editButton.setOnClickListener { actionListener.onEditTeam(team) }
            deleteButton.setOnClickListener {
                val deleteTeamDialog = DeleteTeamDialog(context, team)
                deleteTeamDialog.show()
            }
            addPlayerButton.setOnClickListener {
                val addPlayerDialog = AddPlayerToTeamDialog(context, team)
                addPlayerDialog.show()
            }
        }

        return groupView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

    fun updateTeam(position: Int, updatedTeam: TeamView) {
        mutableTeamList[position] = updatedTeam

        (context as? Activity)?.runOnUiThread {
            notifyDataSetChanged()

            expandableListView.collapseGroup(position)
            expandableListView.expandGroup(position)
            expandableListView.setAdapter(this)
        }
    }

    // Dodaj filtr dla wyszukiwania zespołów
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterPattern = constraint?.toString()?.lowercase(Locale.getDefault())?.trim()

                val filteredResults = if (filterPattern.isNullOrEmpty()) {
                    originalTeamList
                } else {
                    originalTeamList.filter {
                        it.name.lowercase(Locale.getDefault()).contains(filterPattern)
                    }
                }

                return FilterResults().apply {
                    values = filteredResults
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                mutableTeamList.clear()
                mutableTeamList.addAll(results?.values as List<TeamView>)
                notifyDataSetChanged()
            }
        }
    }

    interface TeamActionListener {
        fun onEditTeam(team: TeamView)
        fun onDeleteTeam(team: TeamView)
        fun onAddPlayer(team: TeamView)
    }
}
