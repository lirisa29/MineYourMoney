import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao //(Google Developers Training team, 2025)
interface UserDao { //(Google Developers Training team, 2025)
    @Insert  //(Google Developers Training team, 2025)
    fun insertUser(user: User): Long //(Google Developers Training team, 2025)

    // Login query
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1") //(Google Developers Training team, 2025)
    fun login(username: String, password: String): User? //(Google Developers Training team, 2025)

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1") //(Google Developers Training team, 2025)
    suspend fun findById(id: Int): User? //(Google Developers Training team, 2025)

    // Check if username exists
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1") //(Google Developers Training team, 2025)
    fun findByUsername(username: String): User? //(Google Developers Training team, 2025)
}
//Reference List:
/*(Google Developers Training team, 2025). Save data in a local database using Room. [Online].
Available at: https://developer.android.com/training/data-storage/room [Accessed 3 October 2025). */