package com.wallet.mywallet.ui.bills

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.mywallet.R
import com.wallet.mywallet.billing.Feature
import com.wallet.mywallet.billing.FeatureAccess
import com.wallet.mywallet.db.entity.CategoryEntity
import com.wallet.mywallet.db.entity.RecurrenceInterval
import com.wallet.mywallet.db.entity.RecurringBillEntity
import com.wallet.mywallet.db.entity.TransactionType
import com.wallet.mywallet.db.entity.WalletEntity
import com.wallet.mywallet.repository.BillRepository
import com.wallet.mywallet.repository.CategoryRepository
import com.wallet.mywallet.repository.SubscriptionRepository
import com.wallet.mywallet.repository.WalletRepository
import com.wallet.mywallet.ui.common.UiEvent
import com.wallet.mywallet.util.DateUtils
import com.wallet.mywallet.util.MoneyFormatter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * State and actions of the "add / edit recurring bill" form.
 * Everything picked from a list (wallet, category, repeat, due date, active) is kept in
 * [savedState], so it survives rotation. Typed text lives in the text fields.
 */
class AddEditBillViewModel(
    private val billRepository: BillRepository,
    private val walletRepository: WalletRepository,
    categoryRepository: CategoryRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {

    private val billId: Long = savedState["billId"] ?: 0L
    val isEditing = billId != 0L

    val walletId: StateFlow<Long> = savedState.getStateFlow(KEY_WALLET, 0L)
    val categoryId: StateFlow<Long> = savedState.getStateFlow(KEY_CATEGORY, 0L)
    val interval: StateFlow<RecurrenceInterval> =
        savedState.getStateFlow(KEY_INTERVAL, RecurrenceInterval.MONTHLY.name)
            .map { RecurrenceInterval.valueOf(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, RecurrenceInterval.MONTHLY)
    val nextDueDate: StateFlow<Long> = savedState.getStateFlow(KEY_DUE, DateUtils.today())
    val isActive: StateFlow<Boolean> = savedState.getStateFlow(KEY_ACTIVE, true)

    val wallets: StateFlow<List<WalletEntity>> =
        walletRepository.observeWallets()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> =
        categoryRepository.observeByType(TransactionType.EXPENSE)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _existing = MutableStateFlow<RecurringBillEntity?>(null)
    val existing: StateFlow<RecurringBillEntity?> = _existing

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
            val loaded = if (isEditing) billRepository.getBill(billId) else null
            _existing.value = loaded
            if (savedState.get<Boolean>(KEY_PICKS_LOADED) != true) {
                if (loaded != null) {
                    savedState[KEY_WALLET] = loaded.walletId
                    savedState[KEY_CATEGORY] = loaded.categoryId ?: 0L
                    savedState[KEY_INTERVAL] = loaded.interval.name
                    savedState[KEY_DUE] = loaded.nextDueDate
                    savedState[KEY_ACTIVE] = loaded.isActive
                } else {
                    walletRepository.observeWallets().first().firstOrNull()?.let { savedState[KEY_WALLET] = it.id }
                }
                savedState[KEY_PICKS_LOADED] = true
            }
        }
    }

    fun selectWallet(id: Long) {
        savedState[KEY_WALLET] = id
    }

    fun selectCategory(id: Long) {
        savedState[KEY_CATEGORY] = id
    }

    fun selectInterval(value: RecurrenceInterval) {
        savedState[KEY_INTERVAL] = value.name
    }

    fun selectDueDate(millis: Long) {
        savedState[KEY_DUE] = millis
    }

    fun setActive(active: Boolean) {
        savedState[KEY_ACTIVE] = active
    }

    fun save(name: String, amountText: String, remindDaysText: String) {
        if (saving) return
        viewModelScope.launch {
            val trimmedName = name.trim()
            if (trimmedName.isEmpty()) return@launch message(R.string.error_name_required)

            val wallet = wallets.value.firstOrNull { it.id == walletId.value }
                ?: return@launch message(R.string.error_wallet_required)

            val amount = MoneyFormatter.parse(amountText, wallet.currency)
            if (amount == null || amount <= 0) return@launch message(R.string.error_invalid_amount)

            val remindDays = remindDaysText.trim().ifEmpty { "0" }.toIntOrNull()
            if (remindDays == null || remindDays !in 0..60) return@launch message(R.string.error_invalid_remind_days)

            if (!isEditing && !FeatureAccess.canUse(Feature.RECURRING_BILLS, subscriptionRepository.isPremium())) {
                _events.send(UiEvent.NeedsPremium(R.string.limit_bills))
                return@launch
            }

            saving = true
            billRepository.save(
                RecurringBillEntity(
                    id = billId,
                    name = trimmedName,
                    amount = amount,
                    walletId = wallet.id,
                    categoryId = categoryId.value.takeIf { it != 0L },
                    interval = interval.value,
                    nextDueDate = nextDueDate.value,
                    remindDaysBefore = remindDays,
                    isActive = isActive.value,
                    createdAt = _existing.value?.createdAt ?: System.currentTimeMillis()
                )
            )
            _events.send(UiEvent.Done)
        }
    }

    fun delete() {
        val bill = _existing.value ?: return
        viewModelScope.launch {
            billRepository.delete(bill)
            _events.send(UiEvent.Done)
        }
    }

    private suspend fun message(resId: Int) = _events.send(UiEvent.ShowMessage(resId))

    private companion object {
        const val KEY_WALLET = "walletPick"
        const val KEY_CATEGORY = "categoryPick"
        const val KEY_INTERVAL = "intervalPick"
        const val KEY_DUE = "duePick"
        const val KEY_ACTIVE = "activePick"
        const val KEY_PICKS_LOADED = "picksLoaded"
        const val KEY_TEXT_SHOWN = "textShown"
    }
}
