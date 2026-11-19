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

/** Creates an account. If Supabase asks for email confirmation, the screen tells the user and returns to Login. */
class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /** Fires when the account exists but the user must click the link in their email before logging in. */
    private val _confirmationSent = Channel<Unit>(Channel.BUFFERED)
    val confirmationSent = _confirmationSent.receiveAsFlow()

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        val cleanName = name.trim()
        val cleanEmail = email.trim()
        when {
            cleanName.isEmpty() -> return send(R.string.error_name_required)
            !Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches() -> return send(R.string.error_invalid_email)
            password.length < MIN_PASSWORD_LENGTH -> return send(R.string.error_password_short)
            password != confirmPassword -> return send(R.string.error_password_mismatch)
        }
        if (_loading.value) return

        viewModelScope.launch {
            _loading.value = true
            val result = authRepository.register(cleanName, cleanEmail, password)
            _loading.value = false
            when (result) {
                is AuthResult.Failure -> _events.send(UiEvent.ShowMessage(result.error.messageRes()))
                // Logged in at once: MainActivity sees the new session and opens the home screen.
                is AuthResult.Success ->
                    if (result.needsEmailConfirmation) _confirmationSent.send(Unit)
            }
        }
    }

    private fun send(messageRes: Int) {
        viewModelScope.launch { _events.send(UiEvent.ShowMessage(messageRes)) }
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6 // Supabase's default minimum
    }
}
