package fr.lucwaw.utou.user.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.lucwaw.utou.domain.GetUsersUseCase
import fr.lucwaw.utou.domain.PingUserUseCase
import fr.lucwaw.utou.user.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import utou.v1.Common
import javax.inject.Inject


@HiltViewModel
class UsersViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase,
    private val pingUserUseCase: PingUserUseCase
) : ViewModel() {
    private val _usersFlow = MutableStateFlow<List<User>>(emptyList())
    val usersFlow: StateFlow<List<User>> = _usersFlow.asStateFlow()


    init {
        loadAllUsers()
    }

    fun loadAllUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            val users = getUsersUseCase.execute()
            _usersFlow.value = users
        }
    }

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent

    fun sendPing(toUserId: String) {
        viewModelScope.launch {
            val result = pingUserUseCase.execute( toUserId)

            val toast = when(result.status) {
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