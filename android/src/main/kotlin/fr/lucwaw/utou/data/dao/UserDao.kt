package fr.lucwaw.utou.data.dao

import androidx.room.Dao
import androidx.room.Query
import fr.lucwaw.utou.data.entity.UserEntity
import fr.lucwaw.utou.domain.modele.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY cachedAt DESC")
    fun getUsers(): Flow<List<UserEntity>>

    @Query(value = "SELECT * FROM users WHERE userId=:userId")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT userId FROM users WHERE isLocalUser = 1 LIMIT 1")
    suspend fun getLocalUserId(): String?

    @Query(
        """
    UPDATE users
    SET name = :name,
        cachedAt = :cachedAt,
        syncStatus = :syncStatus,
        isLocalUser = :isLocalUser
    WHERE userId = :userId
    """
    )
    suspend fun updateUser(
        userId: String,
        name: String,
        cachedAt: Instant,
        syncStatus: SyncStatus,
        isLocalUser: Boolean
    )

    @Query("UPDATE users SET isLocalUser = 0")
    suspend fun clearLocalUserFlag()
}