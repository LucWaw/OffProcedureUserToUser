package fr.lucwaw.utou.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import fr.lucwaw.utou.domain.modele.SyncStatus
import kotlin.time.Instant


@Entity(tableName = "users", indices = [Index(value = ["userGUID"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "userGUID") val userGUID: String?,
    val name: String,
    val cachedAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncStatus,
    val isActualUser: Boolean
)