package com.wallet.mywallet.ui.common

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.StringRes

/**
 * One-time things a ViewModel asks its screen to do (show a message, close, open another screen).
 * They are different from screen state: they must happen once and must not repeat after rotation.
 */
sealed interface UiEvent {

    /** Show a short message at the bottom of the screen. */
    data class ShowMessage(@StringRes val messageRes: Int) : UiEvent

    /** Show a message with an "Upgrade" button that opens the Premium screen. */
    data class NeedsPremium(@StringRes val messageRes: Int) : UiEvent

    /** Open another screen. */
    data class Navigate(@IdRes val destinationId: Int, val args: Bundle? = null) : UiEvent

    /** The work is finished: close this screen. */
    data object Done : UiEvent
}
