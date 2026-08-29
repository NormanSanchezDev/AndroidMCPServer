package com.acme.auth.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag

@Composable
fun HomeScreen() {
    Column(modifier = Modifier.fillMaxSize().testTag("home_screen")) {
        Text("Welcome home")
        Icon(imageVector = Icons.Default.Settings as ImageVector, contentDescription = "Settings")
    }
}