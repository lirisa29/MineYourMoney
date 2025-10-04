import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao //(Google Developers Training team, 2024)
interface UserDao { //(Google Developers Training team, 2024)
    @Insert  //(Google Developers Training team, 2024)
    fun insertUser(user: User): Long //(Google Developers Training team, 2024)

    // Login query
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1") //(Google Developers Training team, 2024)
    fun login(username: String, password: String): User? //(Google Developers Training team, 2024)

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1") //(Google Developers Training team, 2024)
    suspend fun findById(id: Int): User? //(Google Developers Training team, 2024)

    // Check if username exists
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1") //(Google Developers Training team, 2024)
    fun findByUsername(username: String): User? //(Google Developers Training team, 2024)
}
//Reference List:
/*(Google Developers Training team, 2024). Save data in a local database using Room. [Online].
Available at: https://developer.android.com/training/data-storage/room [Accessed 3 October 2025). */