package fr.lucwaw.utou.data.repository

import fr.lucwaw.utou.domain.modele.CreateDeviceResult
import fr.lucwaw.utou.domain.modele.CreateUserResult
import fr.lucwaw.utou.domain.modele.SendPingResult
import fr.lucwaw.utou.user.User
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {

    val generatedUserId: String
    var lastTokenGenerated: String

    val usersFlow: StateFlow<List<User>>
    suspend fun refreshUsers()

    suspend fun registerUser(userName: String): CreateUserResult

    suspend fun registerDevice(generatedFcmToken: String): CreateDeviceResult

    suspend fun sendPing(toUserId: String): SendPingResult
}
