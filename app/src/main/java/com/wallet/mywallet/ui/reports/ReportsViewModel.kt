package com.wallet.mywallet.ui.reports

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.mywallet.R
import com.wallet.mywallet.db.model.CategoryTotal
import com.wallet.mywallet.db.model.MonthlyTotal
import com.wallet.mywallet.repository.SubscriptionRepository
import com.wallet.mywallet.repository.TransactionRepository
import com.wallet.mywallet.repository.UserRepository
import com.wallet.mywallet.repository.WalletRepository
import com.wallet.mywallet.ui.common.UiEvent
import com.wallet.mywallet.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One bar group of the trend chart. */
data class TrendPoint(val label: String, val income: Long, val expense: Long)

/** Everything the report screen shows. */
data class ReportState(
    val currency: String,
    val currencies: List<String>,
    val monthStart: Long,
    val income: Long,
    val expense: Long,
    val categories: List<CategoryTotal>,
    val trend: List<TrendPoint>,
    val isPremium: Boolean
)

/**
 * State and actions of the reports screen.
 * The chosen month and currency live in [savedState], so rotation keeps them.
 * Free users only see the current month; browsing months and the trend chart are premium.
 */
class ReportsViewModel(
    transactionRepository: TransactionRepository,
    walletRepository: WalletRepository,
    userRepository: UserRepository,
    subscriptionRepository: SubscriptionRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {

    /** 0 = this month, -1 = last month, and so on. */
    private val monthOffset: StateFlow<Int> = savedState.getStateFlow(KEY_OFFSET, 0)

    /** The currency picked by the user, or "" to use the default. */
    private val chosenCurrency: StateFlow<String> = savedState.getStateFlow(KEY_CURRENCY, "")

    private val currencies = walletRepository.observeCurrencies()
    private val isPremium = subscriptionRepository.observeIsPremium()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val premiumNow = isPremium.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** The report to show. null = loading, or the user has no wallets yet. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<ReportState?> =
        combine(monthOffset, chosenCurrency, currencies, isPremium) { offset, chosen, available, premium ->
            val defaultCurrency = userRepository.getUser()?.defaultCurrency
            val currency = when {
                chosen in available -> chosen
                defaultCurrency in available -> defaultCurrency
                else -> available.firstOrNull()
            }
            // Free users are always on the current month.
            Inputs(currency, available, if (premium) offset else 0, premium)
        }.flatMapLatest { inputs ->
            if (inputs.currency == null) {
                flowOf(null)
            } else {
                buildReport(transactionRepository, inputs.currency, inputs.currencies, inputs.offset, inputs.premium)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private data class Inputs(
        val currency: String?,
        val currencies: List<String>,
        val offset: Int,
        val premium: Boolean
    )

    private fun buildReport(
        repository: TransactionRepository,
        currency: String,
        currencies: List<String>,
        offset: Int,
        premium: Boolean
    ) = combine(
        repository.observeExpenseByCategory(currency, DateUtils.startOfMonth(offset), DateUtils.startOfMonth(offset + 1)),
        repository.observeMonthlyTotals(currency, DateUtils.startOfMonth(offset - 5), DateUtils.startOfMonth(offset + 1))
    ) { categories, monthly ->
        val monthStart = DateUtils.startOfMonth(offset)
        val trend = (-5..0).map { step -> trendPoint(monthly, DateUtils.startOfMonth(offset + step)) }
        ReportState(
            currency = currency,
            currencies = currencies,
            monthStart = monthStart,
            income = trend.last().income,
            expense = trend.last().expense,
            categories = categories,
            trend = trend,
            isPremium = premium
        )
    }

    /** The totals of one month. Months without any transaction count as zero. */
    private fun trendPoint(monthly: List<MonthlyTotal>, monthStart: Long): TrendPoint {
        val found = monthly.firstOrNull { it.month == DateUtils.monthKey(monthStart) }
        return TrendPoint(DateUtils.formatMonthShort(monthStart), found?.income ?: 0L, found?.expense ?: 0L)
    }

    /** Moves to another month. Future months are not allowed. Premium only. */
    fun changeMonth(delta: Int) {
        if (!premiumNow.value) {
            viewModelScope.launch { _events.send(UiEvent.NeedsPremium(R.string.reports_premium_only)) }
            return
        }
        savedState[KEY_OFFSET] = (monthOffset.value + delta).coerceAtMost(0)
    }

    fun chooseCurrency(code: String) {
        savedState[KEY_CURRENCY] = code
    }

    private companion object {
        const val KEY_OFFSET = "monthOffset"
        const val KEY_CURRENCY = "currency"
    }
}
