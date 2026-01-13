package fr.lucwaw.utou.user.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.lucwaw.utou.domain.RegisterUserUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import utou.v1.Common
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUserUseCase: RegisterUserUseCase
): ViewModel(){
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    fun register(userName: String) {
        viewModelScope.launch {
            val result = registerUserUseCase.execute(userName)
            val message = when(result.status) {
                Common.StatusCode.STATUS_OK -> "Utilisateur créé"
                Common.StatusCode.STATUS_ALREADY_EXISTS -> "Utilisateur déjà existant"
                else -> "Erreur"
            }
            _toastEvent.emit(message)
        }
    }
}