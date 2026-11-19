package com.wallet.mywallet.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.mywallet.R
import com.wallet.mywallet.db.model.CurrencyTotal
import com.wallet.mywallet.db.model.TransactionDetail
import com.wallet.mywallet.repository.TransactionRepository
import com.wallet.mywallet.repository.WalletRepository
import com.wallet.mywallet.ui.common.UiEvent
import com.wallet.mywallet.util.DateUtils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** State and actions of the transaction list (the home screen). */
class TransactionsViewModel(
    transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    /** All transactions, newest first. null = still loading. */
    val transactions: StateFlow<List<TransactionDetail>?> =
        transactionRepository.observeDetails()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Income and expense of the current month, one entry per currency. */
    val monthTotals: StateFlow<List<CurrencyTotal>> =
        transactionRepository.observeTotalsByCurrency(
            from = DateUtils.startOfMonth(0),
            to = DateUtils.startOfMonth(1)
        ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /** A transaction needs a wallet, so ask the user to create one first if there is none. */
    fun onAddClicked() {
        viewModelScope.launch {
            _events.send(
                if (walletRepository.count() == 0) UiEvent.ShowMessage(R.string.create_wallet_first)
                else UiEvent.Navigate(R.id.addEditTransactionFragment)
            )
        }
    }
}
