import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import com.anw.tenistats.MatchView
import com.anw.tenistats.R

import java.util.*

class MyAdapter(private val originalList: List<MatchView>) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>(), Filterable {

    private var filteredList: List<MatchView> = originalList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.match_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = filteredList[position]

        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        val date = Date(currentItem.data.toLong())
        val formattedDate = dateFormat.format(date)

        holder.data.text = formattedDate
        holder.player1.text = currentItem.player1
        holder.player2.text = currentItem.player2
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredResults = mutableListOf<MatchView>()
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
                filteredList = results?.values as? List<MatchView> ?: emptyList()
                notifyDataSetChanged()
            }
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val data: TextView = itemView.findViewById(R.id.textviewDateVM)
        val player1: TextView = itemView.findViewById(R.id.textviewPlayer1VM)
        val player2: TextView = itemView.findViewById(R.id.textviewPlayer2VM)
    }
}
