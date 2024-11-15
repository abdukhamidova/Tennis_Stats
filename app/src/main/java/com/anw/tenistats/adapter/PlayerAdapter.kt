package com.anw.tenistats.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.anw.tenistats.player.PlayerDetailsActivity
import com.anw.tenistats.player.PlayerView
import com.anw.tenistats.R
import com.anw.tenistats.player.ViewPlayerActivity
import com.anw.tenistats.player.ViewTeamActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import java.util.*

class PlayerAdapter(
    private val originalList: List<PlayerView>,
    private var firebaseAuth: FirebaseAuth,
) : RecyclerView.Adapter<PlayerAdapter.MyViewHolder>(), Filterable {

    private var filteredList: List<PlayerView> = originalList.filter { it.active }
    private lateinit var context: Context
    private var listener: OnItemClickListener? = null
    private lateinit var database: DatabaseReference


    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        context = parent.context
        val itemView = LayoutInflater.from(context).inflate(R.layout.player_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int = filteredList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = filteredList[position]
        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser?.uid
        database = FirebaseDatabase.getInstance("https://tennis-stats-ededc-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference(user.toString())
        holder.playerName.text = currentItem.player

        database.child("Players").child(currentItem.player).child("isFavorite")
            .get()
            .addOnSuccessListener { snapshot ->
                val isFavoriteInDatabase = snapshot.getValue(Boolean::class.java) ?: false
                if (isFavoriteInDatabase) {
                    holder.buttonAddToTeam.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.star_full, 0)
                } else {
                    holder.buttonAddToTeam.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.star, 0)
                }
            }

        holder.itemView.setOnClickListener {
            listener?.onItemClick(currentItem)
            val intent = Intent(context, PlayerDetailsActivity::class.java)
            intent.putExtra("playerId", currentItem.player)
            context.startActivity(intent)
        }

        holder.buttonAddToTeam.setOnClickListener {
            val favoriteTeamRef = database.child("Teams").child("Favorites").child("players")
            favoriteTeamRef.get().addOnSuccessListener { snapshot ->
                val playersList = snapshot.getValue(object : GenericTypeIndicator<MutableList<String>>() {}) ?: mutableListOf()

                if (playersList.contains(currentItem.player)) {
                    database.child("Players").child(currentItem.player).child("isFavorite").setValue(false)
                    playersList.remove(currentItem.player)
                    favoriteTeamRef.setValue(playersList)
                        .addOnSuccessListener {
                            Toast.makeText(context, "${currentItem.player} removed from favorites", Toast.LENGTH_SHORT).show()
                            holder.buttonAddToTeam.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.star, 0)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to remove ${currentItem.player} from favorites", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    playersList.add(currentItem.player)
                    database.child("Players").child(currentItem.player).child("isFavorite").setValue(true)
                    favoriteTeamRef.setValue(playersList)
                        .addOnSuccessListener {
                            Toast.makeText(context, "${currentItem.player} added to favorites", Toast.LENGTH_SHORT).show()
                            holder.buttonAddToTeam.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.star_full, 0)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to add ${currentItem.player} to favorites", Toast.LENGTH_SHORT).show()
                        }
                }
                /*val intent = Intent(context, ViewPlayerActivity   ::class.java)
                context.startActivity(intent)*/
            }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to retrieve players list", Toast.LENGTH_SHORT).show()
                }
        }


    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredResults: List<PlayerView> = if (constraint.isNullOrEmpty()) {
                    originalList.filter { it.active }
                } else {
                    val filterPattern = constraint.toString().toLowerCase(Locale.getDefault()).trim()
                    originalList.filter {
                        it.firstName.toLowerCase(Locale.getDefault()).contains(filterPattern) ||
                                it.lastName.toLowerCase(Locale.getDefault()).contains(filterPattern)
                    }
                }
                return FilterResults().apply {
                    values = filteredResults.distinct() // Usuwamy duplikaty, jeżeli występują
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<PlayerView> ?: emptyList()
                notifyDataSetChanged()

                // Ustaw widoczność komunikatu w zależności od liczby wyników
                if (filteredList.isEmpty()) {
                    (context as? Activity)?.findViewById<TextView>(R.id.textViewNotFound)?.visibility = View.VISIBLE
                } else {
                    (context as? Activity)?.findViewById<TextView>(R.id.textViewNotFound)?.visibility = View.GONE
                }
            }

        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerName: TextView = itemView.findViewById(R.id.textviewPlayer)
        val buttonAddToTeam: TextView = itemView.findViewById(R.id.buttonAddToFavorite)
    }

    interface OnItemClickListener {
        fun onItemClick(playerView: PlayerView)
    }
}
