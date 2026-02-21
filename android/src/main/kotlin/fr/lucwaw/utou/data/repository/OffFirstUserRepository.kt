package fr.lucwaw.utou.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import fr.lucwaw.utou.data.dao.UserDao
import fr.lucwaw.utou.data.entity.UserEntity
import fr.lucwaw.utou.data.workers.SyncScheduler
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
import kotlinx.coroutines.tasks.await
import utou.v1.Common
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant

class OffFirstUserRepository @Inject constructor(
    private val stub: UserServiceGrpcKt.UserServiceCoroutineStub,
    private val pingStub: PingServiceGrpcKt.PingServiceCoroutineStub,
    private val userDao: UserDao,
    private val syncScheduler: SyncScheduler
) : UserRepository {

    override val users: Flow<List<User>> =
        userDao.getUsers().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun scheduleRefresh() {
        syncScheduler.scheduleOnetimeRefreshSync()
    }

    override fun schedulePeriodicSync() {
        syncScheduler.schedulePeriodicSync()
    }

    override fun scheduleUpdateToken() {
        syncScheduler.scheduleUpdateToken()
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

    override suspend fun syncRegisteredUser(userName: String, userIdInput: Long): CreateUserResult {
        val response = stub.createUser(
            createUserRequest { displayName = userName }
        )
        if (response.status != Common.StatusCode.STATUS_OK) {
            throw IllegalStateException(response.message)
        }
        userDao.updateFromId(
            name = userName,
            updatedAt = Instant.fromEpochMilliseconds(response.user.updatedAt),
            cachedAt = Clock.System.now(),
            userGUID = response.user.userGUID,
            syncStatus = SyncStatus.SYNCED,
            id = userIdInput
        )
        registerDevice( Firebase.messaging.token.await())

        return CreateUserResult(
            status = response.status,
            userId = response.user.userGUID,
            message = response.message
        )

    }

    override suspend fun registerUser(userName: String): Boolean {
        val now = Clock.System.now()

        val result = userDao.insert(
            UserEntity(
                id = 0L,
                userGUID = "",
                name = userName,
                cachedAt = now,
                updatedAt = now,
                syncStatus = SyncStatus.PENDING_UPLOAD,
                isActualUser = true
            )
        )

        syncScheduler.scheduleOneTimeRegisterSync(result, userName)

        return result != -1L
    }

    override suspend fun registerDevice(generatedFcmToken: String) {
        val generatedGUID = userDao.getActualUserGUID() ?: throw IllegalStateException("pas d'user actuel")
        if (generatedGUID.isBlank()) {
            throw IllegalStateException("user actuel sans guid")
        }
        stub.registerDevice(
            registerDeviceRequest {
                userId = generatedGUID
                fcmToken = generatedFcmToken
            })
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

    override fun blanck() {
        println("dc")
    }
}
