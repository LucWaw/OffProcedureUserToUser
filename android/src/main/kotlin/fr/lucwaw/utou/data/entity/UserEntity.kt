package fr.lucwaw.utou.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import fr.lucwaw.utou.domain.modele.SyncStatus
import kotlin.time.Instant


@Entity(tableName = "users", indices = [Index(value = ["userId"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "userId") val userId: String?,
    val name: String,
    val cachedAt: Instant,
    val syncStatus: SyncStatus,
    val isLocalUser: Boolean
)