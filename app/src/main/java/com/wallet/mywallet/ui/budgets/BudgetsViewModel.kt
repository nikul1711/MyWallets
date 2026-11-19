package com.wallet.mywallet.ui.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.mywallet.R
import com.wallet.mywallet.billing.Feature
import com.wallet.mywallet.billing.FeatureAccess
import com.wallet.mywallet.db.model.BudgetWithSpent
import com.wallet.mywallet.repository.BudgetRepository
import com.wallet.mywallet.repository.SubscriptionRepository
import com.wallet.mywallet.ui.common.UiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** State and actions of the budget list. */
class BudgetsViewModel(
    private val budgetRepository: BudgetRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    /** Budgets with what has been spent this month. null = still loading. */
    val budgets: StateFlow<List<BudgetWithSpent>?> =
        budgetRepository.observeCurrentMonth()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /** Free users can create a limited number of budgets. */
    fun onAddClicked() {
        viewModelScope.launch {
            val allowed = FeatureAccess.canAdd(
                Feature.BUDGETS,
                budgetRepository.count(),
                subscriptionRepository.isPremium()
            )
            _events.send(
                if (allowed) UiEvent.Navigate(R.id.addEditBudgetFragment)
                else UiEvent.NeedsPremium(R.string.limit_budgets)
            )
        }
    }
}
