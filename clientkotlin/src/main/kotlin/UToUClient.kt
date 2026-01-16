import fr.lucwaw.utou.ping.PingServiceGrpcKt
import fr.lucwaw.utou.ping.sendPingRequest
import fr.lucwaw.utou.user.*
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.runBlocking
import java.io.Closeable

class UserPingClient(
    private val channel: ManagedChannel
) : Closeable
{

    private val userStub = UserServiceGrpcKt.UserServiceCoroutineStub(channel)
    private val pingStub = PingServiceGrpcKt.PingServiceCoroutineStub(channel)

    // --- USER ---
    suspend fun createUser(displayName: String): String {
        val request = createUserRequest { this.displayName = displayName }
        val response = userStub.createUser(request)
        println("CreateUser status=${response.status}, userId=${response.user.userId}")
        return response.user.userId
    }

    suspend fun listUsers(): List<GrpcUser> {
        val request = listUsersRequest {}
        val response = userStub.listUsers(request)
        println("ListUsers: ${response.usersList}")
        return response.usersList
    }

    suspend fun registerDevice(userId: String, fcmToken: String) {
        val request = registerDeviceRequest {
            this.userId = userId
            this.fcmToken = fcmToken
        }
        val response = userStub.registerDevice(request)
        println("RegisterDevice status=${response.status}, message=${response.message}")
    }

    // --- PING ---
    suspend fun sendPing(fromUserId: String, toUserId: String) {
        val request = sendPingRequest {
            this.fromUserId = fromUserId
            this.toUserId = toUserId
        }
        val response = pingStub.sendPing(request)
        println("SendPing status=${response.status}, fromUserId=${response.fromUserId}, toUserId=${response.toUserId}")
    }

    override fun close() {
        channel.shutdown()
            .awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)
    }
}

// --- MAIN ---
fun main() = runBlocking {
    val port = System.getenv("PORT")?.toInt() ?: 50052
    val channel = ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build()

    UserPingClient(channel).use { client ->
        val userIdA = client.createUser("Alice")
        val userIdB = client.createUser("Bob")

        client.registerDevice(userIdA,  "tokenA")
        client.registerDevice(userIdB, "tokenB")

        client.listUsers()

        // Send a ping from Alice to Bob
        client.sendPing(userIdA, userIdB)
    }
}
