import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Insert
    fun insertUser(user: User)

    // Login query
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    fun login(username: String, password: String): User?

    // Check if username exists
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    fun findByUsername(username: String): User?
}
