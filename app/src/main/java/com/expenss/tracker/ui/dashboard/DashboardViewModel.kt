package com.expenss.tracker.ui.dashboard

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

class DashboardViewModel(context: Context) : ViewModel() {
    private val tokenManager = TokenManager(context)
    private val api: ApiService = RetrofitClient.create(AuthInterceptor(tokenManager))

    private val now = Calendar.getInstance()

    // Tracking mode
    private var trackingMode = "monthly"
    private var cycleStartDay = 1
    private var cycleOffset = 0
    private var cycleStartDate = Date()
    private var cycleEndDate = Date()

    // Monthly mode navigation
    private var currentMonth = now.get(Calendar.MONTH) + 1
    private var currentYear  = now.get(Calendar.YEAR)

    private val _periodLabel = MutableStateFlow("")
    val periodLabel: StateFlow<String> = _periodLabel

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses

    private val _budget = MutableStateFlow(0.0)
    val budget: StateFlow<Double> = _budget

    private val _categoryBudgets = MutableStateFlow<Map<String, Double>>(emptyMap())
    val categoryBudgets: StateFlow<Map<String, Double>> = _categoryBudgets

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _currency = MutableStateFlow("JPY")
    val currency: StateFlow<String> = _currency

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        updatePeriodLabel()
        loadAll()
    }

    fun prevMonth() {
        if (trackingMode == "paycycle") {
            cycleOffset--
            calcCycleDates()
        } else {
            if (currentMonth == 1) { currentMonth = 12; currentYear-- }
            else currentMonth--
        }
        updatePeriodLabel()
        loadExpensesAndBudget()
    }

    fun nextMonth() {
        if (trackingMode == "paycycle") {
            cycleOffset++
            calcCycleDates()
        } else {
            if (currentMonth == 12) { currentMonth = 1; currentYear++ }
            else currentMonth++
        }
        updatePeriodLabel()
        loadExpensesAndBudget()
    }

    private fun calcCycleDates() {
        val today = Calendar.getInstance()
        var month = today.get(Calendar.MONTH)
        var year  = today.get(Calendar.YEAR)

        if (today.get(Calendar.DAY_OF_MONTH) < cycleStartDay) {
            if (month == 0) { month = 11; year-- } else month--
        }

        month += cycleOffset
        while (month > 11) { month -= 12; year++ }
        while (month < 0)  { month += 12; year-- }

        val startCal = Calendar.getInstance()
        startCal.set(year, month, cycleStartDay)
        cycleStartDate = startCal.time

        val endMonth = if (month == 11) 0 else month + 1
        val endYear  = if (month == 11) year + 1 else year
        val endCal = Calendar.getInstance()
        endCal.set(endYear, endMonth, cycleStartDay - 1)
        cycleEndDate = endCal.time
    }

    private fun updatePeriodLabel() {
        if (trackingMode == "paycycle") {
            val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
            _periodLabel.value = "${fmt.format(cycleStartDate)} – ${fmt.format(cycleEndDate)}"
        } else {
            val cal = Calendar.getInstance()
            cal.set(currentYear, currentMonth - 1, 1)
            _periodLabel.value = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
        }
    }

    fun loadAll() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = api.getMe()
                _username.value = user.username
                _currency.value = user.currency
                trackingMode  = user.trackingMode ?: "monthly"
                cycleStartDay = user.cycleStartDay ?: 1
                if (trackingMode == "paycycle") calcCycleDates()
                updatePeriodLabel()
            } catch (e: Exception) {
                _error.value = e.message
            }
            loadExpensesAndBudget()
        }
    }

    // Budget records are always keyed by calendar month/year. In paycycle mode that's
    // the month/year of the cycle start date; in monthly mode it's the selected month.
    private fun budgetMonth(): Int =
        if (trackingMode == "paycycle")
            Calendar.getInstance().also { it.time = cycleStartDate }.get(Calendar.MONTH) + 1
        else currentMonth

    private fun budgetYear(): Int =
        if (trackingMode == "paycycle")
            Calendar.getInstance().also { it.time = cycleStartDate }.get(Calendar.YEAR)
        else currentYear

    private fun loadExpensesAndBudget() {
        viewModelScope.launch {
            _isLoading.value = true
            // Each call is independent — mirrors the web dashboard, where expenses, budget and
            // category budgets are three separate subscriptions. A failure in one must never
            // prevent the others from loading (this is what previously zeroed the budget).
            try {
                _expenses.value = if (trackingMode == "paycycle") {
                    val isoFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    api.getPaydayExpenses(isoFmt.format(cycleStartDate), isoFmt.format(cycleEndDate))
                } else {
                    api.getExpenses(currentMonth, currentYear)
                }
            } catch (e: Exception) { _error.value = e.message }

            try {
                val b = api.getBudget(budgetMonth(), budgetYear())
                _budget.value = b?.amount ?: 0.0
            } catch (e: Exception) { _error.value = e.message }

            try {
                _categoryBudgets.value = api.getCategoryBudgets(budgetMonth(), budgetYear())
            } catch (e: Exception) { _categoryBudgets.value = emptyMap() }

            _isLoading.value = false
        }
    }

    fun deleteExpense(id: Int) {
        viewModelScope.launch {
            try {
                api.deleteExpense(id)
                _expenses.value = _expenses.value.filter { it.id != id }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun createExpense(name: String, amount: Double, category: String, date: String, note: String?, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                api.createExpense(CreateExpenseRequest(name, amount, category, date, note))
                onDone()
                loadExpensesAndBudget()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun setMonthlyBudget(amount: Double, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                api.setBudget(SetBudgetRequest(budgetMonth(), budgetYear(), amount))
                onDone()
                loadExpensesAndBudget()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun setCategoryBudget(category: String, amount: Double, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                api.setCategoryBudget(SetCategoryBudgetRequest(budgetMonth(), budgetYear(), category, amount))
                onDone()
                loadExpensesAndBudget()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun deleteMonthlyBudget(onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                api.deleteMonthlyBudget(budgetMonth(), budgetYear())
                onDone()
                loadExpensesAndBudget()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun deleteCategoryBudget(category: String) {
        viewModelScope.launch {
            try {
                api.deleteCategoryBudget(DeleteCategoryBudgetRequest(budgetMonth(), budgetYear(), category))
                loadExpensesAndBudget()
            } catch (e: Exception) { _error.value = e.message }
        }
    }

    fun getDailyAvg(totalSpent: Double): Double {
        val now = Calendar.getInstance()
        if (trackingMode == "paycycle") {
            val startMs = cycleStartDate.time
            val endMs   = cycleEndDate.time
            val nowMs   = now.timeInMillis
            val refMs   = when {
                nowMs < startMs -> endMs
                nowMs > endMs   -> endMs
                else            -> nowMs
            }
            val days = ((refMs - startMs) / 86400000.0).toLong() + 1
            return if (days > 0) totalSpent / days else 0.0
        }
        val isCurrentMonth = currentYear == now.get(Calendar.YEAR) && currentMonth == now.get(Calendar.MONTH) + 1
        val isFutureMonth  = currentYear > now.get(Calendar.YEAR) ||
            (currentYear == now.get(Calendar.YEAR) && currentMonth > now.get(Calendar.MONTH) + 1)
        if (isFutureMonth) return 0.0
        val divisor = if (isCurrentMonth) now.get(Calendar.DAY_OF_MONTH).toDouble()
                      else Calendar.getInstance().also { it.set(currentYear, currentMonth, 0) }
                          .get(Calendar.DAY_OF_MONTH).toDouble()
        return if (divisor > 0) totalSpent / divisor else 0.0
    }

    fun getDaysLeft(): Int {
        val now = Calendar.getInstance()
        if (trackingMode == "paycycle") {
            return maxOf(((cycleEndDate.time - now.timeInMillis) / 86400000.0).toInt(), 0)
        }
        val lastDay = Calendar.getInstance().also { it.set(currentYear, currentMonth, 0) }
            .get(Calendar.DAY_OF_MONTH)
        return maxOf(lastDay - now.get(Calendar.DAY_OF_MONTH), 0)
    }

    fun setCurrency(code: String) {
        _currency.value = code  // optimistic — update UI immediately
        viewModelScope.launch {
            try { api.setCurrency(SetCurrencyRequest(code)) }
            catch (e: Exception) { _error.value = e.message }
        }
    }

    fun editExpense(id: Int, name: String, amount: Double, category: String, date: String, note: String?, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                api.updateExpense(id, CreateExpenseRequest(name, amount, category, date, note))
                onDone()
                loadExpensesAndBudget()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
