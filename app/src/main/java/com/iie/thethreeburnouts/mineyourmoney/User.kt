import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "users") //(Google Developers Training team, 2024)
data class User( //(Google Developers Training team, 2024)
    @PrimaryKey(autoGenerate = true) val id: Int = 0, //(Google Developers Training team, 2024)
    val username: String, //(Google Developers Training team, 2024)
    val password: String //(Google Developers Training team, 2024)
) : Parcelable

//Reference List:
/* Google Developers Training team. 2024. Save data in a local database using Room. [Online].
Available at: https://developer.android.com/training/data-storage/room [Accessed 3 October 2025). */