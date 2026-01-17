package fr.lucwaw.utou.domain.modele

import fr.lucwaw.utou.data.entity.UserEntity
import fr.lucwaw.utou.user.GrpcUser
import kotlin.time.Clock
import kotlin.time.Instant

data class User(
    val id: Long,
    val userGUID: String?,
    val name: String,
    val cachedAt: Instant,
    val updatedAt: Instant,
    val syncStatus: SyncStatus,
    val isActualUser: Boolean
)

fun UserEntity.toDomain(): User {
    return User(
        id = this.id,
        userGUID = this.userGUID,
        name = this.name,
        cachedAt = this.cachedAt,
        updatedAt = this.updatedAt,
        syncStatus = this.syncStatus,
        isActualUser = this.isActualUser
    )
}

fun GrpcUser.toEntity(localUserGUID: String? = null): UserEntity {
    val now = Clock.System.now()
    return UserEntity(
        id = 0L,
        userGUID = this.userGUID,
        name = this.displayName,
        cachedAt = now,
        updatedAt = Instant.fromEpochMilliseconds(this.updatedAt),
        syncStatus = SyncStatus.SYNCED,
        isActualUser = localUserGUID != null && localUserGUID == this.userGUID
    )
}
