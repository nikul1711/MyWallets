package com.wallet.mywallet.ui.bills

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.FragmentBillsBinding
import com.wallet.mywallet.ui.common.appViewModels
import com.wallet.mywallet.ui.common.collectFlow
import com.wallet.mywallet.ui.common.handleUiEvent

class BillsFragment : Fragment(R.layout.fragment_bills) {

    private var _binding: FragmentBillsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BillsViewModel by appViewModels { app, _ ->
        BillsViewModel(app.billRepository, app.subscriptionRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentBillsBinding.bind(view)

        val adapter = BillsAdapter(
            onClick = { item ->
                findNavController().navigate(R.id.addEditBillFragment, bundleOf("billId" to item.bill.id))
            },
            onMarkPaid = { item -> viewModel.markPaid(item) }
        )
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter
        binding.fab.setOnClickListener { viewModel.onAddClicked() }
        binding.upgradeButton.setOnClickListener { findNavController().navigate(R.id.premiumFragment) }

        collectFlow(viewModel.bills) { bills ->
            adapter.submitList(bills.orEmpty())
            binding.empty.isVisible = bills != null && bills.isEmpty()
        }
        // Free users see a banner explaining that bills are a premium feature.
        collectFlow(viewModel.isPremium) { isPremium ->
            binding.premiumBanner.isVisible = isPremium == false
        }
        collectFlow(viewModel.events) { handleUiEvent(it) }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
