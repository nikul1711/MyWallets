package com.wallet.mywallet.ui.budgets

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.FragmentAddEditBudgetBinding
import com.wallet.mywallet.ui.common.appViewModels
import com.wallet.mywallet.ui.common.bindItems
import com.wallet.mywallet.ui.common.collectFlow
import com.wallet.mywallet.ui.common.confirmDelete
import com.wallet.mywallet.ui.common.handleUiEvent
import com.wallet.mywallet.ui.common.textOrEmpty
import com.wallet.mywallet.util.CurrencyUtils
import com.wallet.mywallet.util.MoneyFormatter
import kotlinx.coroutines.flow.combine

class AddEditBudgetFragment : Fragment(R.layout.fragment_add_edit_budget) {

    private var _binding: FragmentAddEditBudgetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditBudgetViewModel by appViewModels { app, savedState ->
        AddEditBudgetViewModel(
            app.budgetRepository,
            app.categoryRepository,
            app.userRepository,
            app.subscriptionRepository,
            savedState
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentAddEditBudgetBinding.bind(view)

        binding.deleteButton.isVisible = viewModel.isEditing
        binding.saveButton.setOnClickListener { viewModel.save(binding.limitInput.textOrEmpty()) }
        binding.deleteButton.setOnClickListener {
            confirmDelete(R.string.delete_budget_message) { viewModel.delete() }
        }

        // Editing: copy the saved limit into the field once (not again after rotation).
        collectFlow(viewModel.existing) { budget ->
            if (budget != null && !viewModel.initialTextShown) {
                binding.limitInput.setText(MoneyFormatter.toPlain(budget.limitAmount, budget.currency))
                viewModel.initialTextShown = true
            }
        }

        // Category drop-down. The first entry means "all expenses".
        collectFlow(combine(viewModel.categories, viewModel.categoryId, ::Pair)) { (categories, selectedId) ->
            val items = listOf(getString(R.string.all_expenses)) + categories.map { it.name }
            val selectedIndex = if (selectedId == 0L) 0 else categories.indexOfFirst { it.id == selectedId } + 1
            binding.categoryInput.bindItems(items, selectedIndex) { index ->
                viewModel.selectCategory(if (index == 0) 0L else categories[index - 1].id)
            }
        }

        collectFlow(viewModel.currency) { code ->
            val options = CurrencyUtils.options(code.ifEmpty { null })
            binding.currencyInput.bindItems(options.map { CurrencyUtils.label(it) }, options.indexOf(code)) {
                viewModel.selectCurrency(options[it])
            }
        }

        collectFlow(viewModel.events) { handleUiEvent(it) }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
