package com.acme.shop.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CheckoutUi {
    data object Loading : CheckoutUi()
    data object Success : CheckoutUi()
    data object Error : CheckoutUi()
}

data class CartItem(val id: String, val name: String, val priceCents: Int)

class CheckoutRepository {
    fun pay(cartId: String): Boolean = true
}

class CheckoutViewModel : ViewModel() {

    private val _state = MutableStateFlow<CheckoutUi>(CheckoutUi.Loading)
    val state: StateFlow<CheckoutUi> = _state

    fun submit(cartId: String) {
        viewModelScope.launch {
            val ok = CheckoutRepository().pay(cartId)
            _state.value = if (ok) CheckoutUi.Success else CheckoutUi.Error
        }
    }
}