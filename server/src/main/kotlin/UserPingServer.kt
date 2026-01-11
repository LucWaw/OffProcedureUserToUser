import fr.lucwaw.utou.ping.PingServiceGrpcKt
import fr.lucwaw.utou.ping.SendPingRequest
import fr.lucwaw.utou.ping.SendPingResponse
import fr.lucwaw.utou.ping.sendPingResponse
import fr.lucwaw.utou.user.*
import io.grpc.Server
import io.grpc.ServerBuilder
import utou.v1.Common
import java.util.*


class UserPingServer(private val port: Int) {
    val listOfUsers: MutableSet<User> = mutableSetOf()
    private val devices: MutableMap<String, String> = mutableMapOf()  // userId -> deviceId


    val server: Server =
        ServerBuilder.forPort(port)
            .addService(UserToUserService(listOfUsers, devices))
            .addService(PingService(listOfUsers, devices))
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

    internal class UserToUserService(private val listOfUsers: MutableSet<User>, private val devices: MutableMap<String, String>) : UserServiceGrpcKt.UserServiceCoroutineImplBase() {
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

        /**
         * Here, for simplicity, we have chosen that a user have only one device, so we don't store deviceId
         */
        override suspend fun registerDevice(request: RegisterDeviceRequest): RegisterDeviceResponse {
            val userId = request.userId
            val token = request.fcmToken

            // --- validations de base ---
            if (userId.isBlank() || token.isBlank()) {
                return registerDeviceResponse {
                    status = Common.StatusCode.STATUS_ERROR
                    message = "Invalid arguments: userId, deviceId and fcmToken must not be empty."
                }
            }

            // L'utilisateur existe-t-il ?
            val userExists = listOfUsers.any { it.userId == userId }
            if (!userExists) {
                return registerDeviceResponse {
                    status = Common.StatusCode.STATUS_NOT_FOUND
                    message = "User not found"
                }
            }

            // Enregistrement / mise à jour du device
            val alreadyExists = devices.containsKey(userId)
            devices[userId] = token

            return registerDeviceResponse {
                status = if (alreadyExists) Common.StatusCode.STATUS_ALREADY_EXISTS else Common.StatusCode.STATUS_OK
                message = if (alreadyExists) "Device updated" else "Device registered"
            }
        }
        }
    }

internal class PingService(
    private val listOfUsers: MutableSet<User>,
    private val devices: MutableMap<String, String>
) : PingServiceGrpcKt.PingServiceCoroutineImplBase() {

    override suspend fun sendPing(request: SendPingRequest): SendPingResponse {
        println("Send Ping")
        val userId = request.toUserId

        // Vérifier que l'utilisateur existe et qu'il a un device
        val userExists = listOfUsers.any { it.userId == userId }
        val deviceExists = devices.containsKey(userId)

        val status = if (!userExists || !deviceExists) {
            Common.StatusCode.STATUS_NOT_FOUND
        } else {
            Common.StatusCode.STATUS_OK
        }

        return sendPingResponse {
            this.fromUserId = request.fromUserId
            this.toUserId = request.toUserId
            this.status = status
            this.message = if (status == Common.StatusCode.STATUS_OK) "Ping sent" else "User or device not found"
        }
    }
}

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 50052
    val server = UserPingServer(port)
    server.start()
    server.blockUntilShutdown()
}