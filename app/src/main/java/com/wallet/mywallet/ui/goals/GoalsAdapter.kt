package com.wallet.mywallet.ui.goals

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.ItemGoalBinding
import com.wallet.mywallet.db.entity.GoalEntity
import com.wallet.mywallet.ui.common.DataClassDiff
import com.wallet.mywallet.util.DateUtils
import com.wallet.mywallet.util.MoneyFormatter

/** Draws one card per savings goal. */
class GoalsAdapter(
    private val onClick: (GoalEntity) -> Unit,
    private val onAddSavings: (GoalEntity) -> Unit
) : ListAdapter<GoalEntity, GoalsAdapter.ViewHolder>(DataClassDiff { it.id }) {

    inner class ViewHolder(private val binding: ItemGoalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(goal: GoalEntity) {
            val context = binding.root.context
            val isDone = goal.savedAmount >= goal.targetAmount

            binding.name.text = goal.name
            binding.amounts.text = context.getString(
                R.string.goal_saved_of,
                MoneyFormatter.format(goal.savedAmount, goal.currency),
                MoneyFormatter.format(goal.targetAmount, goal.currency)
            )
            val percent = if (goal.targetAmount > 0) (goal.savedAmount * 100 / goal.targetAmount).coerceIn(0, 100).toInt() else 0
            binding.progress.setProgressCompat(percent, false)
            binding.percent.text = context.getString(R.string.percent_format, percent)

            binding.status.text = when {
                isDone -> context.getString(R.string.goal_completed)
                goal.deadline != null -> context.getString(R.string.goal_due, DateUtils.formatDate(goal.deadline))
                else -> ""
            }
            binding.status.isVisible = binding.status.text.isNotEmpty()
            binding.addSavingsButton.isVisible = !isDone

            binding.root.setOnClickListener { onClick(goal) }
            binding.addSavingsButton.setOnClickListener { onAddSavings(goal) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))
}
