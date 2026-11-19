package com.wallet.mywallet.ui.budgets

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.FragmentBudgetsBinding
import com.wallet.mywallet.ui.common.appViewModels
import com.wallet.mywallet.ui.common.collectFlow
import com.wallet.mywallet.ui.common.handleUiEvent

class BudgetsFragment : Fragment(R.layout.fragment_budgets) {

    private var _binding: FragmentBudgetsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetsViewModel by appViewModels { app, _ ->
        BudgetsViewModel(app.budgetRepository, app.subscriptionRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentBudgetsBinding.bind(view)

        val adapter = BudgetsAdapter { item ->
            findNavController().navigate(R.id.addEditBudgetFragment, bundleOf("budgetId" to item.budget.id))
        }
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter
        binding.fab.setOnClickListener { viewModel.onAddClicked() }

        collectFlow(viewModel.budgets) { budgets ->
            adapter.submitList(budgets.orEmpty())
            binding.empty.isVisible = budgets != null && budgets.isEmpty()
        }
        collectFlow(viewModel.events) { handleUiEvent(it) }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
