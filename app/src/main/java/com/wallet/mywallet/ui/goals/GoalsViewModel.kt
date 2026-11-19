package com.wallet.mywallet.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.mywallet.R
import com.wallet.mywallet.billing.Feature
import com.wallet.mywallet.billing.FeatureAccess
import com.wallet.mywallet.db.entity.GoalEntity
import com.wallet.mywallet.repository.GoalRepository
import com.wallet.mywallet.repository.SubscriptionRepository
import com.wallet.mywallet.ui.common.UiEvent
import com.wallet.mywallet.util.MoneyFormatter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** State and actions of the savings goals list. */
class GoalsViewModel(
    private val goalRepository: GoalRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    /** All goals. null = still loading. */
    val goals: StateFlow<List<GoalEntity>?> =
        goalRepository.observeGoals()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /** Free users can create a limited number of goals. */
    fun onAddClicked() {
        viewModelScope.launch {
            val allowed = FeatureAccess.canAdd(
                Feature.GOALS,
                goalRepository.count(),
                subscriptionRepository.isPremium()
            )
            _events.send(
                if (allowed) UiEvent.Navigate(R.id.addEditGoalFragment)
                else UiEvent.NeedsPremium(R.string.limit_goals)
            )
        }
    }

    /** Puts the typed amount into a goal. */
    fun addSavings(goal: GoalEntity, amountText: String) {
        viewModelScope.launch {
            val amount = MoneyFormatter.parse(amountText, goal.currency)
            if (amount == null || amount <= 0) {
                _events.send(UiEvent.ShowMessage(R.string.error_invalid_amount))
                return@launch
            }
            goalRepository.addSavings(goal.id, amount)
            _events.send(UiEvent.ShowMessage(R.string.savings_added))
        }
    }
}
