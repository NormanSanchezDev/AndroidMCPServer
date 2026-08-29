package com.acme.shop.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.acme.shop.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.openCartButton).setOnClickListener {
            val intent = Intent(this, CheckoutActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.signOutButton).setOnClickListener {
            finish()
        }
    }

    fun showSupport() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SupportFragment())
            .addToBackStack(null)
            .commit()
    }

    fun showUndefined() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, OrderHistoryFragment())
            .addToBackStack(null)
            .commit()
    }
}