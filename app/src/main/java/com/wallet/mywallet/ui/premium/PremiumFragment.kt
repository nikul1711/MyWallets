package com.wallet.mywallet.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wallet.mywallet.R
import com.wallet.mywallet.billing.Feature
import com.wallet.mywallet.billing.PurchasePlan
import com.wallet.mywallet.billing.PurchasePlans
import com.wallet.mywallet.databinding.FragmentPremiumBinding
import com.wallet.mywallet.databinding.ItemFeatureRowBinding
import com.wallet.mywallet.databinding.ItemPlanBinding
import com.wallet.mywallet.db.entity.SubscriptionEntity
import com.wallet.mywallet.ui.common.appViewModels
import com.wallet.mywallet.ui.common.collectFlow
import com.wallet.mywallet.ui.common.handleUiEvent
import com.wallet.mywallet.util.DateUtils

class PremiumFragment : Fragment(R.layout.fragment_premium) {

    private var _binding: FragmentPremiumBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PremiumViewModel by appViewModels { app, _ ->
        PremiumViewModel(app.subscriptionRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentPremiumBinding.bind(view)

        showFeatureComparison()
        binding.resetButton.setOnClickListener { viewModel.resetToFree() }

        collectFlow(viewModel.subscription) { subscription -> render(subscription) }
        collectFlow(viewModel.events) { handleUiEvent(it) }
    }

    private fun render(subscription: SubscriptionEntity?) {
        val isPremium = subscription?.isPremium() == true
        val currentPlan = PurchasePlans.of(subscription?.plan ?: PurchasePlans.free.type)
        val expiresAt = subscription?.expiresAt

        binding.currentPlan.setText(currentPlan.titleRes)
        binding.currentPlanDetail.text = when {
            !isPremium -> getString(R.string.plan_status_free)
            expiresAt == null -> getString(R.string.plan_status_never_expires)
            else -> getString(R.string.plan_status_until, DateUtils.formatDate(expiresAt))
        }
        binding.resetButton.isVisible = isPremium

        // Rebuild the three plan cards. (Only 3 small views, so no RecyclerView is needed.)
        binding.plansContainer.removeAllViews()
        PurchasePlans.purchasable.forEach { plan ->
            val card = ItemPlanBinding.inflate(LayoutInflater.from(requireContext()), binding.plansContainer, false)
            card.title.setText(plan.titleRes)
            card.price.text = getString(R.string.plan_price_period, getString(plan.priceRes), getString(plan.periodRes))

            val isCurrent = isPremium && subscription.plan == plan.type
            card.chooseButton.isEnabled = !isCurrent
            card.chooseButton.setText(if (isCurrent) R.string.current_plan else R.string.choose_plan)
            card.chooseButton.setOnClickListener { confirmChoose(plan) }
            binding.plansContainer.addView(card.root)
        }
    }

    /** Test mode: explain that nothing is charged, then switch the plan on. */
    private fun confirmChoose(plan: PurchasePlan) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.test_mode_title)
            .setMessage(getString(R.string.test_mode_message, getString(plan.titleRes)))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.activate) { _, _ -> viewModel.choosePlan(plan) }
            .show()
    }

    /** The Free vs Premium table, built from the [Feature] list so it can never go out of date. */
    private fun showFeatureComparison() {
        binding.featuresContainer.removeAllViews()
        Feature.entries.forEach { feature ->
            val row = ItemFeatureRowBinding.inflate(LayoutInflater.from(requireContext()), binding.featuresContainer, false)
            row.title.setText(feature.titleRes)
            row.freeText.setText(feature.freeTextRes)
            row.premiumText.setText(feature.premiumTextRes)
            binding.featuresContainer.addView(row.root)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
