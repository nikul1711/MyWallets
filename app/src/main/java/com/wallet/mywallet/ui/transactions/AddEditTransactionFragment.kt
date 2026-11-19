package com.wallet.mywallet.ui.transactions

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.FragmentAddEditTransactionBinding
import com.wallet.mywallet.db.entity.TransactionType
import com.wallet.mywallet.ui.common.appViewModels
import com.wallet.mywallet.ui.common.bindItems
import com.wallet.mywallet.ui.common.collectFlow
import com.wallet.mywallet.ui.common.confirmDelete
import com.wallet.mywallet.ui.common.handleUiEvent
import com.wallet.mywallet.ui.common.showDatePicker
import com.wallet.mywallet.ui.common.textOrEmpty
import com.wallet.mywallet.util.DateUtils
import com.wallet.mywallet.util.MoneyFormatter
import kotlinx.coroutines.flow.combine

class AddEditTransactionFragment : Fragment(R.layout.fragment_add_edit_transaction) {

    private var _binding: FragmentAddEditTransactionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditTransactionViewModel by appViewModels { app, savedState ->
        AddEditTransactionViewModel(
            app.transactionRepository,
            app.walletRepository,
            app.categoryRepository,
            savedState
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentAddEditTransactionBinding.bind(view)

        binding.deleteButton.isVisible = viewModel.isEditing
        binding.expenseButton.setOnClickListener { viewModel.selectType(TransactionType.EXPENSE) }
        binding.incomeButton.setOnClickListener { viewModel.selectType(TransactionType.INCOME) }
        binding.dateInput.setOnClickListener {
            showDatePicker(viewModel.date.value) { viewModel.selectDate(it) }
        }
        binding.saveButton.setOnClickListener {
            viewModel.save(binding.amountInput.textOrEmpty(), binding.noteInput.textOrEmpty())
        }
        binding.deleteButton.setOnClickListener {
            confirmDelete(R.string.delete_transaction_message) { viewModel.delete() }
        }

        // Editing: copy the saved amount and note into the fields once (not again after rotation,
        // because Android restores what the user typed). The amount needs the wallet's currency,
        // so we wait until both the transaction and the wallet list have loaded.
        collectFlow(combine(viewModel.existing, viewModel.wallets, ::Pair)) { (transaction, wallets) ->
            if (transaction != null && !viewModel.initialTextShown) {
                val currency = wallets.firstOrNull { it.id == transaction.walletId }?.currency
                if (currency != null) {
                    binding.amountInput.setText(MoneyFormatter.toPlain(transaction.amount, currency))
                    binding.noteInput.setText(transaction.note.orEmpty())
                    viewModel.initialTextShown = true
                }
            }
        }

        // Income / expense toggle.
        collectFlow(viewModel.type) { type ->
            binding.typeToggle.check(if (type == TransactionType.INCOME) R.id.incomeButton else R.id.expenseButton)
        }

        // Wallet drop-down.
        collectFlow(combine(viewModel.wallets, viewModel.walletId, ::Pair)) { (wallets, selectedId) ->
            binding.walletInput.bindItems(wallets.map { it.name }, wallets.indexOfFirst { it.id == selectedId }) {
                viewModel.selectWallet(wallets[it].id)
            }
        }

        // Category drop-down. The first entry means "no category".
        collectFlow(combine(viewModel.categories, viewModel.categoryId, ::Pair)) { (categories, selectedId) ->
            val items = listOf(getString(R.string.no_category)) + categories.map { it.name }
            val selectedIndex = if (selectedId == 0L) 0 else categories.indexOfFirst { it.id == selectedId } + 1
            binding.categoryInput.bindItems(items, selectedIndex) { index ->
                viewModel.selectCategory(if (index == 0) 0L else categories[index - 1].id)
            }
        }

        collectFlow(viewModel.date) { binding.dateInput.setText(DateUtils.formatDate(it)) }
        collectFlow(viewModel.events) { handleUiEvent(it) }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
