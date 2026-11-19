package com.wallet.mywallet.ui.common

import androidx.recyclerview.widget.DiffUtil

/**
 * Tells a RecyclerView which rows changed, so it only redraws those (and animates nicely).
 * [idOf] returns the unique id of a row; data classes are compared field by field.
 */
class DataClassDiff<T : Any>(private val idOf: (T) -> Any) : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T) = idOf(oldItem) == idOf(newItem)
    override fun areContentsTheSame(oldItem: T, newItem: T) = oldItem == newItem
}
