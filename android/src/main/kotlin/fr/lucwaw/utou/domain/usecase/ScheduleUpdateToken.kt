package fr.lucwaw.utou.domain.usecase

import fr.lucwaw.utou.data.repository.UserRepository
import javax.inject.Inject

class ScheduleUpdateToken @Inject constructor(
    private val repo: UserRepository
){
    operator fun invoke() = repo.scheduleUpdateToken()
}