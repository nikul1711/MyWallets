package com.wallet.mywallet.ui.goals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.mywallet.R
import com.wallet.mywallet.billing.Feature
import com.wallet.mywallet.billing.FeatureAccess
import com.wallet.mywallet.db.entity.GoalEntity
import com.wallet.mywallet.repository.GoalRepository
import com.wallet.mywallet.repository.SubscriptionRepository
import com.wallet.mywallet.repository.UserRepository
import com.wallet.mywallet.ui.common.UiEvent
import com.wallet.mywallet.util.CurrencyUtils
import com.wallet.mywallet.util.MoneyFormatter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** State and actions of the "add / edit goal" form. Picks are kept in [savedState] (rotation-safe). */
class AddEditGoalViewModel(
    private val goalRepository: GoalRepository,
    private val userRepository: UserRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {

    private val goalId: Long = savedState["goalId"] ?: 0L
    val isEditing = goalId != 0L

    val currency: StateFlow<String> = savedState.getStateFlow(KEY_CURRENCY, "")

    /** Optional deadline as the start of a day in epoch millis. 0 means "no deadline". */
    val deadline: StateFlow<Long> = savedState.getStateFlow(KEY_DEADLINE, 0L)

    private val _existing = MutableStateFlow<GoalEntity?>(null)
    val existing: StateFlow<GoalEntity?> = _existing

    var initialTextShown: Boolean
        get() = savedState[KEY_TEXT_SHOWN] ?: false
        set(value) {
            savedState[KEY_TEXT_SHOWN] = value
        }

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var saving = false

    init {
        viewModelScope.launch {
            val loaded = if (isEditing) goalRepository.getGoal(goalId) else null
            _existing.value = loaded
            if (savedState.get<Boolean>(KEY_PICKS_LOADED) != true) {
                savedState[KEY_CURRENCY] = loaded?.currency
                    ?: userRepository.getUser()?.defaultCurrency
                    ?: CurrencyUtils.deviceDefault()
                savedState[KEY_DEADLINE] = loaded?.deadline ?: 0L
                savedState[KEY_PICKS_LOADED] = true
            }
        }
    }

    fun selectCurrency(code: String) {
        savedState[KEY_CURRENCY] = code
    }

    fun selectDeadline(millis: Long) {
        savedState[KEY_DEADLINE] = millis
    }

    fun clearDeadline() {
        savedState[KEY_DEADLINE] = 0L
    }

    fun save(name: String, targetText: String) {
        if (saving) return
        viewModelScope.launch {
            val code = currency.value
            val trimmedName = name.trim()
            if (trimmedName.isEmpty()) return@launch message(R.string.error_name_required)
            if (code.isEmpty()) return@launch message(R.string.error_currency_required)

            val target = MoneyFormatter.parse(targetText, code)
            if (target == null || target <= 0) return@launch message(R.string.error_invalid_amount)

            val isPremium = subscriptionRepository.isPremium()
            if (!isEditing && !FeatureAccess.canAdd(Feature.GOALS, goalRepository.count(), isPremium)) {
                _events.send(UiEvent.NeedsPremium(R.string.limit_goals))
                return@launch
            }

            saving = true
            val old = _existing.value
            goalRepository.save(
                GoalEntity(
                    id = goalId,
                    name = trimmedName,
                    targetAmount = target,
                    savedAmount = old?.savedAmount ?: 0L, // editing never changes the money already saved
                    currency = code,
                    deadline = deadline.value.takeIf { it != 0L },
                    createdAt = old?.createdAt ?: System.currentTimeMillis()
                )
            )
            _events.send(UiEvent.Done)
        }
    }

    fun delete() {
        val goal = _existing.value ?: return
        viewModelScope.launch {
            goalRepository.delete(goal)
            _events.send(UiEvent.Done)
        }
    }

    private suspend fun message(resId: Int) = _events.send(UiEvent.ShowMessage(resId))

    private companion object {
        const val KEY_CURRENCY = "currency"
        const val KEY_DEADLINE = "deadlinePick"
        const val KEY_PICKS_LOADED = "picksLoaded"
        const val KEY_TEXT_SHOWN = "textShown"
    }
}
