package com.wallet.mywallet.ui.auth

import androidx.annotation.StringRes
import com.wallet.mywallet.R
import com.wallet.mywallet.auth.AuthError

/** The text shown to the user for each kind of login / register failure. */
@StringRes
fun AuthError.messageRes(): Int = when (this) {
    AuthError.NOT_CONFIGURED -> R.string.error_auth_not_configured
    AuthError.INVALID_CREDENTIALS -> R.string.error_auth_invalid_credentials
    AuthError.INVALID_EMAIL -> R.string.error_invalid_email
    AuthError.RATE_LIMITED -> R.string.error_auth_rate_limited
    AuthError.EMAIL_TAKEN -> R.string.error_auth_email_taken
    AuthError.WEAK_PASSWORD -> R.string.error_auth_weak_password
    AuthError.EMAIL_NOT_CONFIRMED -> R.string.error_auth_email_not_confirmed
    AuthError.NETWORK -> R.string.error_auth_network
    AuthError.UNKNOWN -> R.string.error_auth_unknown
}
