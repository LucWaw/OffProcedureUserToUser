package fr.lucwaw.utou.data.repository

import fr.lucwaw.utou.domain.modele.CreateUserResult
import fr.lucwaw.utou.domain.modele.SendPingResult
import fr.lucwaw.utou.domain.modele.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    val users: Flow<List<User>>

    suspend fun refreshUsers()

    suspend fun getActualUserGUID(): String?

    suspend fun registerUser(userName: String): Boolean

    fun scheduleRefresh()

    fun schedulePeriodicSync()

    fun scheduleUpdateToken()

    suspend fun registerDevice(generatedFcmToken: String)

    suspend fun sendPing(toUserGUID: String): SendPingResult
    suspend fun syncRegisteredUser(userName: String, userIdInput: Long): CreateUserResult
    fun blanck()
}
