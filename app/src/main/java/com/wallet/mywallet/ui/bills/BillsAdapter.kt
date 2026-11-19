package com.wallet.mywallet.ui.bills

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.ItemBillBinding
import com.wallet.mywallet.db.model.BillDetail
import com.wallet.mywallet.ui.common.DataClassDiff
import com.wallet.mywallet.util.DateUtils
import com.wallet.mywallet.util.MoneyFormatter

/** Draws one card per recurring bill. */
class BillsAdapter(
    private val onClick: (BillDetail) -> Unit,
    private val onMarkPaid: (BillDetail) -> Unit
) : ListAdapter<BillDetail, BillsAdapter.ViewHolder>(DataClassDiff { it.bill.id }) {

    inner class ViewHolder(private val binding: ItemBillBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BillDetail) {
            val context = binding.root.context
            val bill = item.bill

            binding.name.text = bill.name
            binding.details.text = listOfNotNull(
                item.walletName,
                context.getString(bill.interval.labelRes()),
                item.categoryName
            ).joinToString(" • ")
            binding.amount.text = MoneyFormatter.format(bill.amount, item.walletCurrency)

            val days = DateUtils.daysBetween(DateUtils.today(), bill.nextDueDate)
            binding.due.text = when {
                !bill.isActive -> context.getString(R.string.bill_paused)
                days < 0 -> context.resources.getQuantityString(R.plurals.reminder_overdue_days, -days, -days)
                days == 0 -> context.getString(R.string.reminder_due_today)
                days <= 7 -> context.resources.getQuantityString(R.plurals.reminder_due_in_days, days, days)
                else -> context.getString(R.string.bill_due_on, DateUtils.formatDate(bill.nextDueDate))
            }
            val urgent = bill.isActive && days <= 0
            binding.due.setTextColor(
                ContextCompat.getColor(context, if (urgent) R.color.expense else R.color.md_onSurfaceVariant)
            )

            binding.markPaidButton.isVisible = bill.isActive
            binding.root.setOnClickListener { onClick(item) }
            binding.markPaidButton.setOnClickListener { onMarkPaid(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemBillBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))
}
