package com.corporate.feature.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state
}

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false
)

interface LoginRepository {
    suspend fun login(email: String, password: String): Boolean
}
