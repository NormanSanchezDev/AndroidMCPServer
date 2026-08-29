package com.acme.auth.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.acme.auth.ui.HomeScreen
import com.acme.auth.ui.LoginScreen
import com.acme.auth.ui.SplashScreen

@Composable
fun AppNavRoot() {
    val navController: NavHostController = rememberNavController()
    NavHost(navController = navController, startDestination = "splash") {
        composable(route = "splash") { SplashScreen(navController) }
        composable(route = "login") { LoginScreen(navController) }
        composable(route = "home") { HomeScreen() }
    }
}