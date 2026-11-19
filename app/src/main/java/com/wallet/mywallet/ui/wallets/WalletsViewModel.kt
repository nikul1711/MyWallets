package com.wallet.mywallet.ui.wallets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.mywallet.R
import com.wallet.mywallet.billing.Feature
import com.wallet.mywallet.billing.FeatureAccess
import com.wallet.mywallet.db.model.WalletWithBalance
import com.wallet.mywallet.repository.SubscriptionRepository
import com.wallet.mywallet.repository.WalletRepository
import com.wallet.mywallet.ui.common.UiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** State and actions of the wallet list screen. */
class WalletsViewModel(
    private val walletRepository: WalletRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    /**
     * The wallets, kept up to date by the database. null = still loading.
     * stateIn(...) turns the database Flow into a StateFlow that survives rotation:
     * WhileSubscribed(5000) keeps the query alive for 5 seconds after the screen goes away,
     * which is longer than a rotation takes, so the data is not reloaded.
     */
    val wallets: StateFlow<List<WalletWithBalance>?> =
        walletRepository.observeWalletsWithBalance()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /** Free users can create a limited number of wallets. */
    fun onAddClicked() {
        viewModelScope.launch {
            val allowed = FeatureAccess.canAdd(
                Feature.WALLETS,
                walletRepository.count(),
                subscriptionRepository.isPremium()
            )
            _events.send(
                if (allowed) UiEvent.Navigate(R.id.addEditWalletFragment)
                else UiEvent.NeedsPremium(R.string.limit_wallets)
            )
        }
    }
}
