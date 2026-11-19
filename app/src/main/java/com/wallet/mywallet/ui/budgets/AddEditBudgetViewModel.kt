package com.wallet.mywallet.ui.budgets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.mywallet.R
import com.wallet.mywallet.billing.Feature
import com.wallet.mywallet.billing.FeatureAccess
import com.wallet.mywallet.db.entity.BudgetEntity
import com.wallet.mywallet.db.entity.CategoryEntity
import com.wallet.mywallet.db.entity.TransactionType
import com.wallet.mywallet.repository.BudgetRepository
import com.wallet.mywallet.repository.CategoryRepository
import com.wallet.mywallet.repository.SubscriptionRepository
import com.wallet.mywallet.repository.UserRepository
import com.wallet.mywallet.ui.common.UiEvent
import com.wallet.mywallet.util.CurrencyUtils
import com.wallet.mywallet.util.MoneyFormatter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** State and actions of the "add / edit budget" form. Picks are kept in [savedState] (rotation-safe). */
class AddEditBudgetViewModel(
    private val budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository,
    private val userRepository: UserRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {

    private val budgetId: Long = savedState["budgetId"] ?: 0L
    val isEditing = budgetId != 0L

    /** Chosen category id. 0 means "all expenses". */
    val categoryId: StateFlow<Long> = savedState.getStateFlow(KEY_CATEGORY, 0L)
    val currency: StateFlow<String> = savedState.getStateFlow(KEY_CURRENCY, "")

    /** Budgets only make sense for spending, so only expense categories are offered. */
    val categories: StateFlow<List<CategoryEntity>> =
        categoryRepository.observeByType(TransactionType.EXPENSE)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _existing = MutableStateFlow<BudgetEntity?>(null)
    val existing: StateFlow<BudgetEntity?> = _existing

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
            val loaded = if (isEditing) budgetRepository.getBudget(budgetId) else null
            _existing.value = loaded
            if (savedState.get<Boolean>(KEY_PICKS_LOADED) != true) {
                savedState[KEY_CATEGORY] = loaded?.categoryId ?: 0L
                savedState[KEY_CURRENCY] = loaded?.currency
                    ?: userRepository.getUser()?.defaultCurrency
                    ?: CurrencyUtils.deviceDefault()
                savedState[KEY_PICKS_LOADED] = true
            }
        }
    }

    fun selectCategory(id: Long) {
        savedState[KEY_CATEGORY] = id
    }

    fun selectCurrency(code: String) {
        savedState[KEY_CURRENCY] = code
    }

    fun save(limitText: String) {
        if (saving) return
        viewModelScope.launch {
            val code = currency.value
            if (code.isEmpty()) return@launch message(R.string.error_currency_required)

            val limit = MoneyFormatter.parse(limitText, code)
            if (limit == null || limit <= 0) return@launch message(R.string.error_invalid_amount)

            val isPremium = subscriptionRepository.isPremium()
            if (!isEditing && !FeatureAccess.canAdd(Feature.BUDGETS, budgetRepository.count(), isPremium)) {
                _events.send(UiEvent.NeedsPremium(R.string.limit_budgets))
                return@launch
            }

            saving = true
            budgetRepository.save(
                BudgetEntity(
                    id = budgetId,
                    categoryId = categoryId.value.takeIf { it != 0L },
                    limitAmount = limit,
                    currency = code,
                    createdAt = _existing.value?.createdAt ?: System.currentTimeMillis()
                )
            )
            _events.send(UiEvent.Done)
        }
    }

    fun delete() {
        val budget = _existing.value ?: return
        viewModelScope.launch {
            budgetRepository.delete(budget)
            _events.send(UiEvent.Done)
        }
    }

    private suspend fun message(resId: Int) = _events.send(UiEvent.ShowMessage(resId))

    private companion object {
        const val KEY_CATEGORY = "categoryPick"
        const val KEY_CURRENCY = "currency"
        const val KEY_PICKS_LOADED = "picksLoaded"
        const val KEY_TEXT_SHOWN = "textShown"
    }
}
