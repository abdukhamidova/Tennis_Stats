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
import com.anw.tenistats.PlayerDetailsActivity
import com.anw.tenistats.PlayerView
import com.anw.tenistats.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class MyAdapterPlayer(
    private val originalList: List<PlayerView>,
    private var firebaseAuth: FirebaseAuth,
) : RecyclerView.Adapter<MyAdapterPlayer.MyViewHolder>(), Filterable {

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

        // Ustawienie ikony przycisku w zależności od stanu isFavorite
        database.child("Players").child(currentItem.player).child("isFavorite")
            .get() // Wykonujemy zapytanie do bazy o wartość isFavorite
            .addOnSuccessListener { snapshot ->
                val isFavoriteInDatabase = snapshot.getValue(Boolean::class.java) ?: false
                if (isFavoriteInDatabase) {
                    holder.buttonAddToTeam.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.star_full, 0)
                } else {
                    holder.buttonAddToTeam.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.star, 0)
                }
            }

        // Kliknięcie na element, aby otworzyć szczegóły zawodnika
        holder.itemView.setOnClickListener {
            listener?.onItemClick(currentItem)
            val intent = Intent(context, PlayerDetailsActivity::class.java)
            intent.putExtra("playerId", currentItem.player)
            context.startActivity(intent)
        }

        // Kliknięcie na przycisk "Add to Team"
        holder.buttonAddToTeam.setOnClickListener {
            // Odczytujemy status isFavorite z bazy
            database.child("Players").child(currentItem.player).child("isFavorite")
                .get()
                .addOnSuccessListener { snapshot ->
                    val isFavoriteInDatabase = snapshot.getValue(Boolean::class.java) ?: false

                    if (isFavoriteInDatabase) {
                        // Jeśli zawodnik jest już w ulubionych, to usuwamy go
                        database.child("Players").child(currentItem.player).child("isFavorite").setValue(false)
                            .addOnSuccessListener {
                                // Usuwamy z sekcji "Teams->Favorite->players"
                                database.child("Teams").child("Favorites").child("players").child(currentItem.player).removeValue()
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "${currentItem.player} removed from favorites", Toast.LENGTH_SHORT).show()
                                        holder.buttonAddToTeam.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.star, 0)
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Failed to remove ${currentItem.player} from the Favorite team", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to remove ${currentItem.player} from favorites", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // Jeśli zawodnik nie jest w ulubionych, to dodajemy go
                        database.child("Players").child(currentItem.player).child("isFavorite").setValue(true)
                            .addOnSuccessListener {
                                // Dodajemy do sekcji "Teams->Favorite->players"
                                database.child("Teams").child("Favorites").child("players").child(currentItem.player).setValue(true)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "${currentItem.player} added to favorites", Toast.LENGTH_SHORT).show()
                                        holder.buttonAddToTeam.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.star_full, 0)
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Failed to add ${currentItem.player} to the Favorite team", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to add ${currentItem.player} to favorites", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to retrieve favorite status for ${currentItem.player}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                // Przygotuj listę, która będzie wynikiem filtrowania
                val filteredResults: List<PlayerView> = if (constraint.isNullOrEmpty()) {
                    originalList.filter { it.active }  // Tylko aktywni gracze
                } else {
                    val filterPattern = constraint.toString().toLowerCase(Locale.getDefault()).trim()

                    // Zastosowanie filtra na imieniu i nazwisku zawodnika
                    originalList.filter {
                        it.firstName.toLowerCase(Locale.getDefault()).contains(filterPattern) ||
                                it.lastName.toLowerCase(Locale.getDefault()).contains(filterPattern)
                    }
                }
                // Upewnij się, że w wyniku filtrowania nie ma duplikatów
                return FilterResults().apply {
                    values = filteredResults.distinct()  // Usuwamy duplikaty, jeżeli występują
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<PlayerView> ?: emptyList()
                notifyDataSetChanged()  // Aktualizujemy widok
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
