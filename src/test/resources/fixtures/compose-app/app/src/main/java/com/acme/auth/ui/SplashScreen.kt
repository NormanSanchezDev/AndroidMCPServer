package com.acme.auth.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.acme.auth.session.SessionState
import com.acme.auth.session.SessionViewModel

@Composable
fun SplashScreen(navController: NavHostController) {
    val sessionViewModel: SessionViewModel = viewModel()
    val session by sessionViewModel.session.collectAsState()

    when (session) {
        is SessionState.Authenticated -> navController.navigate("home")
        is SessionState.Expired -> navController.navigate("login")
        SessionState.Loading -> Unit
    }

    Box(modifier = Modifier.fillMaxSize().testTag("splash_screen")) {
        Text("Welcome to Acme")
    }
}