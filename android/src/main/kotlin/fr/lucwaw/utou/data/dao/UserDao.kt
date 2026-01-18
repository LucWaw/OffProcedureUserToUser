package fr.lucwaw.utou.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import fr.lucwaw.utou.data.entity.UserEntity
import fr.lucwaw.utou.domain.modele.toEntity
import fr.lucwaw.utou.user.GrpcUser
import kotlinx.coroutines.flow.Flow
import kotlin.collections.map
import kotlin.time.Clock
import kotlin.time.Instant

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY cachedAt DESC")
    fun getUsers(): Flow<List<UserEntity>>

    @Query(value = "SELECT * FROM users WHERE id=:id")
    fun getUserById(id: Long): Flow<UserEntity?>

    @Query("SELECT userGUID FROM users WHERE isActualUser = 1 LIMIT 1")
    suspend fun getActualUserGUID(): String?

    @Query("UPDATE users SET isActualUser = 0")
    suspend fun clearLocalUserFlag()

    @Query("SELECT * FROM users WHERE userGUID IN (:uuids)")
    suspend fun getByRemoteUuids(uuids: List<String>): List<UserEntity>

    @Insert
    suspend fun insert(user: UserEntity)

    @Query(
        """
        UPDATE users
        SET name = :name,
            updatedAt = :updatedAt,
            cachedAt = :cachedAt
        WHERE userGUID = :userGUID
    """
    )
    suspend fun updateFromRemoteGUID(
        name: String,
        updatedAt: Instant,
        cachedAt: Instant,
        userGUID: String
    )

    @Query("""
        UPDATE users
        SET name = :name,
            updatedAt = :updatedAt,
            cachedAt = :cachedAt,
            userGUID = :userGUID
        WHERE id = :id
    """)
    suspend fun updateFromRemote(
        id: Long,
        name: String,
        updatedAt: Instant,
        cachedAt: Instant,
        userGUID: String?
    )

    @Transaction
    suspend fun refreshLww(remoteUsers: List<GrpcUser>) {
        val remoteUUIDs = remoteUsers.map { remoteUsers -> remoteUsers.userGUID }

        val localUsers = getByRemoteUuids(remoteUUIDs)
        val localMap = localUsers.associateBy { cachedUser -> cachedUser.userGUID }

        val actualUser = getActualUserGUID()

        remoteUsers.forEach { remote ->
            val remoteUserEntity = remote.toEntity(actualUser)
            val local = localMap[remote.userGUID]

            when {
                // User inconnu localement → INSERT
                local == null -> {
                    insert(
                        remoteUserEntity
                    )
                }

                // Comparaison LWW : mise à jour si le serveur est plus récent
                remoteUserEntity.updatedAt > local.updatedAt -> {
                    updateFromRemote(
                        id = local.id,
                        name = remoteUserEntity.name,
                        updatedAt = remoteUserEntity.updatedAt,
                        cachedAt = remoteUserEntity.cachedAt,
                        userGUID = remoteUserEntity.userGUID
                        //No need for isLocalUser because if its an update it is already here
                    )
                }

                // Local plus récent -> ignorer
                else -> Unit
            }
        }
    }

}