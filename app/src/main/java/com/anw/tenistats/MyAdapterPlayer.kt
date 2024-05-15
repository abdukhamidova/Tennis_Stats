import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anw.tenistats.PlayerDetailsActivity
import com.anw.tenistats.PlayerView
import com.anw.tenistats.R
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class MyAdapterPlayer(
    private val originalList: List<PlayerView>,
    private val firebaseAuth: FirebaseAuth
) :
    RecyclerView.Adapter<MyAdapterPlayer.MyViewHolder>(), Filterable {

    private var filteredList: List<PlayerView> = originalList.filter { it.active } // Filtrowanie tylko aktywnych zawodników
    private lateinit var context: Context
    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        context = parent.context
        val itemView = LayoutInflater.from(context).inflate(R.layout.player_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = filteredList[position]

        holder.playerName.text = currentItem.player

        holder.itemView.setOnClickListener {
            val player = filteredList[position]
            listener?.onItemClick(player)

            val playerId = currentItem.player
            val intent = Intent(context, PlayerDetailsActivity::class.java)
            intent.putExtra("playerId", playerId)
            context.startActivity(intent)
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredResults = originalList.filter { it.active } // Filtrowanie tylko aktywnych zawodników
                if (constraint.isNullOrEmpty()) {
                    return FilterResults().apply {
                        values = filteredResults
                    }
                } else {
                    val filterPattern = constraint.toString().toLowerCase(Locale.getDefault()).trim()
                    val filteredList = filteredResults.filter {
                        it.firstName.toLowerCase(Locale.getDefault()).contains(filterPattern)
                                || it.lastName.toLowerCase(Locale.getDefault()).contains(filterPattern)
                    }
                    return FilterResults().apply {
                        values = filteredList
                    }
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<PlayerView> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerName: TextView = itemView.findViewById(R.id.textviewPlayer)
    }

    interface OnItemClickListener {
        fun onItemClick(playerView: PlayerView)
    }
}
