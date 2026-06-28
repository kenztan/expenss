package com.expenss.tracker.ui.contact

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expenss.tracker.data.network.AuthInterceptor
import com.expenss.tracker.data.network.ContactRequest
import com.expenss.tracker.data.network.RetrofitClient
import com.expenss.tracker.util.TokenManager
import com.expenss.tracker.util.mapBackendError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ContactState {
    object Idle : ContactState()
    object Loading : ContactState()
    data class Success(val message: String) : ContactState()
    data class Error(val message: String) : ContactState()
}

class ContactViewModel(context: Context) : ViewModel() {
    private val tokenManager = TokenManager(context)
    private val api = RetrofitClient.create(AuthInterceptor(tokenManager))

    private val _state = MutableStateFlow<ContactState>(ContactState.Idle)
    val state: StateFlow<ContactState> = _state

    fun send(email: String, message: String) {
        viewModelScope.launch {
            _state.value = ContactState.Loading
            try {
                val res = api.sendContact(ContactRequest(email, message))
                _state.value = ContactState.Success(res.message)
            } catch (e: Exception) {
                _state.value = ContactState.Error(mapBackendError(e))
            }
        }
    }
}
