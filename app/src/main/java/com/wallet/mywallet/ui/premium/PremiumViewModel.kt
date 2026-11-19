package com.wallet.mywallet.ui.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.mywallet.R
import com.wallet.mywallet.billing.PurchasePlan
import com.wallet.mywallet.db.entity.SubscriptionEntity
import com.wallet.mywallet.repository.SubscriptionRepository
import com.wallet.mywallet.ui.common.UiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * State and actions of the Premium screen.
 *
 * Payments are not connected yet, so choosing a plan only switches it on locally ("test mode").
 * Later, [choosePlan] will start the Google Play purchase flow instead.
 */
class PremiumViewModel(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    /** The current plan. null = still loading. */
    val subscription: StateFlow<SubscriptionEntity?> =
        subscriptionRepository.observeCurrent()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun choosePlan(plan: PurchasePlan) {
        viewModelScope.launch {
            subscriptionRepository.activatePlanForTesting(plan)
            _events.send(UiEvent.ShowMessage(R.string.plan_activated_test))
        }
    }

    fun resetToFree() {
        viewModelScope.launch {
            subscriptionRepository.resetToFree()
            _events.send(UiEvent.ShowMessage(R.string.plan_reset_test))
        }
    }
}
