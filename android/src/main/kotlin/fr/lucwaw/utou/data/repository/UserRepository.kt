package fr.lucwaw.utou.data.repository

import fr.lucwaw.utou.domain.modele.CreateDeviceResult
import fr.lucwaw.utou.domain.modele.CreateUserResult
import fr.lucwaw.utou.domain.modele.SendPingResult
import fr.lucwaw.utou.user.User

interface UserRepository {

    val generatedUserId: String
    var lastTokenGenerated: String

    suspend fun getUsers(): List<User>

    suspend fun registerUser(userName: String): CreateUserResult

    suspend fun registerDevice(generatedFcmToken: String): CreateDeviceResult

    suspend fun sendPing(toUserId: String): SendPingResult
}
