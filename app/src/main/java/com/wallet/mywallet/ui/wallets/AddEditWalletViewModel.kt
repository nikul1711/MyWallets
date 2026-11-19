package com.wallet.mywallet.ui.wallets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.mywallet.R
import com.wallet.mywallet.billing.Feature
import com.wallet.mywallet.billing.FeatureAccess
import com.wallet.mywallet.db.entity.WalletEntity
import com.wallet.mywallet.repository.SubscriptionRepository
import com.wallet.mywallet.repository.UserRepository
import com.wallet.mywallet.repository.WalletRepository
import com.wallet.mywallet.ui.common.UiEvent
import com.wallet.mywallet.util.CurrencyUtils
import com.wallet.mywallet.util.MoneyFormatter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * State and actions of the "add / edit wallet" form.
 *
 * Rotation: this ViewModel is not destroyed when the phone rotates.
 * The typed text lives in the text fields (Android restores it), and the currency choice lives in
 * [savedState], which also survives Android killing the app in the background.
 */
class AddEditWalletViewModel(
    private val walletRepository: WalletRepository,
    private val userRepository: UserRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {

    /** 0 means "new wallet". Passed in when the screen is opened. */
    private val walletId: Long = savedState["walletId"] ?: 0L
    val isEditing = walletId != 0L

    /** The wallet being edited (null for a new one, or while loading). */
    private val _existing = MutableStateFlow<WalletEntity?>(null)
    val existing: StateFlow<WalletEntity?> = _existing

    /** The chosen currency code. Empty until the default is known. */
    val currency: StateFlow<String> = savedState.getStateFlow(KEY_CURRENCY, "")

    /** True once the screen has copied the loaded wallet into its text fields (only done once). */
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
            val loaded = if (isEditing) walletRepository.getWallet(walletId) else null
            _existing.value = loaded
            if (savedState.get<String>(KEY_CURRENCY).isNullOrEmpty()) {
                savedState[KEY_CURRENCY] = loaded?.currency
                    ?: userRepository.getUser()?.defaultCurrency
                    ?: CurrencyUtils.deviceDefault()
            }
        }
    }

    fun chooseCurrency(code: String) {
        savedState[KEY_CURRENCY] = code
    }

    fun save(name: String, initialBalanceText: String) {
        if (saving) return
        viewModelScope.launch {
            val code = currency.value
            val trimmedName = name.trim()
            if (trimmedName.isEmpty()) return@launch message(R.string.error_name_required)
            if (code.isEmpty()) return@launch message(R.string.error_currency_required)

            val balance = if (initialBalanceText.isBlank()) 0L else MoneyFormatter.parse(initialBalanceText, code)
            if (balance == null) return@launch message(R.string.error_invalid_amount)

            val isPremium = subscriptionRepository.isPremium()
            if (!isEditing && !FeatureAccess.canAdd(Feature.WALLETS, walletRepository.count(), isPremium)) {
                _events.send(UiEvent.NeedsPremium(R.string.limit_wallets))
                return@launch
            }

            saving = true
            walletRepository.save(
                WalletEntity(
                    id = walletId,
                    name = trimmedName,
                    currency = code,
                    initialBalance = balance,
                    createdAt = _existing.value?.createdAt ?: System.currentTimeMillis()
                )
            )
            _events.send(UiEvent.Done)
        }
    }

    fun delete() {
        val wallet = _existing.value ?: return
        viewModelScope.launch {
            walletRepository.delete(wallet) // its transactions are deleted too (foreign key CASCADE)
            _events.send(UiEvent.Done)
        }
    }

    private suspend fun message(resId: Int) = _events.send(UiEvent.ShowMessage(resId))

    private companion object {
        const val KEY_CURRENCY = "currency"
        const val KEY_TEXT_SHOWN = "textShown"
    }
}
