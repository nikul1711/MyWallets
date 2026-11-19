package com.wallet.mywallet.ui.wallets

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.FragmentAddEditWalletBinding
import com.wallet.mywallet.ui.common.appViewModels
import com.wallet.mywallet.ui.common.bindItems
import com.wallet.mywallet.ui.common.collectFlow
import com.wallet.mywallet.ui.common.confirmDelete
import com.wallet.mywallet.ui.common.handleUiEvent
import com.wallet.mywallet.ui.common.textOrEmpty
import com.wallet.mywallet.util.CurrencyUtils
import com.wallet.mywallet.util.MoneyFormatter

class AddEditWalletFragment : Fragment(R.layout.fragment_add_edit_wallet) {

    private var _binding: FragmentAddEditWalletBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditWalletViewModel by appViewModels { app, savedState ->
        AddEditWalletViewModel(app.walletRepository, app.userRepository, app.subscriptionRepository, savedState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentAddEditWalletBinding.bind(view)

        binding.deleteButton.isVisible = viewModel.isEditing
        binding.saveButton.setOnClickListener {
            viewModel.save(binding.nameInput.textOrEmpty(), binding.balanceInput.textOrEmpty())
        }
        binding.deleteButton.setOnClickListener {
            confirmDelete(R.string.delete_wallet_message) { viewModel.delete() }
        }

        // Editing: copy the saved wallet into the fields once. After a rotation Android restores
        // the text itself, so we must not overwrite what the user has typed.
        collectFlow(viewModel.existing) { wallet ->
            if (wallet != null && !viewModel.initialTextShown) {
                binding.nameInput.setText(wallet.name)
                binding.balanceInput.setText(MoneyFormatter.toPlain(wallet.initialBalance, wallet.currency))
                viewModel.initialTextShown = true
            }
        }

        // Currency drop-down: the choice is kept in the ViewModel.
        collectFlow(viewModel.currency) { code ->
            val options = CurrencyUtils.options(code.ifEmpty { null })
            binding.currencyInput.bindItems(options.map { CurrencyUtils.label(it) }, options.indexOf(code)) { index ->
                viewModel.chooseCurrency(options[index])
            }
        }

        collectFlow(viewModel.events) { handleUiEvent(it) }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
