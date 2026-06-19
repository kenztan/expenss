package com.expenss.tracker.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expenss.tracker.data.network.ApiService
import com.expenss.tracker.data.network.AuthInterceptor
import com.expenss.tracker.data.network.LoginResponse
import com.expenss.tracker.data.network.LoginRequest
import com.expenss.tracker.data.network.RetrofitClient
import com.expenss.tracker.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(context: Context) : ViewModel() {
    private val tokenManager = TokenManager(context)
    private val api: ApiService = RetrofitClient.create(AuthInterceptor(tokenManager))
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(username: String, password: String){
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = api.login(LoginRequest(username, password))
                tokenManager.saveToken(response.access_token)
                _loginState.value = LoginState.Success
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Login failed")
            }
        }
    }
}

sealed class LoginState {
    object Idle: LoginState()
    object Loading: LoginState()
    object Success : LoginState()
    data class Error(val message: String): LoginState()
}

