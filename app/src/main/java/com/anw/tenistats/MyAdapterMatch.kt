import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anw.tenistats.MatchView
import com.anw.tenistats.R
import com.anw.tenistats.com.anw.tenistats.ResumeOrStatsDialogActivity
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class MyAdapterMatch(
    private val originalList: List<MatchView>,
    private val firebaseAuth: FirebaseAuth
) :
    RecyclerView.Adapter<MyAdapterMatch.MyViewHolder>(), Filterable {

    private var filteredList: List<MatchView> = originalList
    private lateinit var context: Context
    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        context = parent.context
        val itemView = LayoutInflater.from(context).inflate(R.layout.match_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = filteredList[position]

        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        val date = Date(currentItem.data)
        val formattedDate = dateFormat.format(date)

        Log.d("MyAdapter", "onBindViewHolder - date: $formattedDate, player1: ${currentItem.player1}, player2: ${currentItem.player2}")

        holder.date.text = formattedDate
        holder.player1.text = currentItem.player1
        holder.player2.text = currentItem.player2

        holder.itemView.setOnClickListener {
            val match = filteredList[position]
            listener?.onItemClick(match)

            // Pobierz datę meczu w formacie milisekund
            val dateInMillis = currentItem.data
            Log.d("MyAdapter", "Date in milliseconds: $dateInMillis")
            // Otwórz ViewHistoryActivity i przekaż datę meczu
            /*val intent = Intent(context, ViewHistoryActivity::class.java)
            intent.putExtra("matchDateInMillis", dateInMillis)
            context.startActivity(intent)*/
            // Otwórz ResumeOrStatsDialog i przekaż datę meczu
            val ResumeOrStatsDialog = ResumeOrStatsDialogActivity(context)
            ResumeOrStatsDialog.show(
                dateInMillis
            )
        }
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
        val date: TextView = itemView.findViewById(R.id.textviewDateVM)
        val player1: TextView = itemView.findViewById(R.id.textviewPlayer1VM)
        val player2: TextView = itemView.findViewById(R.id.textviewPlayer2VM)
    }

    interface OnItemClickListener {
        fun onItemClick(matchView: MatchView)
    }
}
