package fr.lucwaw.utou.domain

import fr.lucwaw.utou.data.repository.UserRepository
import fr.lucwaw.utou.user.User
import javax.inject.Inject


class GetUsersUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend fun execute(): List<User> = repository.getUsers()
}
