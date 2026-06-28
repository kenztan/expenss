package com.expenss.tracker.ui.analytics

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expenss.tracker.data.network.*
import com.expenss.tracker.i18n.AppLang
import com.expenss.tracker.i18n.LocaleManager
import com.expenss.tracker.ui.dashboard.currencySymbol
import com.expenss.tracker.util.TokenManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

data class PeriodTotal(val label: String, val amount: Double, val pct: Int, val isCurrent: Boolean)
data class CatTotal(val key: String, val amount: Double, val color: Color, val pct: Int)

// Exact hex values from analytics.component.ts's `catColors` map.
val ANALYTICS_CAT_COLORS = mapOf(
    "food" to Color(0xFFF97316), "housing" to Color(0xFF3B82F6), "transport" to Color(0xFF8B5CF6),
    "shopping" to Color(0xFFEC4899), "entertainment" to Color(0xFFF59E0B), "taxes" to Color(0xFF14B8A6),
    "investment" to Color(0xFF4ADE80), "savings" to Color(0xFFA78BFA), "other" to Color(0xFF64748B)
)

class AnalyticsViewModel(context: Context) : ViewModel() {
    private val tokenManager = TokenManager(context)
    private val api = RetrofitClient.create(AuthInterceptor(tokenManager))

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _currency = MutableStateFlow("JPY")
    val currency: StateFlow<String> = _currency

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _monthlyTotals = MutableStateFlow<List<PeriodTotal>>(emptyList())
    val monthlyTotals: StateFlow<List<PeriodTotal>> = _monthlyTotals

    private val _categoryTotals = MutableStateFlow<List<CatTotal>>(emptyList())
    val categoryTotals: StateFlow<List<CatTotal>> = _categoryTotals

    private val _comparisonMonths = MutableStateFlow<List<PeriodTotal>>(emptyList())
    val comparisonMonths: StateFlow<List<PeriodTotal>> = _comparisonMonths

    private val _totalCurrentPeriod = MutableStateFlow(0.0)
    val totalCurrentPeriod: StateFlow<Double> = _totalCurrentPeriod

    private var trackingMode = "monthly"
    private var cycleStartDay = 1

    init {
        viewModelScope.launch {
            try {
                val user = api.getMe()
                _username.value = user.username
                _currency.value = user.currency
                trackingMode = user.trackingMode ?: "monthly"
                cycleStartDay = user.cycleStartDay ?: 1
            } catch (_: Exception) { /* fall through with monthly defaults, mirrors web's error->loadExpenses path */ }
            loadData()
        }
    }

    fun setCurrency(code: String) {
        _currency.value = code
        viewModelScope.launch {
            try { api.setCurrency(SetCurrencyRequest(code)) } catch (_: Exception) {}
        }
    }

    // Mirrors analytics.component.ts#monthLabel / #cycleLabel exactly (ja uses "N月", en/id use
    // a short month name via Date.toLocaleDateString).
    private fun monthLabel(monthIndex0: Int): String {
        if (LocaleManager.lang.value == AppLang.JA) return "${monthIndex0 + 1}月"
        val cal = Calendar.getInstance().apply { set(2024, monthIndex0, 1) }
        val locale = if (LocaleManager.lang.value == AppLang.ID) Locale("in", "ID") else Locale.US
        return SimpleDateFormat("MMM", locale).format(cal.time)
    }

    private fun cycleLabel(monthIndex0: Int): String {
        if (LocaleManager.lang.value == AppLang.JA) return "${monthIndex0 + 1}月${cycleStartDay}日"
        return "${monthLabel(monthIndex0)} $cycleStartDay"
    }

    private data class MonthSlot(val month: Int, val year: Int, val label: String, val isCurrent: Boolean)
    private data class CycleSlot(val start: String, val end: String, val label: String, val isCurrent: Boolean)

    private fun monthWindow(n: Int): List<MonthSlot> {
        val today = Calendar.getInstance()
        val out = mutableListOf<MonthSlot>()
        for (i in (n - 1) downTo 0) {
            var m = today.get(Calendar.MONTH) - i
            var y = today.get(Calendar.YEAR)
            while (m < 0) { m += 12; y-- }
            out.add(MonthSlot(m + 1, y, monthLabel(m), i == 0))
        }
        return out
    }

