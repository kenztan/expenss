package com.expenss.tracker.util

import com.expenss.tracker.i18n.LocaleManager
import com.expenss.tracker.i18n.Strings
import org.json.JSONObject
import retrofit2.HttpException

// Mirrors frontend-expenss/src/app/auth/auth-error.util.ts's errorKeyMap exactly: exact backend
// message -> errors.* key, then resolved through Strings for the current language.
private val backendErrorKeyMap = mapOf(
    "Invalid Credentials" to "errors.invalidCredentials",
    "Please verify your email before logging in" to "errors.emailNotVerified",
    "Username already taken" to "errors.usernameTaken",
    "Email already taken" to "errors.emailTaken",
    "Verification link already used or expired" to "errors.linkExpired",
    "Invalid or expired verification link" to "errors.linkInvalid",
    "Invalid token type" to "errors.linkInvalid",
    "Reset link already used or expired" to "errors.resetLinkExpired",
    "Invalid or expired reset link" to "errors.resetLinkInvalid",
    "Email not found" to "errors.unexpected",
    "Too many requests. Please wait before trying again" to "errors.tooManyRequests",
)

fun mapBackendError(e: Exception, fallbackKey: String = "errors.unexpected"): String {
    val backendMessage: String? = if (e is HttpException) {
        val body = e.response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            try { JSONObject(body).optString("message").takeIf { it.isNotBlank() } }
            catch (_: Exception) { null }
        } else null
    } else null

    val key = backendMessage?.let { backendErrorKeyMap[it] } ?: fallbackKey
    return Strings.t(key, LocaleManager.lang.value)
}
