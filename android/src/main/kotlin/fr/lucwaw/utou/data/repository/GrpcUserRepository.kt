package fr.lucwaw.utou.data.repository

import fr.lucwaw.utou.domain.modele.CreateDeviceResult
import fr.lucwaw.utou.domain.modele.CreateUserResult
import fr.lucwaw.utou.domain.modele.SendPingResult
import fr.lucwaw.utou.ping.PingServiceGrpcKt
import fr.lucwaw.utou.ping.sendPingRequest
import fr.lucwaw.utou.user.RegisterDeviceResponse
import fr.lucwaw.utou.user.User
import fr.lucwaw.utou.user.UserServiceGrpcKt
import fr.lucwaw.utou.user.createUserRequest
import fr.lucwaw.utou.user.listUsersRequest
import fr.lucwaw.utou.user.registerDeviceRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import utou.v1.Common
import javax.inject.Inject

class GrpcUserRepository @Inject constructor(
    private val stub: UserServiceGrpcKt.UserServiceCoroutineStub,
    private val pingStub: PingServiceGrpcKt.PingServiceCoroutineStub
) : UserRepository {

    override var generatedUserId: String = ""
    override var lastTokenGenerated: String = ""

    private val _users = MutableStateFlow<List<User>>(emptyList())
    override val usersFlow: StateFlow<List<User>> get() = _users

    override suspend fun refreshUsers() {
        val response = stub.listUsers(
            listUsersRequest {
                limit = 100
                offset = 0
            }
        )
        _users.value = response.usersList
    }

    override suspend fun registerUser(userName: String): CreateUserResult {
        val response = stub.createUser(
            createUserRequest {
                displayName = userName
            })
        generatedUserId = response.userId

        var registerDeviceResponse: RegisterDeviceResponse? = null
        if (lastTokenGenerated.isNotBlank()) {
            registerDeviceResponse = stub.registerDevice(
                registerDeviceRequest {
                    userId = generatedUserId
                    fcmToken = lastTokenGenerated
                })
        }

        return CreateUserResult(
            status = response.status,
            userId = response.userId,
            message = response.message,
            registerDeviceResponse?.status ?: Common.StatusCode.UNRECOGNIZED,
            registerDeviceResponse?.message ?: "No token generated"

        )
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
