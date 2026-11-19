package com.wallet.mywallet.ui.more

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.FragmentMoreBinding
import com.wallet.mywallet.ui.common.appViewModels
import com.wallet.mywallet.ui.common.collectFlow

/** The "More" tab: a menu with a profile header and links to the other screens. */
class MoreFragment : Fragment(R.layout.fragment_more) {

    private var _binding: FragmentMoreBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MoreViewModel by appViewModels { app, _ ->
        MoreViewModel(app.userRepository, app.subscriptionRepository, app.authRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentMoreBinding.bind(view)

        val nav = findNavController()
        binding.profileCard.setOnClickListener { nav.navigate(R.id.profileFragment) }
        binding.goalsRow.setOnClickListener { nav.navigate(R.id.goalsFragment) }
        binding.billsRow.setOnClickListener { nav.navigate(R.id.billsFragment) }
        binding.premiumRow.setOnClickListener { nav.navigate(R.id.premiumFragment) }
        binding.logoutRow.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.logout_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.action_logout) { _, _ -> viewModel.logout() }
                .show()
        }

        collectFlow(viewModel.user) { user ->
            binding.userName.text = user?.displayName.orEmpty()
            binding.userInitial.text = user?.displayName?.firstOrNull()?.uppercase().orEmpty()
        }
        collectFlow(viewModel.isPremium) { isPremium ->
            binding.planBadge.setText(if (isPremium) R.string.plan_premium_badge else R.string.plan_free_badge)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
