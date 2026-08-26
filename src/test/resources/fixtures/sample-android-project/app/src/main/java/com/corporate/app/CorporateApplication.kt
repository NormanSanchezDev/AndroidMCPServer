package com.corporate.app

import android.app.Application
import com.corporate.data.UserRepository

class CorporateApplication : Application() {

    val userRepository: UserRepository by lazy {
        UserRepository()
    }

    override fun onCreate() {
        super.onCreate()
        userRepository.warmUp()
    }
}
