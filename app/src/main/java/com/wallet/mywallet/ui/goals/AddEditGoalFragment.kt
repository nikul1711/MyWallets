package com.wallet.mywallet.ui.goals

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.FragmentAddEditGoalBinding
import com.wallet.mywallet.ui.common.appViewModels
import com.wallet.mywallet.ui.common.bindItems
import com.wallet.mywallet.ui.common.collectFlow
import com.wallet.mywallet.ui.common.confirmDelete
import com.wallet.mywallet.ui.common.handleUiEvent
import com.wallet.mywallet.ui.common.showDatePicker
import com.wallet.mywallet.ui.common.textOrEmpty
import com.wallet.mywallet.util.CurrencyUtils
import com.wallet.mywallet.util.DateUtils
import com.wallet.mywallet.util.MoneyFormatter

class AddEditGoalFragment : Fragment(R.layout.fragment_add_edit_goal) {

    private var _binding: FragmentAddEditGoalBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditGoalViewModel by appViewModels { app, savedState ->
        AddEditGoalViewModel(app.goalRepository, app.userRepository, app.subscriptionRepository, savedState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentAddEditGoalBinding.bind(view)

        binding.deleteButton.isVisible = viewModel.isEditing
        binding.deadlineInput.setOnClickListener {
            showDatePicker(viewModel.deadline.value) { viewModel.selectDeadline(it) }
        }
        binding.clearDeadlineButton.setOnClickListener { viewModel.clearDeadline() }
        binding.saveButton.setOnClickListener {
            viewModel.save(binding.nameInput.textOrEmpty(), binding.targetInput.textOrEmpty())
        }
        binding.deleteButton.setOnClickListener {
            confirmDelete(R.string.delete_goal_message) { viewModel.delete() }
        }

        // Editing: copy the saved name and target into the fields once (not again after rotation).
        collectFlow(viewModel.existing) { goal ->
            if (goal != null && !viewModel.initialTextShown) {
                binding.nameInput.setText(goal.name)
                binding.targetInput.setText(MoneyFormatter.toPlain(goal.targetAmount, goal.currency))
                viewModel.initialTextShown = true
            }
        }

        collectFlow(viewModel.currency) { code ->
            val options = CurrencyUtils.options(code.ifEmpty { null })
            binding.currencyInput.bindItems(options.map { CurrencyUtils.label(it) }, options.indexOf(code)) {
                viewModel.selectCurrency(options[it])
            }
        }

        collectFlow(viewModel.deadline) { millis ->
            binding.deadlineInput.setText(if (millis == 0L) "" else DateUtils.formatDate(millis))
            binding.clearDeadlineButton.isVisible = millis != 0L
        }

        collectFlow(viewModel.events) { handleUiEvent(it) }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
