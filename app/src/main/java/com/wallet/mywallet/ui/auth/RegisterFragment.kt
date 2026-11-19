package com.wallet.mywallet.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.FragmentRegisterBinding
import com.wallet.mywallet.ui.common.appViewModels
import com.wallet.mywallet.ui.common.collectFlow
import com.wallet.mywallet.ui.common.handleUiEvent
import com.wallet.mywallet.ui.common.textOrEmpty

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by appViewModels { app, _ -> RegisterViewModel(app.authRepository) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentRegisterBinding.bind(view)

        binding.registerButton.setOnClickListener {
            viewModel.register(
                binding.nameInput.textOrEmpty(),
                binding.emailInput.textOrEmpty(),
                binding.passwordInput.textOrEmpty(),
                binding.confirmPasswordInput.textOrEmpty()
            )
        }
        binding.loginLink.setOnClickListener { findNavController().popBackStack() }

        collectFlow(viewModel.loading) { loading ->
            binding.registerButton.isEnabled = !loading
            binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
        }
        collectFlow(viewModel.events) { handleUiEvent(it) }
        collectFlow(viewModel.confirmationSent) {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.confirm_email_sent)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok) { _, _ -> findNavController().popBackStack() }
                .show()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
