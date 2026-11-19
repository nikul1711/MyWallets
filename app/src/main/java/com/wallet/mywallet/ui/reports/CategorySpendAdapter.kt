package com.wallet.mywallet.ui.reports

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.ItemCategorySpendBinding
import com.wallet.mywallet.ui.common.DataClassDiff
import com.wallet.mywallet.util.MoneyFormatter

/** One row of the "spending by category" list. */
data class CategorySpendRow(
    val categoryId: Long?,
    val name: String,
    val amount: Long,
    val currency: String,
    val percent: Int
)

class CategorySpendAdapter :
    ListAdapter<CategorySpendRow, CategorySpendAdapter.ViewHolder>(DataClassDiff { it.categoryId ?: -1L }) {

    inner class ViewHolder(private val binding: ItemCategorySpendBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: CategorySpendRow) {
            binding.name.text = row.name
            binding.amount.text = MoneyFormatter.format(row.amount, row.currency)
            binding.percent.text = binding.root.context.getString(R.string.percent_format, row.percent)
            binding.progress.setProgressCompat(row.percent, false)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCategorySpendBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))
}
