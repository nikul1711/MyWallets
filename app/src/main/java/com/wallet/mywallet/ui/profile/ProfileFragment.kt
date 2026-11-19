package com.wallet.mywallet.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.FragmentProfileBinding
import com.wallet.mywallet.ui.common.appViewModels
import com.wallet.mywallet.ui.common.bindItems
import com.wallet.mywallet.ui.common.collectFlow
import com.wallet.mywallet.ui.common.handleUiEvent
import com.wallet.mywallet.ui.common.textOrEmpty
import com.wallet.mywallet.util.CurrencyUtils

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by appViewModels { app, savedState ->
        ProfileViewModel(app.userRepository, savedState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentProfileBinding.bind(view)

        binding.saveButton.setOnClickListener {
            viewModel.save(binding.nameInput.textOrEmpty(), binding.emailInput.textOrEmpty())
        }
        binding.planButton.setOnClickListener { findNavController().navigate(R.id.premiumFragment) }

        // Copy the saved name and email into the fields once (not again after rotation).
        collectFlow(viewModel.user) { user ->
            if (user != null && !viewModel.initialTextShown) {
                binding.nameInput.setText(user.displayName)
                binding.emailInput.setText(user.email.orEmpty())
                viewModel.initialTextShown = true
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
