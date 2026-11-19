package com.wallet.mywallet.ui.budgets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.ItemBudgetBinding
import com.wallet.mywallet.db.model.BudgetWithSpent
import com.wallet.mywallet.ui.common.DataClassDiff
import com.wallet.mywallet.util.MoneyFormatter

/** Draws one card per budget with a progress bar. */
class BudgetsAdapter(
    private val onClick: (BudgetWithSpent) -> Unit
) : ListAdapter<BudgetWithSpent, BudgetsAdapter.ViewHolder>(DataClassDiff { it.budget.id }) {

    inner class ViewHolder(private val binding: ItemBudgetBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BudgetWithSpent) {
            val context = binding.root.context
            val currency = item.budget.currency
            val limit = item.budget.limitAmount
            val isOver = item.spent > limit

            binding.title.text = item.categoryName ?: context.getString(R.string.all_expenses)
            binding.amounts.text = context.getString(
                R.string.budget_spent_of,
                MoneyFormatter.format(item.spent, currency),
                MoneyFormatter.format(limit, currency)
            )

            val percent = if (limit > 0) (item.spent * 100 / limit).coerceIn(0, 100).toInt() else 0
            binding.progress.setProgressCompat(percent, false)
            val color = ContextCompat.getColor(context, if (isOver) R.color.expense else R.color.md_primary)
            binding.progress.setIndicatorColor(color)

            binding.remaining.text =
                if (isOver) context.getString(R.string.budget_over, MoneyFormatter.format(item.spent - limit, currency))
                else context.getString(R.string.budget_left, MoneyFormatter.format(limit - item.spent, currency))
            binding.remaining.setTextColor(
                ContextCompat.getColor(context, if (isOver) R.color.expense else R.color.md_onSurfaceVariant)
            )

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))
}
