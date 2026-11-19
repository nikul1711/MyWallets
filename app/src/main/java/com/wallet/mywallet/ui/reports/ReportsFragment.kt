package com.wallet.mywallet.ui.reports

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.FragmentReportsBinding
import com.wallet.mywallet.ui.common.appViewModels
import com.wallet.mywallet.ui.common.bindItems
import com.wallet.mywallet.ui.common.collectFlow
import com.wallet.mywallet.ui.common.handleUiEvent
import com.wallet.mywallet.util.CurrencyUtils
import com.wallet.mywallet.util.DateUtils
import com.wallet.mywallet.util.MoneyFormatter

class ReportsFragment : Fragment(R.layout.fragment_reports) {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportsViewModel by appViewModels { app, savedState ->
        ReportsViewModel(
            app.transactionRepository,
            app.walletRepository,
            app.userRepository,
            app.subscriptionRepository,
            savedState
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentReportsBinding.bind(view)

        val adapter = CategorySpendAdapter()
        binding.categoryList.layoutManager = LinearLayoutManager(requireContext())
        binding.categoryList.adapter = adapter
        binding.categoryList.isNestedScrollingEnabled = false // the whole screen scrolls, not the list

        binding.previousMonthButton.setOnClickListener { viewModel.changeMonth(-1) }
        binding.nextMonthButton.setOnClickListener { viewModel.changeMonth(+1) }
        binding.upgradeButton.setOnClickListener { findNavController().navigate(R.id.premiumFragment) }

        collectFlow(viewModel.state) { state -> render(state, adapter) }
        collectFlow(viewModel.events) { handleUiEvent(it) }
    }

    private fun render(state: ReportState?, adapter: CategorySpendAdapter) {
        // No wallets yet (or still loading): show only the hint.
        binding.content.isVisible = state != null
        binding.empty.isVisible = state == null
        if (state == null) return

        binding.monthTitle.text = DateUtils.formatMonth(state.monthStart)
        binding.nextMonthButton.isEnabled = state.monthStart < DateUtils.startOfMonth(0)

        // Currency chooser is only needed when the user has wallets in several currencies.
        binding.currencyLayout.isVisible = state.currencies.size > 1
        binding.currencyInput.bindItems(
            state.currencies.map { CurrencyUtils.label(it) },
            state.currencies.indexOf(state.currency)
        ) { viewModel.chooseCurrency(state.currencies[it]) }

        binding.incomeValue.text = MoneyFormatter.format(state.income, state.currency)
        binding.expenseValue.text = MoneyFormatter.format(state.expense, state.currency)
        binding.netValue.text = MoneyFormatter.format(state.income - state.expense, state.currency)

        val totalExpense = state.categories.sumOf { it.total }
        adapter.submitList(
            state.categories.map {
                CategorySpendRow(
                    categoryId = it.categoryId,
                    name = it.categoryName ?: getString(R.string.uncategorised),
                    amount = it.total,
                    currency = state.currency,
                    percent = if (totalExpense > 0) (it.total * 100 / totalExpense).toInt() else 0
                )
            }
        )
        binding.noSpending.isVisible = state.categories.isEmpty()

        // The 6-month trend is a premium feature.
        binding.trendChart.isVisible = state.isPremium
        binding.trendLocked.isVisible = !state.isPremium
        binding.trendChart.setPoints(state.trend)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
