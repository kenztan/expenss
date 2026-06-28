package com.expenss.tracker.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expenss.tracker.data.network.ApiService
import com.expenss.tracker.data.network.AuthInterceptor
import com.expenss.tracker.data.network.LoginResponse
import com.expenss.tracker.data.network.LoginRequest
import com.expenss.tracker.data.network.ForgotPasswordRequest
import com.expenss.tracker.data.network.RegisterRequest
import com.expenss.tracker.data.network.ResetPasswordRequest
import com.expenss.tracker.data.network.RetrofitClient
import com.expenss.tracker.data.network.SetTrackingModeRequest
import com.expenss.tracker.util.TokenManager
import com.expenss.tracker.util.mapBackendError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(context: Context) : ViewModel() {
    private val tokenManager = TokenManager(context)
    private val api: ApiService = RetrofitClient.create(AuthInterceptor(tokenManager))
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            try {
                api.register(RegisterRequest(username, email, password))
                _registerState.value = RegisterState.Success
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(parseError(e, "errors.unexpected"))
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = api.login(LoginRequest(username, password))
                tokenManager.saveToken(response.access_token)
                val profile = api.getMe()
                if (profile.onboardingCompleted) {
                    _loginState.value = LoginState.Success
                } else {
                    _loginState.value = LoginState.NeedsOnboarding
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(parseError(e, "errors.unexpected"))
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _forgotState.value = ForgotState.Loading
            try {
                api.forgotPassword(ForgotPasswordRequest(email))
                _forgotState.value = ForgotState.Success
            } catch (e: Exception) {
                _forgotState.value = ForgotState.Error(parseError(e, "errors.unexpected"))
            }
        }
    }

    private val _forgotState = MutableStateFlow<ForgotState>(ForgotState.Idle)
    val forgotState: StateFlow<ForgotState> = _forgotState

    fun setTrackingMode(currency: String, trackingMode: String, cycleStartDay: Int?) {
        viewModelScope.launch {
            _onboardingState.value = OnboardingState.Loading
            try {
                api.setTrackingMode(SetTrackingModeRequest(currency, trackingMode, cycleStartDay))
                _onboardingState.value = OnboardingState.Success
            } catch (e: Exception) {
                _onboardingState.value = OnboardingState.Error(parseError(e, "errors.unexpected"))
            }
        }
    }

    private val _onboardingState = MutableStateFlow<OnboardingState>(OnboardingState.Idle)
    val onboardingState: StateFlow<OnboardingState> = _onboardingState

    fun resetPassword(token: String, newPassword: String) {
        viewModelScope.launch {
            _resetPasswordState.value = ResetPasswordState.Loading
            try {
                api.resetPassword(ResetPasswordRequest(token, newPassword))
                _resetPasswordState.value = ResetPasswordState.Success
            } catch (e: Exception) {
                _resetPasswordState.value = ResetPasswordState.Error(parseError(e, "errors.unexpected"))
            }
        }
    }

    private val _resetPasswordState = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Idle)
    val resetPasswordState: StateFlow<ResetPasswordState> = _resetPasswordState

    fun verifyEmail(token: String) {
        viewModelScope.launch {
            _verifyEmailState.value = VerifyEmailState.Loading
            try {
                api.verifyEmail(token)
                _verifyEmailState.value = VerifyEmailState.Success
            } catch (e: Exception) {
                _verifyEmailState.value = VerifyEmailState.Error(parseError(e, "errors.unexpected"))
            }
        }
    }

    private val _verifyEmailState = MutableStateFlow<VerifyEmailState>(VerifyEmailState.Idle)
    val verifyEmailState: StateFlow<VerifyEmailState> = _verifyEmailState

    private fun parseError(e: Exception, fallbackKey: String): String = mapBackendError(e, fallbackKey)
}

sealed class LoginState {
    object Idle: LoginState()
    object Loading: LoginState()
    object Success: LoginState()
    object NeedsOnboarding: LoginState()
    data class Error(val message: String): LoginState()
}

sealed class ForgotState {
    object Idle: ForgotState()
    object Loading: ForgotState()
    object Success: ForgotState()
    data class Error(val message: String): ForgotState()
}

sealed class OnboardingState {
    object Idle: OnboardingState()
    object Loading: OnboardingState()
    object Success: OnboardingState()
    data class Error(val message: String): OnboardingState()
}

sealed class RegisterState {
    object Idle: RegisterState()
    object Loading: RegisterState()
    object Success: RegisterState()
    data class Error(val message: String): RegisterState()
}

sealed class ResetPasswordState {
    object Idle: ResetPasswordState()
    object Loading: ResetPasswordState()
    object Success: ResetPasswordState()
    data class Error(val message: String): ResetPasswordState()
}

sealed class VerifyEmailState {
    object Idle: VerifyEmailState()
    object Loading: VerifyEmailState()
    object Success: VerifyEmailState()
    data class Error(val message: String): VerifyEmailState()
}

