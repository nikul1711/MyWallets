package com.wallet.mywallet.ui.bills

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.FragmentAddEditBillBinding
import com.wallet.mywallet.db.entity.RecurrenceInterval
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

class AddEditBillFragment : Fragment(R.layout.fragment_add_edit_bill) {

    private var _binding: FragmentAddEditBillBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditBillViewModel by appViewModels { app, savedState ->
        AddEditBillViewModel(
            app.billRepository,
            app.walletRepository,
            app.categoryRepository,
            app.subscriptionRepository,
            savedState
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentAddEditBillBinding.bind(view)

        binding.deleteButton.isVisible = viewModel.isEditing
        binding.activeSwitch.isVisible = viewModel.isEditing
        binding.dueDateInput.setOnClickListener {
            showDatePicker(viewModel.nextDueDate.value) { viewModel.selectDueDate(it) }
        }
        binding.activeSwitch.setOnCheckedChangeListener { _, checked -> viewModel.setActive(checked) }
        binding.saveButton.setOnClickListener {
            viewModel.save(
                binding.nameInput.textOrEmpty(),
                binding.amountInput.textOrEmpty(),
                binding.remindInput.textOrEmpty()
            )
        }
        binding.deleteButton.setOnClickListener {
            confirmDelete(R.string.delete_bill_message) { viewModel.delete() }
        }

        // Editing: copy the saved texts into the fields once. The amount needs the wallet's
        // currency, so wait until both the bill and the wallets are loaded.
        collectFlow(combine(viewModel.existing, viewModel.wallets, ::Pair)) { (bill, wallets) ->
            if (bill != null && !viewModel.initialTextShown) {
                val currency = wallets.firstOrNull { it.id == bill.walletId }?.currency
                if (currency != null) {
                    binding.nameInput.setText(bill.name)
                    binding.amountInput.setText(MoneyFormatter.toPlain(bill.amount, currency))
                    binding.remindInput.setText(bill.remindDaysBefore.toString())
                    viewModel.initialTextShown = true
                }
            }
        }

        collectFlow(combine(viewModel.wallets, viewModel.walletId, ::Pair)) { (wallets, selectedId) ->
            binding.walletInput.bindItems(wallets.map { it.name }, wallets.indexOfFirst { it.id == selectedId }) {
                viewModel.selectWallet(wallets[it].id)
            }
        }

        collectFlow(combine(viewModel.categories, viewModel.categoryId, ::Pair)) { (categories, selectedId) ->
            val items = listOf(getString(R.string.no_category)) + categories.map { it.name }
            val selectedIndex = if (selectedId == 0L) 0 else categories.indexOfFirst { it.id == selectedId } + 1
            binding.categoryInput.bindItems(items, selectedIndex) { index ->
                viewModel.selectCategory(if (index == 0) 0L else categories[index - 1].id)
            }
        }

        collectFlow(viewModel.interval) { selected ->
            val all = RecurrenceInterval.entries
            binding.intervalInput.bindItems(all.map { getString(it.labelRes()) }, all.indexOf(selected)) {
                viewModel.selectInterval(all[it])
            }
        }

        collectFlow(viewModel.nextDueDate) { binding.dueDateInput.setText(DateUtils.formatDate(it)) }
        collectFlow(viewModel.isActive) { active ->
            // Setting "checked" from code would trigger the listener again; skip if unchanged.
            if (binding.activeSwitch.isChecked != active) binding.activeSwitch.isChecked = active
        }
        collectFlow(viewModel.events) { handleUiEvent(it) }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
