package com.wallet.mywallet.ui.goals

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.FragmentGoalsBinding
import com.wallet.mywallet.ui.common.appViewModels
import com.wallet.mywallet.ui.common.collectFlow
import com.wallet.mywallet.ui.common.handleUiEvent
import com.wallet.mywallet.ui.common.showAmountDialog

class GoalsFragment : Fragment(R.layout.fragment_goals) {

    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GoalsViewModel by appViewModels { app, _ ->
        GoalsViewModel(app.goalRepository, app.subscriptionRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentGoalsBinding.bind(view)

        val adapter = GoalsAdapter(
            onClick = { goal ->
                findNavController().navigate(R.id.addEditGoalFragment, bundleOf("goalId" to goal.id))
            },
            onAddSavings = { goal ->
                showAmountDialog(R.string.add_savings) { text -> viewModel.addSavings(goal, text) }
            }
        )
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter
        binding.fab.setOnClickListener { viewModel.onAddClicked() }

        collectFlow(viewModel.goals) { goals ->
            adapter.submitList(goals.orEmpty())
            binding.empty.isVisible = goals != null && goals.isEmpty()
        }
        collectFlow(viewModel.events) { handleUiEvent(it) }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
