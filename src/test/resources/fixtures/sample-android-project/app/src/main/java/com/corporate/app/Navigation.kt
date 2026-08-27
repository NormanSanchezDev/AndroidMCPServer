package com.corporate.app

import androidx.compose.runtime.Composable

@Composable
fun AppNavHost() {
    androidx.navigation.compose.NavHost(
        navController = androidx.navigation.compose.rememberNavController(),
        startDestination = "home"
    ) {
        composable("home") { HomeScreen() }
        composable("login") { LoginScreen() }
        composable("profile/{userId}") { /* profile */ }
    }
}

@Composable
fun HomeScreen() {}

@Composable
fun LoginScreen() {}
