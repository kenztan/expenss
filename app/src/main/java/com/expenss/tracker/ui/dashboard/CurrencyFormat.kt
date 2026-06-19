package com.expenss.tracker.ui.dashboard

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

// Mirrors the web CurrencyService exactly (frontend-expenss/src/app/shared/currency.service.ts).
//
// getSymbol():  USD -> "$" | IDR -> "Rp" | default (JPY & anything else) -> "¥"
// formatAmount(): USD -> $1,234.50 | IDR -> Rp186.155 (dot grouping) | JPY/default -> ¥186,155
//
// These are plain functions of the *currency* string. Call sites pass the currency value that
// was collected from the ViewModel via collectAsState(), so Compose subscribes to currency
// changes and re-renders every amount/prefix the instant the user switches currency.

fun currencySymbol(currency: String): String = when (currency) {
    "USD" -> "$"
    "IDR" -> "Rp"
    else  -> "¥"
}

fun formatAmount(currency: String, n: Double): String {
    val amount = abs(n)
    return when (currency) {
        "USD" -> "$" + DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US)).format(amount)
        "IDR" -> "Rp" + DecimalFormat("#,##0",
            DecimalFormatSymbols(Locale.US).apply { groupingSeparator = '.' }).format(amount)
        else  -> "¥" + DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US)).format(amount)
    }
}
