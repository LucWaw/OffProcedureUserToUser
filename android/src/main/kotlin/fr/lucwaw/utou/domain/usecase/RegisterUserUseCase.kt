package fr.lucwaw.utou.domain.usecase

import fr.lucwaw.utou.data.repository.UserRepository
import fr.lucwaw.utou.domain.modele.CreateUserResult
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /**
     * Exécute l'enregistrement d'un utilisateur.
     *
     * @param userName le nom à enregistrer
     * @return CreateUserResult retourné par le repository
     */
    suspend fun execute(userName: String): Boolean {
        return userRepository.registerUser(userName)
    }
}
