package com.wallet.mywallet.ui.transactions

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.FragmentTransactionsBinding
import com.wallet.mywallet.ui.common.appViewModels
import com.wallet.mywallet.ui.common.collectFlow
import com.wallet.mywallet.ui.common.handleUiEvent
import com.wallet.mywallet.util.MoneyFormatter

class TransactionsFragment : Fragment(R.layout.fragment_transactions) {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionsViewModel by appViewModels { app, _ ->
        TransactionsViewModel(app.transactionRepository, app.walletRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentTransactionsBinding.bind(view)

        val adapter = TransactionsAdapter { item ->
            findNavController().navigate(
                R.id.addEditTransactionFragment,
                bundleOf("transactionId" to item.transaction.id)
            )
        }
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter
        binding.fab.setOnClickListener { viewModel.onAddClicked() }

        collectFlow(viewModel.transactions) { list ->
            adapter.submitList(list.orEmpty())
            binding.empty.isVisible = list != null && list.isEmpty()
        }

        collectFlow(viewModel.monthTotals) { totals ->
            // One line per currency, because amounts in different currencies must not be added up.
            binding.summaryText.text =
                if (totals.isEmpty()) {
                    getString(R.string.no_activity_this_month)
                } else {
                    totals.joinToString("\n\n") {
                        getString(
                            R.string.month_summary_line,
                            MoneyFormatter.format(it.income, it.currency),
                            MoneyFormatter.format(it.expense, it.currency)
                        )
                    }
                }
        }

        collectFlow(viewModel.events) { handleUiEvent(it) }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