    private fun cycleWindow(n: Int): List<CycleSlot> {
        val today = Calendar.getInstance()
        var cycleMonth = today.get(Calendar.MONTH)
        var cycleYear = today.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_MONTH) < cycleStartDay) {
            if (cycleMonth == 0) { cycleMonth = 11; cycleYear-- } else cycleMonth--
        }
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val out = mutableListOf<CycleSlot>()
        for (i in (n - 1) downTo 0) {
            var m = cycleMonth - i
            var y = cycleYear
            while (m < 0) { m += 12; y-- }
            val endMonth = if (m == 11) 0 else m + 1
            val endYear = if (m == 11) y + 1 else y
            val startCal = Calendar.getInstance().apply { set(y, m, cycleStartDay) }
            val endCal = Calendar.getInstance().apply { set(endYear, endMonth, cycleStartDay - 1) }
            out.add(CycleSlot(fmt.format(startCal.time), fmt.format(endCal.time), cycleLabel(m), i == 0))
        }
        return out
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val results: List<Triple<List<Expense>, String, Boolean>> = if (trackingMode == "paycycle") {
                    cycleWindow(6).map { c ->
                        async { Triple(api.getPaydayExpenses(c.start, c.end), c.label, c.isCurrent) }
                    }.awaitAll()
                } else {
                    monthWindow(6).map { m ->
                        async { Triple(api.getExpenses(m.month, m.year), m.label, m.isCurrent) }
                    }.awaitAll()
                }
                computeCharts(results)
            } catch (_: Exception) {
                _monthlyTotals.value = emptyList()
                _categoryTotals.value = emptyList()
                _comparisonMonths.value = emptyList()
            }
            _isLoading.value = false
        }
    }

    // Mirrors analytics.component.ts#computeCharts exactly: bar pct scales to 85% of the chart
    // height (min 8 when amount>0, 4 when zero), category breakdown is the *current* period only,
    // comparison is the last 3 periods scaled independently to their own max.
    private fun computeCharts(results: List<Triple<List<Expense>, String, Boolean>>) {
        val totals = results.map { (expenses, label, isCurrent) ->
            Triple(label, expenses.sumOf { it.amount }, isCurrent)
        }
        val maxTotal = (totals.maxOfOrNull { it.second } ?: 0.0).coerceAtLeast(1.0)
        _monthlyTotals.value = totals.map { (label, amount, isCurrent) ->
            PeriodTotal(
                label = label, amount = amount, isCurrent = isCurrent,
                pct = if (amount > 0) maxOf((amount / maxTotal * 85).roundToInt(), 8) else 4
            )
        }

        val current = results.find { it.third } ?: results.lastOrNull()
        val catMap = LinkedHashMap<String, Double>()
        current?.first?.forEach { e ->
            val key = e.category.lowercase()
            catMap[key] = (catMap[key] ?: 0.0) + e.amount
        }
        val totalCurrent = catMap.values.sum()
        _totalCurrentPeriod.value = totalCurrent
        val divisor = if (totalCurrent > 0) totalCurrent else 1.0
        _categoryTotals.value = catMap.entries
            .filter { it.value > 0 }
            .sortedByDescending { it.value }
            .map { (key, amount) ->
                CatTotal(
                    key = key, amount = amount,
                    color = ANALYTICS_CAT_COLORS[key] ?: ANALYTICS_CAT_COLORS["other"]!!,
                    pct = (amount / divisor * 100).roundToInt()
                )
            }

        val last3 = totals.takeLast(3)
        val maxComp = (last3.maxOfOrNull { it.second } ?: 0.0).coerceAtLeast(1.0)
        _comparisonMonths.value = last3.map { (label, amount, isCurrent) ->
            PeriodTotal(
                label = label, amount = amount, isCurrent = isCurrent,
                pct = if (amount > 0) maxOf((amount / maxComp * 100).roundToInt(), 6) else 0
            )
        }
    }
}

// Mirrors analytics.component.ts#formatShort exactly (compact K/M suffixes, "" for zero).
fun formatShort(currency: String, n: Double): String {
    if (n == 0.0) return ""
    val sym = currencySymbol(currency)
    return when {
        n >= 1_000_000 -> "$sym${"%.1f".format(n / 1_000_000)}M"
        n >= 1_000 -> "$sym${(n / 1_000).roundToInt()}K"
        else -> "$sym${n.roundToInt()}"
    }
}
