package org.example.proyectogestionpagos.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class AppNavigator(
    initialDestination: AppDestination = AppDestination.Login,
) {
    var currentDestination by mutableStateOf(initialDestination)
        private set

    fun navigateTo(destination: AppDestination) {
        currentDestination = destination
    }
}

@Composable
fun rememberAppNavigator(): AppNavigator = remember { AppNavigator() }
