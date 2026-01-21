package fr.lucwaw.utou.data.repository

import fr.lucwaw.utou.domain.modele.CreateDeviceResult
import fr.lucwaw.utou.domain.modele.CreateUserResult
import fr.lucwaw.utou.domain.modele.SendPingResult
import fr.lucwaw.utou.domain.modele.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    val users: Flow<List<User>>

    var lastTokenGenerated: String

    suspend fun refreshUsers()

    suspend fun getActualUserGUID(): String?

    suspend fun registerUser(userName: String)

    fun scheduleRefresh()

    fun schedulePeriodicSync()

    fun scheduleUpdateToken()

    suspend fun registerDevice(generatedFcmToken: String): CreateDeviceResult

    suspend fun sendPing(toUserGUID: String): SendPingResult
    suspend fun syncRegisteredUser(userName: String): CreateUserResult
}
