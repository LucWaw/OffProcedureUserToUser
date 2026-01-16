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
import fr.lucwaw.utou.domain.modele.toEntity
import fr.lucwaw.utou.ping.PingServiceGrpcKt
import fr.lucwaw.utou.ping.sendPingRequest
import fr.lucwaw.utou.user.RegisterDeviceResponse
import fr.lucwaw.utou.user.UserServiceGrpcKt
import fr.lucwaw.utou.user.createUserRequest
import fr.lucwaw.utou.user.listUsersRequest
import fr.lucwaw.utou.user.registerDeviceRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import utou.v1.Common
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Clock

class OffFirstUserRepository @Inject constructor(
    private val stub: UserServiceGrpcKt.UserServiceCoroutineStub,
    private val pingStub: PingServiceGrpcKt.PingServiceCoroutineStub,
    private val userDao: UserDao
) : UserRepository {

    val users: Flow<List<User>> =
        userDao.getUsers().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun refreshUsers() {
        try {
            val localUserId = userDao.getLocalUserId()

            val remoteUsers = stub.listUsers(listUsersRequest {})

            val entities = remoteUsers.usersList.map {
                it.toEntity(localUserId)
            }

            userDao.insertUsers(entities)
        } catch (e: Exception) {
            // silent fail, cache data stays visible
            Log.d("OffFirstRepository", "Error while syncing users, \n error :$e")
        }
    }


    override suspend fun registerUser(userName: String): CreateUserResult {
        val tempId = UUID.randomUUID().toString()
        val tempUser = UserEntity(
            userId = tempId,
            name = userName,
            cachedAt = Clock.System.now(),
            syncStatus = SyncStatus.FALSE,
            isLocalUser = true
        )
        userDao.insertUser(tempUser)

        try {
            val response = stub.createUser(
                createUserRequest { displayName = userName }
            )

            val updatedUser = tempUser.copy(
                userId = response.user.userId,
                syncStatus = SyncStatus.SYNCED
            )
            userDao.updateUser(updatedUser)

            return CreateUserResult(
                status = response.status,
                userId = response.user.userId,
                message = response.message
            )
        } catch (e: Exception) {
            return CreateUserResult(
                status = Common.StatusCode.STATUS_ERROR,
                userId = tempId,
                message = "Offline / pending sync"
            )
        }
    }

    override suspend fun registerDevice(generatedFcmToken: String): CreateDeviceResult {
        val response = stub.registerDevice(
            registerDeviceRequest {
                userId = generatedUserId
                fcmToken = generatedFcmToken
            })
        return CreateDeviceResult(
            status = response.status, message = response.message
        )
    }

    override suspend fun sendPing(toUserId: String): SendPingResult {
        if(generatedUserId.isBlank()){
            return SendPingResult(
                Common.StatusCode.STATUS_ERROR,
                "The user sending the ping needs to register earlier."
            )
        }

        val request = sendPingRequest {
            this.fromUserId = generatedUserId
            this.toUserId = toUserId
        }

        val response = pingStub.sendPing(request)

        return SendPingResult(
            status = response.status,
            message = response.message
        )
    }
}
