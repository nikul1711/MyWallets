package com.wallet.mywallet.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.DialogResetPasswordBinding
import com.wallet.mywallet.databinding.FragmentLoginBinding
import com.wallet.mywallet.ui.common.appViewModels
import com.wallet.mywallet.ui.common.collectFlow
import com.wallet.mywallet.ui.common.handleUiEvent
import com.wallet.mywallet.ui.common.textOrEmpty

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by appViewModels { app, _ -> LoginViewModel(app.authRepository) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentLoginBinding.bind(view)

        binding.loginButton.setOnClickListener {
            viewModel.login(binding.emailInput.textOrEmpty(), binding.passwordInput.textOrEmpty())
        }
        binding.forgotPassword.setOnClickListener { showResetDialog() }
        binding.registerLink.setOnClickListener { findNavController().navigate(R.id.action_login_to_register) }

        collectFlow(viewModel.loading) { loading ->
            binding.loginButton.isEnabled = !loading
            binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
        }
        collectFlow(viewModel.events) { handleUiEvent(it) }
    }

    private fun showResetDialog() {
        val dialogBinding = DialogResetPasswordBinding.inflate(LayoutInflater.from(requireContext()))
        dialogBinding.emailInput.setText(binding.emailInput.textOrEmpty())
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.reset_password_title)
            .setMessage(R.string.reset_password_message)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.action_send_reset_link) { _, _ ->
                viewModel.resetPassword(dialogBinding.emailInput.textOrEmpty())
            }
            .show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
