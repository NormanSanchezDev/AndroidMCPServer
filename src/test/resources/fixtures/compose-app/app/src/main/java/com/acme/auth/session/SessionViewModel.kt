package com.acme.auth.session

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed interface SessionState {
    data object Loading : SessionState
    data object Authenticated : SessionState
    data object Expired : SessionState
}

class SessionViewModel : ViewModel() {

    private val _session = MutableStateFlow<SessionState>(SessionState.Loading)
    val session: StateFlow<SessionState> = _session

    fun markAuthenticated() {
        _session.value = SessionState.Authenticated
    }

    fun markExpired() {
        _session.value = SessionState.Expired
    }
}