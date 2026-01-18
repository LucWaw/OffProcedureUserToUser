package fr.lucwaw.utou.data.repository

import android.util.Log
import fr.lucwaw.utou.data.dao.UserDao
import fr.lucwaw.utou.data.entity.UserEntity
import fr.lucwaw.utou.domain.modele.CreateDeviceResult
import fr.lucwaw.utou.domain.modele.CreateUserResult
import fr.lucwaw.utou.domain.modele.SendPingResult
import fr.lucwaw.utou.domain.modele.SyncStatus
import fr.lucwaw.utou.domain.modele.User
import fr.lucwaw.utou.domain.modele.toDomain
import fr.lucwaw.utou.ping.PingServiceGrpcKt
import fr.lucwaw.utou.ping.sendPingRequest
import fr.lucwaw.utou.user.UserServiceGrpcKt
import fr.lucwaw.utou.user.createUserRequest
import fr.lucwaw.utou.user.listUsersRequest
import fr.lucwaw.utou.user.registerDeviceRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import utou.v1.Common
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant

class OffFirstUserRepository @Inject constructor(
    private val stub: UserServiceGrpcKt.UserServiceCoroutineStub,
    private val pingStub: PingServiceGrpcKt.PingServiceCoroutineStub,
    private val userDao: UserDao
) : UserRepository {

    override lateinit var lastTokenGenerated: String

    override val users: Flow<List<User>> =
        userDao.getUsers().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun refreshUsers() {
        try {
            val remoteUsers = stub.listUsers(listUsersRequest {})
            userDao.refreshLww(remoteUsers.usersList)
        } catch (e: Exception) {
            // silent fail, cache data stays visible
            Log.d("OffFirstRepository", "Error while syncing users, \n error :$e")
        }
    }

    override suspend fun getActualUserGUID(): String? {
        return userDao.getActualUserGUID()
    }


    override suspend fun registerUser(userName: String): CreateUserResult {
        val now = Clock.System.now()

        userDao.insert(
            UserEntity(
                id = 0L,
                userGUID = "",
                name = userName,
                cachedAt = now,
                updatedAt = now,
                syncStatus = SyncStatus.FALSE,
                isActualUser = true
            )
        )

        try {
            val response = stub.createUser(
                createUserRequest { displayName = userName }
            )
            if (response.status != Common.StatusCode.STATUS_OK) {
                throw IllegalStateException(response.message)
            }
            userDao.updateFromRemoteGUID(
                name = response.user.displayName,
                updatedAt = Instant.fromEpochMilliseconds(response.user.updatedAt),
                cachedAt = Clock.System.now(),
                userGUID = response.user.userGUID
            )
            registerDevice(lastTokenGenerated)

            return CreateUserResult(
                status = response.status,
                userId = response.user.userGUID,
                message = response.message
            )
        } catch (_: Exception) {
            return CreateUserResult(
                status = Common.StatusCode.STATUS_ERROR,
                userId = "",
                message = "Offline / pending sync"
            )
        }
    }

    override suspend fun registerDevice(generatedFcmToken: String): CreateDeviceResult {
        val generatedGUID = userDao.getActualUserGUID() ?: return CreateDeviceResult(
            Common.StatusCode.STATUS_ERROR,
            "actual userGUID Not Found"
        )
        if (generatedGUID.isBlank()) {
            return CreateDeviceResult(
                Common.StatusCode.STATUS_ERROR,
                "The user sending the ping needs to register online earlier."
            )
        }
        val response = stub.registerDevice(
            registerDeviceRequest {
                userId = generatedGUID
                fcmToken = generatedFcmToken
            })

        return CreateDeviceResult(
            status = response.status, message = response.message
        )
    }

    override suspend fun sendPing(toUserGUID: String): SendPingResult {
        val generatedGUID = userDao.getActualUserGUID() ?: return SendPingResult(
            Common.StatusCode.STATUS_ERROR,
            "actual userGUID Not Found"
        )
        if (generatedGUID.isBlank()) {
            return SendPingResult(
                Common.StatusCode.STATUS_ERROR,
                "The user sending the ping needs to register online earlier."
            )
        }

        val request = sendPingRequest {
            this.fromUserId = generatedGUID
            this.toUserId = toUserGUID
        }

        val response = pingStub.sendPing(request)

        return SendPingResult(
            status = response.status,
            message = response.message
        )
    }
}
