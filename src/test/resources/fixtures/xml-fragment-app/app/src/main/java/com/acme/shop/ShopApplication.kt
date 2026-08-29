package com.acme.shop

import android.app.Application

class ShopApplication : Application() {

    companion object {
        val session = SessionStore()
    }
}

class SessionStore {
    var token: String? = null
}