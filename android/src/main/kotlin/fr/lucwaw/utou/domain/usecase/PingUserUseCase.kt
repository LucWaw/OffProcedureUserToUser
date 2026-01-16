package fr.lucwaw.utou.domain.usecase

import fr.lucwaw.utou.data.repository.UserRepository
import fr.lucwaw.utou.domain.modele.SendPingResult
import javax.inject.Inject

class PingUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /**
     * Exécute un ping pour afficher une notification à un utilisateur.
     *
     * @param toUserId l'id de la cible
     * @return SendPingResult retourné par le repository
     */
    suspend fun execute(toUserId: String): SendPingResult {
        return userRepository.sendPing(toUserId)
    }
}