package fr.lucwaw.utou.domain

import fr.lucwaw.utou.data.repository.UserRepository
import javax.inject.Inject


class GetUsersFlowUseCase @Inject constructor(
    private val repo: UserRepository
){
    operator fun invoke() = repo.usersFlow
}

class RefreshUsersUseCase @Inject constructor(
    private val repo: UserRepository
){
    suspend operator fun invoke() = repo.refreshUsers()
}
