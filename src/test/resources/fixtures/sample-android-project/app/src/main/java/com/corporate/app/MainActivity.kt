package com.corporate.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.corporate.data.UserRepository

class MainActivity : AppCompatActivity() {

    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userRepository = (application as CorporateApplication).userRepository
        renderProfile(userRepository.currentUser())
    }

    private fun renderProfile(user: User) {
        setTitle(R.string.app_name)
    }
}
