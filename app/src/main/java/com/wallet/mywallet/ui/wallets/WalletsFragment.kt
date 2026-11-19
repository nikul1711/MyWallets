package com.wallet.mywallet.ui.wallets

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.FragmentWalletsBinding
import com.wallet.mywallet.ui.common.appViewModels
import com.wallet.mywallet.ui.common.collectFlow
import com.wallet.mywallet.ui.common.handleUiEvent

/** The list of wallets. The fragment only shows data and forwards taps to the ViewModel. */
class WalletsFragment : Fragment(R.layout.fragment_wallets) {

    private var _binding: FragmentWalletsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WalletsViewModel by appViewModels { app, _ ->
        WalletsViewModel(app.walletRepository, app.subscriptionRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentWalletsBinding.bind(view)

        val adapter = WalletsAdapter { item ->
            findNavController().navigate(R.id.addEditWalletFragment, bundleOf("walletId" to item.wallet.id))
        }
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter
        binding.fab.setOnClickListener { viewModel.onAddClicked() }

        collectFlow(viewModel.wallets) { wallets ->
            adapter.submitList(wallets.orEmpty())
            binding.empty.isVisible = wallets != null && wallets.isEmpty()
        }
        collectFlow(viewModel.events) { handleUiEvent(it) }
    }

    override fun onDestroyView() {
        _binding = null // release the views so they can be garbage collected
        super.onDestroyView()
    }
}
