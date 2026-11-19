package com.wallet.mywallet.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.mywallet.R
import com.wallet.mywallet.auth.AuthRepository
import com.wallet.mywallet.auth.AuthResult
import com.wallet.mywallet.ui.common.UiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Logs the user in. On success nothing is navigated here: MainActivity watches the session
 * and opens the home screen by itself.
 */
class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun login(email: String, password: String) {
        val cleanEmail = email.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) return send(R.string.error_invalid_email)
        if (password.isEmpty()) return send(R.string.error_auth_invalid_credentials)
        if (_loading.value) return

        viewModelScope.launch {
            _loading.value = true
            val result = authRepository.signIn(cleanEmail, password)
            _loading.value = false
            if (result is AuthResult.Failure) _events.send(UiEvent.ShowMessage(result.error.messageRes()))
        }
    }

    fun resetPassword(email: String) {
        val cleanEmail = email.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) return send(R.string.error_invalid_email)
        viewModelScope.launch {
            val result = authRepository.sendPasswordReset(cleanEmail)
            _events.send(
                UiEvent.ShowMessage(
                    if (result is AuthResult.Failure) result.error.messageRes() else R.string.reset_link_sent
                )
            )
        }
    }

    private fun send(messageRes: Int) {
        viewModelScope.launch { _events.send(UiEvent.ShowMessage(messageRes)) }
    }
}
