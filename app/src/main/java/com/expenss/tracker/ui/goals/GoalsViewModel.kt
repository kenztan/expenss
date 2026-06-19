package com.expenss.tracker.ui.goals

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expenss.tracker.data.network.*
import com.expenss.tracker.util.TokenManager
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GoalsViewModel(context: Context) : ViewModel() {
    private val tokenManager = TokenManager(context)
    private val api = RetrofitClient.create(AuthInterceptor(tokenManager))

    private val _dream = MutableStateFlow<Dream?>(null)
    val dream: StateFlow<Dream?> = _dream

    private val _totalSavings = MutableStateFlow(0.0)
    val totalSavings: StateFlow<Double> = _totalSavings

    private val _avgMonthlySavings = MutableStateFlow(0.0)
    val avgMonthlySavings: StateFlow<Double> = _avgMonthlySavings

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _currency = MutableStateFlow("JPY")
    val currency: StateFlow<String> = _currency

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            try {
                val user = api.getMe()
                _username.value = user.username
                _currency.value = user.currency
            } catch (_: Exception) {}

            // Run both calls in parallel, wait for both before hiding skeleton
            val dreamJob = async {
                try { _dream.value = api.getDream() } catch (_: Exception) { _dream.value = null }
            }
            val savingsJob = async {
                try {
                    val summary = api.getSavingsSummary(0.0)
                    _totalSavings.value = summary.totalSavings
                    _avgMonthlySavings.value = summary.avgMonthlySavings
                } catch (_: Exception) {}
            }
            dreamJob.await()
            savingsJob.await()
            _isLoading.value = false
        }
    }

    fun createDream(name: String, targetAmount: Double, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                _dream.value = api.createDream(CreateDreamRequest(name, targetAmount))
                onDone()
            } catch (_: Exception) {}
        }
    }

    fun editDream(id: Int, name: String, targetAmount: Double, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                _dream.value = api.createDream(CreateDreamRequest(name, targetAmount))
                onDone()
            } catch (_: Exception) {}
        }
    }

    fun deleteDream() {
        viewModelScope.launch {
            try { api.deleteDream(); _dream.value = null } catch (_: Exception) {}
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
