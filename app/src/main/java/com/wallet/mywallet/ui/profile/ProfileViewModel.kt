package com.wallet.mywallet.ui.profile

import android.util.Patterns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.mywallet.R
import com.wallet.mywallet.db.entity.UserEntity
import com.wallet.mywallet.repository.UserRepository
import com.wallet.mywallet.ui.common.UiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** State and actions of the profile screen. The currency choice is kept in [savedState]. */
class ProfileViewModel(
    private val userRepository: UserRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user

    val currency: StateFlow<String> = savedState.getStateFlow(KEY_CURRENCY, "")

    var initialTextShown: Boolean
        get() = savedState[KEY_TEXT_SHOWN] ?: false
        set(value) {
            savedState[KEY_TEXT_SHOWN] = value
        }

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val loaded = userRepository.getUser()
            _user.value = loaded
            if (savedState.get<String>(KEY_CURRENCY).isNullOrEmpty() && loaded != null) {
                savedState[KEY_CURRENCY] = loaded.defaultCurrency
            }
        }
    }

    fun selectCurrency(code: String) {
        savedState[KEY_CURRENCY] = code
    }

    fun save(name: String, email: String) {
        viewModelScope.launch {
            val trimmedName = name.trim()
            val trimmedEmail = email.trim()
            if (trimmedName.isEmpty()) {
                _events.send(UiEvent.ShowMessage(R.string.error_name_required))
                return@launch
            }
            if (trimmedEmail.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                _events.send(UiEvent.ShowMessage(R.string.error_invalid_email))
                return@launch
            }
            userRepository.updateProfile(trimmedName, trimmedEmail.ifEmpty { null }, currency.value)
            _events.send(UiEvent.ShowMessage(R.string.profile_saved))
        }
    }

    private companion object {
        const val KEY_CURRENCY = "currency"
        const val KEY_TEXT_SHOWN = "textShown"
    }
}
