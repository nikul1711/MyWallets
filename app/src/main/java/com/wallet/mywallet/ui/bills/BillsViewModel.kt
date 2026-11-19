package com.wallet.mywallet.ui.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.mywallet.R
import com.wallet.mywallet.db.model.BillDetail
import com.wallet.mywallet.repository.BillRepository
import com.wallet.mywallet.repository.SubscriptionRepository
import com.wallet.mywallet.ui.common.UiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** State and actions of the recurring bills list. Recurring bills are a premium feature. */
class BillsViewModel(
    private val billRepository: BillRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    /** All bills, the ones due soonest first. null = still loading. */
    val bills: StateFlow<List<BillDetail>?> =
        billRepository.observeBills()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** null = not known yet, so the "premium" banner does not flash on screen. */
    val isPremium: StateFlow<Boolean?> =
        subscriptionRepository.observeIsPremium()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAddClicked() {
        viewModelScope.launch {
            _events.send(
                if (subscriptionRepository.isPremium()) UiEvent.Navigate(R.id.addEditBillFragment)
                else UiEvent.NeedsPremium(R.string.limit_bills)
            )
        }
    }

    /** Records the payment as an expense and moves the bill to its next due date. */
    fun markPaid(detail: BillDetail) {
        viewModelScope.launch {
            billRepository.markPaid(detail.bill)
            _events.send(UiEvent.ShowMessage(R.string.bill_marked_paid))
        }
    }
}
