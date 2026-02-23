package fr.lucwaw.utou.domain.usecase

import fr.lucwaw.utou.data.repository.UserRepository
import javax.inject.Inject

class ScheduleOneTimeRefreshUseCase @Inject constructor(
    private val repo: UserRepository
){
    operator fun invoke() = repo.scheduleRefresh()
}