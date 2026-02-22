package fr.lucwaw.utou.user.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.lucwaw.utou.domain.usecase.GetUsersFlowUseCase
import fr.lucwaw.utou.domain.usecase.PingUserUseCase
import fr.lucwaw.utou.domain.usecase.ScheduleOneTimeRefreshUseCase
import fr.lucwaw.utou.domain.usecase.SchedulePeriodicRefreshUseCase
import fr.lucwaw.utou.domain.usecase.ScheduleUpdateToken
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import utou.v1.Common
import javax.inject.Inject


@HiltViewModel
class UsersViewModel @Inject constructor(
    getUsersFlowUseCase: GetUsersFlowUseCase,
    private val schedulePeriodicRefreshUseCase: SchedulePeriodicRefreshUseCase,
    private val scheduleOneTimeRefreshUseCase: ScheduleOneTimeRefreshUseCase,
    private val scheduleUpdateToken: ScheduleUpdateToken,
    private val pingUserUseCase: PingUserUseCase
) : ViewModel() {
    val usersFlow = getUsersFlowUseCase()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()


    fun refresh() {
        _isRefreshing.value = true
        scheduleOneTimeRefreshUseCase()
        _isRefreshing.value = false
    }


    fun periodicRefresh(){
        schedulePeriodicRefreshUseCase()
    }

    fun updateToken(){
        scheduleUpdateToken()
    }

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent

    fun sendPing(toUserId: String) {
        viewModelScope.launch {
            val result = pingUserUseCase.execute(toUserId)

            val toast = when (result.status) {
                Common.StatusCode.STATUS_OK ->
                    "Ping envoyé"

                Common.StatusCode.STATUS_NOT_FOUND ->
                    "Utilisateur introuvable"

                else ->
                    "Erreur lors de l’envoi du ping"
            }

            _toastEvent.emit(toast)
        }
    }
}