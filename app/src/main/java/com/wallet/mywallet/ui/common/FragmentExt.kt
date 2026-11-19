package com.wallet.mywallet.ui.common

import android.app.DatePickerDialog
import android.view.LayoutInflater
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.wallet.mywallet.AppContainer
import com.wallet.mywallet.MyWalletApp
import com.wallet.mywallet.R
import com.wallet.mywallet.databinding.DialogAmountBinding
import com.wallet.mywallet.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Calendar

/*
 * Small helpers shared by all screens, so each fragment stays short and easy to read.
 */

/**
 * Creates a ViewModel that survives screen rotation.
 * [create] receives the app's repositories ([AppContainer]) and a [SavedStateHandle],
 * which also holds the arguments the screen was opened with (for example "walletId").
 */
inline fun <reified VM : ViewModel> Fragment.appViewModels(
    crossinline create: (AppContainer, SavedStateHandle) -> VM
): Lazy<VM> = viewModels {
    viewModelFactory {
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyWalletApp
            create(app.container, createSavedStateHandle())
        }
    }
}

/**
 * Listens to a Flow, but only while the screen is visible (STARTED).
 * When the user leaves the app it stops; when they come back it resumes. This saves battery
 * and avoids touching views that no longer exist.
 */
fun <T> Fragment.collectFlow(flow: Flow<T>, action: (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { action(it) }
        }
    }
}

/** Does what a ViewModel's [UiEvent] asks for. */
fun Fragment.handleUiEvent(event: UiEvent) {
    when (event) {
        is UiEvent.ShowMessage ->
            Snackbar.make(requireView(), event.messageRes, Snackbar.LENGTH_LONG).show()

        is UiEvent.NeedsPremium ->
            Snackbar.make(requireView(), event.messageRes, Snackbar.LENGTH_LONG)
                .setAction(R.string.upgrade) { findNavController().navigate(R.id.premiumFragment) }
                .show()

        is UiEvent.Navigate -> findNavController().navigate(event.destinationId, event.args)

        UiEvent.Done -> findNavController().popBackStack()
    }
}

/** Fills a drop-down with [items], shows the item at [selectedIndex], and reports taps. */
fun MaterialAutoCompleteTextView.bindItems(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    setSimpleItems(items.toTypedArray())
    // "false" = do not filter the list by the text we just set.
    setText(items.getOrNull(selectedIndex).orEmpty(), false)
    setOnItemClickListener { _, _, position, _ -> onSelected(position) }
}

/** Opens a calendar. The result is the start of the chosen local day. */
fun Fragment.showDatePicker(initialMillis: Long, onPicked: (Long) -> Unit) {
    val calendar = Calendar.getInstance().apply { timeInMillis = if (initialMillis > 0) initialMillis else System.currentTimeMillis() }
    DatePickerDialog(
        requireContext(),
        { _, year, month, day ->
            val picked = Calendar.getInstance().apply { set(year, month, day) }.timeInMillis
            onPicked(DateUtils.startOfDay(picked))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

/** Asks "are you sure?" before a delete. */
fun Fragment.confirmDelete(@StringRes message: Int, onConfirm: () -> Unit) {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.delete)
        .setMessage(message)
        .setNegativeButton(R.string.cancel, null)
        .setPositiveButton(R.string.delete) { _, _ -> onConfirm() }
        .show()
}

/** Shows a dialog with one amount field and returns what the user typed. */
fun Fragment.showAmountDialog(@StringRes title: Int, onConfirm: (String) -> Unit) {
    val dialogBinding = DialogAmountBinding.inflate(LayoutInflater.from(requireContext()))
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(title)
        .setView(dialogBinding.root)
        .setNegativeButton(R.string.cancel, null)
        .setPositiveButton(R.string.save) { _, _ -> onConfirm(dialogBinding.amountInput.text?.toString().orEmpty()) }
        .show()
}

/** The text of an input field, never null. */
fun TextInputEditText.textOrEmpty(): String = text?.toString().orEmpty()
