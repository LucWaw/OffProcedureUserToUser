package fr.lucwaw.utou.data.repository

import fr.lucwaw.utou.user.User
import fr.lucwaw.utou.user.UserServiceGrpcKt
import fr.lucwaw.utou.user.listUsersRequest
import javax.inject.Inject

class GrpcUserRepository @Inject constructor(
    private val stub: UserServiceGrpcKt.UserServiceCoroutineStub
) : UserRepository {

    override suspend fun getUsers(): List<User> {
        val response = stub.listUsers(
            listUsersRequest {
                limit = 100
                offset = 0
            }
        )
        return response.usersList
    }
}
