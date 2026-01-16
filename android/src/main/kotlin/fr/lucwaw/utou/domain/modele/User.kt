package fr.lucwaw.utou.domain.modele

import fr.lucwaw.utou.data.entity.UserEntity
import fr.lucwaw.utou.user.GrpcUser
import kotlin.time.Clock
import kotlin.time.Instant

data class User(
    val userId: String?,
    val name: String,
    val cachedAt: Instant,
    val syncStatus: SyncStatus
)

fun UserEntity.toDomain(): User {
    return User(
        userId = this.userId,
        name = this.name,
        cachedAt = this.cachedAt,
        syncStatus = this.syncStatus
    )
}

fun GrpcUser.toEntity(localUserId: String? = ""): UserEntity {
    return UserEntity(
        id = 0L,
        userId = this.userId,
        name = this.displayName,
        cachedAt = Clock.System.now(),
        syncStatus = SyncStatus.SYNCED,
        isLocalUser = localUserId == this.userId,
    )
}
