package com.expenss.tracker.ui.savings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expenss.tracker.data.network.*
import com.expenss.tracker.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SavingsViewModel(context: Context) : ViewModel() {
    private val tokenManager = TokenManager(context)
    private val api = RetrofitClient.create(AuthInterceptor(tokenManager))

    private val _records = MutableStateFlow<List<Saving>>(emptyList())
    val records: StateFlow<List<Saving>> = _records

    private val _totalSavings = MutableStateFlow(0.0)
    val totalSavings: StateFlow<Double> = _totalSavings

    private val _monthlyRemaining = MutableStateFlow(0.0)
    val monthlyRemaining: StateFlow<Double> = _monthlyRemaining

    private val _monthlyCommitment = MutableStateFlow(0.0)
    val monthlyCommitment: StateFlow<Double> = _monthlyCommitment

    private val _avgMonthlySavings = MutableStateFlow(0.0)
    val avgMonthlySavings: StateFlow<Double> = _avgMonthlySavings

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _currency = MutableStateFlow("JPY")
    val currency: StateFlow<String> = _currency

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // paycycle fields
    private var trackingMode  = "monthly"
    private var cycleStartDay = 1

    init {
        viewModelScope.launch {
            try {
                val user = api.getMe()
                _username.value = user.username
                _currency.value = user.currency
                _monthlyCommitment.value = user.monthlyCommitment ?: 0.0
                trackingMode  = user.trackingMode ?: "monthly"
                cycleStartDay = user.cycleStartDay ?: 1
            } catch (_: Exception) {}
            loadAll()
        }
    }

    private fun cycleDates(): Pair<String, String> {
        val today = Calendar.getInstance()
        var month = today.get(Calendar.MONTH)
        var year  = today.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_MONTH) < cycleStartDay) {
            if (month == 0) { month = 11; year-- } else month--
        }
        val startCal = Calendar.getInstance().also { it.set(year, month, cycleStartDay) }
        val endMonth = if (month == 11) 0 else month + 1
        val endYear  = if (month == 11) year + 1 else year
        val endCal = Calendar.getInstance().also { it.set(endYear, endMonth, cycleStartDay - 1) }
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return fmt.format(startCal.time) to fmt.format(endCal.time)
    }

    fun loadAll() {
        viewModelScope.launch {
            _isLoading.value = true

            // Compute monthly remaining independently so a budget/expense failure
            // doesn't block the savings list from loading.
            try {
                val now = Calendar.getInstance()
                val expenses = if (trackingMode == "paycycle") {
                    val (s, e) = cycleDates()
                    api.getPaydayExpenses(s, e)
                } else {
                    api.getExpenses(now.get(Calendar.MONTH) + 1, now.get(Calendar.YEAR))
                }
                val budgetMonth = if (trackingMode == "paycycle") {
                    val (s, _) = cycleDates()
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(s)
                        ?.let { Calendar.getInstance().also { c -> c.time = it }.get(Calendar.MONTH) + 1 }
                        ?: (now.get(Calendar.MONTH) + 1)
                } else now.get(Calendar.MONTH) + 1
                val budgetYear = if (trackingMode == "paycycle") {
                    val (s, _) = cycleDates()
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(s)
                        ?.let { Calendar.getInstance().also { c -> c.time = it }.get(Calendar.YEAR) }
                        ?: now.get(Calendar.YEAR)
                } else now.get(Calendar.YEAR)
                val budget = api.getBudget(budgetMonth, budgetYear)
                val spent  = expenses.sumOf { it.amount }
                _monthlyRemaining.value = if (budget != null) maxOf(0.0, budget.amount - spent) else 0.0
            } catch (_: Exception) {}

            try {
                val summary = api.getSavingsSummary(_monthlyRemaining.value)
                _totalSavings.value      = summary.totalSavings
                _avgMonthlySavings.value = summary.avgMonthlySavings
            } catch (_: Exception) {}

            // Records load independently — never blocked by budget/expense failures.
            try {
                _records.value = api.getSavings()
            } catch (_: Exception) {}

            _isLoading.value = false
        }
    }

    fun createSaving(amount: Double, date: String, note: String?, onDone: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                api.createSaving(CreateSavingRequest(amount, date, note))
                onDone()
                loadAll()
            } catch (_: Exception) {}
            _isLoading.value = false
        }
    }

    fun deleteSaving(id: Int) {
        viewModelScope.launch {
            try {
                api.deleteSaving(id)
                _records.value = _records.value.filter { it.id != id }
            } catch (_: Exception) {}
        }
    }

    fun setCommitment(amount: Double, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                api.setCommitment(SetCommitmentRequest(amount))
                _monthlyCommitment.value = amount
                onDone()
            } catch (_: Exception) {}
        }
    }

    fun setCurrency(code: String) {
        _currency.value = code
        viewModelScope.launch {
            try { api.setCurrency(SetCurrencyRequest(code)) }
            catch (_: Exception) {}
        }
    }
}
