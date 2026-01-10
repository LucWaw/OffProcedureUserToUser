import fr.lucwaw.utou.ping.FetchMissedRequest
import fr.lucwaw.utou.ping.PingList
import fr.lucwaw.utou.ping.PingMessage
import fr.lucwaw.utou.ping.PingServiceGrpcKt
import fr.lucwaw.utou.ping.SendPingRequest
import fr.lucwaw.utou.ping.SendPingResponse
import fr.lucwaw.utou.ping.SubscribeRequest
import fr.lucwaw.utou.user.CreateUserRequest
import fr.lucwaw.utou.user.CreateUserResponse
import fr.lucwaw.utou.user.ListUsersRequest
import fr.lucwaw.utou.user.ListUsersResponse
import fr.lucwaw.utou.user.RegisterDeviceRequest
import fr.lucwaw.utou.user.RegisterDeviceResponse
import fr.lucwaw.utou.user.User
import fr.lucwaw.utou.user.UserServiceGrpcKt
import fr.lucwaw.utou.user.createUserResponse
import fr.lucwaw.utou.user.listUsersResponse
import fr.lucwaw.utou.user.registerDeviceResponse
import io.grpc.Server
import io.grpc.ServerBuilder
import kotlinx.coroutines.flow.Flow
import utou.v1.Common
import java.util.UUID


class UserPingServer(private val port: Int) {
    val listOfUsers : MutableSet<User> = mutableSetOf()
    private val devices = mutableMapOf<String, MutableMap<String, String>>()

    val server: Server =
        ServerBuilder.forPort(port)
            .addService(UserToUserService(listOfUsers, devices))
            .addService(PingService())
            .build()

    fun start() {
        server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime()
            .addShutdownHook(
                Thread {
                    println("*** shutting down gRPC server since JVM is shutting down")
                    this@UserPingServer.stop()
                    println("*** server shut down")
                },
            )
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }

    internal class UserToUserService(private val listOfUsers: MutableSet<User>, private val devices: MutableMap<String, MutableMap<String, String>>) : UserServiceGrpcKt.UserServiceCoroutineImplBase() {
        override suspend fun createUser(request: CreateUserRequest): CreateUserResponse {
            var statusCode = Common.StatusCode.STATUS_OK
            val user = User.newBuilder().setUserId(UUID.randomUUID().toString()).setDisplayName(request.displayName).build()
            if (!listOfUsers.contains(user)){
                listOfUsers.add(user)
            } else {
                statusCode = Common.StatusCode.STATUS_ALREADY_EXISTS
            }

            return createUserResponse {
                this.status = statusCode
                this.userId = user.userId.toString()
                this.message = statusCode.toString()
            }
        }

        override suspend fun listUsers(request: ListUsersRequest): ListUsersResponse {
            return listUsersResponse {
                this.users.addAll(listOfUsers)
            }
        }

        override suspend fun registerDevice(request: RegisterDeviceRequest): RegisterDeviceResponse {
            val userId = request.userId
            val deviceId = request.deviceId
            val token = request.fcmToken

            // --- validations de base ---
            if (userId.isBlank() || deviceId.isBlank() || token.isBlank()) {
                return registerDeviceResponse {
                    status = Common.StatusCode.STATUS_ERROR
                    message = "Invalid arguments: userId, deviceId and fcmToken must not be empty."
                }
            }

            // --- utilisateur existe ? ---
            if (listOfUsers.find { it.userId == userId } == null) {
                return registerDeviceResponse {
                    status = Common.StatusCode.STATUS_NOT_FOUND
                    message = "User not found"
                }
            }

            // --- récupération ou création du registre des devices de cet utilisateur ---
            val userDevices = devices.getOrPut(userId) { mutableMapOf() }

            // --- device déjà enregistré ? ---
            val alreadyExists = userDevices.containsKey(deviceId)

            // update / insert
            userDevices[deviceId] = token

            return registerDeviceResponse {
                status = if (alreadyExists) {
                    Common.StatusCode.STATUS_ALREADY_EXISTS
                } else {
                    Common.StatusCode.STATUS_OK
                }

                message = if (alreadyExists) {
                    "Device already registered. Token updated."
                } else {
                    "Device successfully registered."
                }
            }
        }
    }

    internal class PingService() : PingServiceGrpcKt.PingServiceCoroutineImplBase() {
        override suspend fun fetchMissedPings(request: FetchMissedRequest): PingList {
            return super.fetchMissedPings(request)
        }

        override suspend fun sendPing(request: SendPingRequest): SendPingResponse {
            return super.sendPing(request)
        }

        override fun subscribePings(request: SubscribeRequest): Flow<PingMessage> {
            return super.subscribePings(request)
        }
    }
}

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 50052
    val server = UserPingServer(port)
    server.start()
    server.blockUntilShutdown()
}