package com.wallet.mywallet.auth

import com.wallet.mywallet.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Who is logged in, as far as the app needs to know. */
data class Account(val email: String?, val name: String?)

/** Why a login / register call failed. The ViewModel turns this into a message for the user. */
enum class AuthError { NOT_CONFIGURED, INVALID_CREDENTIALS, INVALID_EMAIL, EMAIL_TAKEN, WEAK_PASSWORD, EMAIL_NOT_CONFIRMED, RATE_LIMITED, NETWORK, UNKNOWN }

sealed interface AuthResult {
    /** Done. [needsEmailConfirmation] is true when Supabase wants the user to click the link in their inbox first. */
    data class Success(val needsEmailConfirmation: Boolean = false) : AuthResult
    data class Failure(val error: AuthError) : AuthResult
}

/**
 * The only class that talks to Supabase Auth. ViewModels call this, never the SDK directly.
 * Supabase stores the session on the device and refreshes it by itself.
 */
class AuthRepository {

    /** False while SUPABASE_URL / SUPABASE_ANON_KEY in local.properties are still the placeholders. */
    val isConfigured: Boolean =
        !BuildConfig.SUPABASE_URL.contains("YOUR_PROJECT_ID") && !BuildConfig.SUPABASE_ANON_KEY.startsWith("YOUR_")

    // Created on first use, so the app still starts when the placeholders have not been replaced.
    private val client: SupabaseClient by lazy {
        createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY) {
            install(Auth)
        }
    }

    /**
     * true = logged in, false = logged out. Emits nothing while the saved session is still loading.
     * A failed token refresh (for example no internet) counts as logged in: the app works offline.
     */
    val isSignedIn: Flow<Boolean> by lazy {
        if (!isConfigured) {
            kotlinx.coroutines.flow.flowOf(false)
        } else {
            client.auth.sessionStatus
                .map { status ->
                    when (status) {
                        is SessionStatus.Authenticated, is SessionStatus.RefreshFailure -> true
                        is SessionStatus.NotAuthenticated -> false
                        else -> null // still initializing
                    }
                }
                .filterNotNull()
                .distinctUntilChanged()
        }
    }

    /** The logged-in account, or null when logged out. */
    val account: Flow<Account?> by lazy {
        if (!isConfigured) {
            kotlinx.coroutines.flow.flowOf(null)
        } else {
            client.auth.sessionStatus
                .map { status ->
                    val user = (status as? SessionStatus.Authenticated)?.session?.user
                    user?.let { Account(it.email, it.userMetadata?.get("name")?.jsonPrimitive?.contentOrNull) }
                }
                .distinctUntilChanged()
        }
    }

    suspend fun signIn(email: String, password: String): AuthResult = call {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        AuthResult.Success()
    }

    suspend fun register(name: String, email: String, password: String): AuthResult = call {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject { put("name", name) }
        }
        // With "Confirm email" switched on in Supabase there is no session until the link is clicked.
        AuthResult.Success(needsEmailConfirmation = client.auth.currentSessionOrNull() == null)
    }

    suspend fun sendPasswordReset(email: String): AuthResult = call {
        client.auth.resetPasswordForEmail(email)
        AuthResult.Success()
    }

    suspend fun signOut() {
        if (isConfigured) {
            try {
                client.auth.signOut()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // No internet: still forget the session on this device.
                client.auth.clearSession()
            }
        }
    }

    /** Runs a Supabase call and turns any exception into an [AuthResult.Failure]. */
    private suspend fun call(block: suspend () -> AuthResult): AuthResult {
        if (!isConfigured) return AuthResult.Failure(AuthError.NOT_CONFIGURED)
        return try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AuthResult.Failure(e.toAuthError())
        }
    }

    /** Supabase puts the reason in the error text; the exception types differ between SDK versions. */
    private fun Exception.toAuthError(): AuthError {
        val text = (message.orEmpty() + " " + javaClass.simpleName).lowercase()
        return when {
            "invalid login credentials" in text || "invalid_credentials" in text -> AuthError.INVALID_CREDENTIALS
            "email_address_invalid" in text || "is invalid" in text -> AuthError.INVALID_EMAIL
            "over_email_send_rate_limit" in text || "rate limit" in text -> AuthError.RATE_LIMITED
            "already registered" in text || "user_already_exists" in text || "already been registered" in text -> AuthError.EMAIL_TAKEN
            "weak_password" in text || "password should be" in text -> AuthError.WEAK_PASSWORD
            "email not confirmed" in text || "email_not_confirmed" in text -> AuthError.EMAIL_NOT_CONFIRMED
            "unable to resolve" in text || "timeout" in text || "timed out" in text ||
                "unknownhost" in text || "failed to connect" in text || "httprequest" in text -> AuthError.NETWORK
            else -> AuthError.UNKNOWN
        }
    }
}
