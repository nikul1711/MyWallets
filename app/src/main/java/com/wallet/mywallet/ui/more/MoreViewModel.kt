package com.wallet.mywallet.ui.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.mywallet.auth.AuthRepository
import com.wallet.mywallet.db.entity.UserEntity
import com.wallet.mywallet.repository.SubscriptionRepository
import com.wallet.mywallet.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** State of the "More" menu: who the user is and which plan they are on. */
class MoreViewModel(
    userRepository: UserRepository,
    subscriptionRepository: SubscriptionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    /** MainActivity sees the session end and opens the Login screen. */
    fun logout() {
        viewModelScope.launch { authRepository.signOut() }
    }

    val user: StateFlow<UserEntity?> =
        userRepository.observeUser()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val isPremium: StateFlow<Boolean> =
        subscriptionRepository.observeIsPremium()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
}
