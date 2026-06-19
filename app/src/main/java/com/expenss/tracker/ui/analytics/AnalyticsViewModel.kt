package com.expenss.tracker.ui.analytics

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expenss.tracker.data.network.*
import com.expenss.tracker.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AnalyticsViewModel(context: Context) : ViewModel() {
    private val tokenManager = TokenManager(context)
    private val api = RetrofitClient.create(AuthInterceptor(tokenManager))

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _currency = MutableStateFlow("JPY")
    val currency: StateFlow<String> = _currency

    init {
        viewModelScope.launch {
            try {
                val user = api.getMe()
                _username.value = user.username
                _currency.value = user.currency
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
