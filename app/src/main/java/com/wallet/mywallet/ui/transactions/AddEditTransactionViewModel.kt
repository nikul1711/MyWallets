package com.wallet.mywallet.ui.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.mywallet.R
import com.wallet.mywallet.db.entity.CategoryEntity
import com.wallet.mywallet.db.entity.TransactionEntity
import com.wallet.mywallet.db.entity.TransactionType
import com.wallet.mywallet.db.entity.WalletEntity
import com.wallet.mywallet.repository.CategoryRepository
import com.wallet.mywallet.repository.TransactionRepository
import com.wallet.mywallet.repository.WalletRepository
import com.wallet.mywallet.ui.common.UiEvent
import com.wallet.mywallet.util.DateUtils
import com.wallet.mywallet.util.MoneyFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * State and actions of the "add / edit transaction" form.
 * Everything the user picks (type, wallet, category, date) is kept in [savedState],
 * so it survives rotation and the app being killed in the background.
 * The typed amount and note live in the text fields, which Android restores itself.
 */
class AddEditTransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    categoryRepository: CategoryRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {

    private val transactionId: Long = savedState["transactionId"] ?: 0L
    val isEditing = transactionId != 0L

    // ---- What the user picked (0 means "nothing picked") ----
    val type: StateFlow<TransactionType> =
        savedState.getStateFlow(KEY_TYPE, TransactionType.EXPENSE.name)
            .map { TransactionType.valueOf(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, TransactionType.EXPENSE)
    val walletId: StateFlow<Long> = savedState.getStateFlow(KEY_WALLET, 0L)
    val categoryId: StateFlow<Long> = savedState.getStateFlow(KEY_CATEGORY, 0L)
    val date: StateFlow<Long> = savedState.getStateFlow(KEY_DATE, DateUtils.today())

    // ---- Choices for the drop-downs ----
    val wallets: StateFlow<List<WalletEntity>> =
        walletRepository.observeWallets()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Categories of the selected type. Switching income/expense switches the list. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val categories: StateFlow<List<CategoryEntity>> =
        type.flatMapLatest { categoryRepository.observeByType(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _existing = MutableStateFlow<TransactionEntity?>(null)
    val existing: StateFlow<TransactionEntity?> = _existing

    /** True once the screen has copied the loaded transaction into its text fields (only done once). */
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
            if (isEditing) {
                val loaded = transactionRepository.getTransaction(transactionId)
                _existing.value = loaded
                // Copy the saved values into the picks once; after rotation keep the user's changes.
                if (loaded != null && savedState.get<Boolean>(KEY_PICKS_LOADED) != true) {
                    savedState[KEY_TYPE] = loaded.type.name
                    savedState[KEY_WALLET] = loaded.walletId
                    savedState[KEY_CATEGORY] = loaded.categoryId ?: 0L
                    savedState[KEY_DATE] = loaded.date
                    savedState[KEY_PICKS_LOADED] = true
                }
            } else if (walletId.value == 0L) {
                // New transaction: start with the first wallet selected.
                walletRepository.observeWallets().first().firstOrNull()?.let { savedState[KEY_WALLET] = it.id }
            }
        }
    }

    fun selectType(newType: TransactionType) {
        if (newType == type.value) return
        savedState[KEY_TYPE] = newType.name
        savedState[KEY_CATEGORY] = 0L // an expense category does not make sense for income
    }

    fun selectWallet(id: Long) {
        savedState[KEY_WALLET] = id
    }

    fun selectCategory(id: Long) {
        savedState[KEY_CATEGORY] = id
    }

    fun selectDate(millis: Long) {
        savedState[KEY_DATE] = millis
    }

    fun save(amountText: String, note: String) {
        if (saving) return
        viewModelScope.launch {
            val wallet = wallets.value.firstOrNull { it.id == walletId.value }
                ?: return@launch message(R.string.error_wallet_required)

            val amount = MoneyFormatter.parse(amountText, wallet.currency)
            if (amount == null || amount <= 0) return@launch message(R.string.error_invalid_amount)

            saving = true
            transactionRepository.save(
                TransactionEntity(
                    id = transactionId,
                    walletId = wallet.id,
                    categoryId = categoryId.value.takeIf { it != 0L },
                    amount = amount,
                    type = type.value,
                    date = date.value,
                    note = note.trim().ifEmpty { null }
                )
            )
            _events.send(UiEvent.Done)
        }
    }

    fun delete() {
        val transaction = _existing.value ?: return
        viewModelScope.launch {
            transactionRepository.delete(transaction)
            _events.send(UiEvent.Done)
        }
    }

    private suspend fun message(resId: Int) = _events.send(UiEvent.ShowMessage(resId))

    private companion object {
        const val KEY_TYPE = "type"
        const val KEY_WALLET = "walletPick"
        const val KEY_CATEGORY = "categoryPick"
        const val KEY_DATE = "datePick"
        const val KEY_PICKS_LOADED = "picksLoaded"
        const val KEY_TEXT_SHOWN = "textShown"
    }
}
