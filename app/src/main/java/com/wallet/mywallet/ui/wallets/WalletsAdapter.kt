package com.wallet.mywallet.ui.wallets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.ItemWalletBinding
import com.wallet.mywallet.db.model.WalletWithBalance
import com.wallet.mywallet.ui.common.DataClassDiff
import com.wallet.mywallet.util.MoneyFormatter

/** Draws one card per wallet. */
class WalletsAdapter(
    private val onClick: (WalletWithBalance) -> Unit
) : ListAdapter<WalletWithBalance, WalletsAdapter.ViewHolder>(DataClassDiff { it.wallet.id }) {

    inner class ViewHolder(private val binding: ItemWalletBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WalletWithBalance) {
            val context = binding.root.context
            binding.name.text = item.wallet.name
            binding.currency.text = item.wallet.currency
            binding.balance.text = MoneyFormatter.format(item.balance, item.wallet.currency)
            binding.balance.setTextColor(
                ContextCompat.getColor(context, if (item.balance < 0) R.color.expense else R.color.md_onSurface)
            )
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemWalletBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))
}
