package com.wallet.mywallet.ui.transactions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.ItemTransactionBinding
import com.wallet.mywallet.db.entity.TransactionType
import com.wallet.mywallet.db.model.TransactionDetail
import com.wallet.mywallet.ui.common.DataClassDiff
import com.wallet.mywallet.util.DateUtils
import com.wallet.mywallet.util.MoneyFormatter

/** Draws one row per transaction. */
class TransactionsAdapter(
    private val onClick: (TransactionDetail) -> Unit
) : ListAdapter<TransactionDetail, TransactionsAdapter.ViewHolder>(DataClassDiff { it.transaction.id }) {

    inner class ViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TransactionDetail) {
            val context = binding.root.context
            val transaction = item.transaction
            val isIncome = transaction.type == TransactionType.INCOME

            binding.title.text = item.categoryName ?: context.getString(R.string.uncategorised)
            val details = listOfNotNull(
                item.walletName,
                DateUtils.formatDate(transaction.date),
                transaction.note?.takeIf { it.isNotBlank() }
            )
            binding.subtitle.text = details.joinToString(" • ")

            val formatted = MoneyFormatter.format(transaction.amount, item.walletCurrency)
            binding.amount.text = (if (isIncome) "+" else "−") + formatted
            binding.amount.setTextColor(ContextCompat.getColor(context, if (isIncome) R.color.income else R.color.expense))

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))
}
