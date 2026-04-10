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
    private val navigationStack = mutableListOf<AppDestination>()
    var currentDestination by mutableStateOf(initialDestination)
        private set

    init {
        navigationStack.add(initialDestination)
    }

    fun navigateTo(destination: AppDestination) {
        if (destination != currentDestination) {
            navigationStack.add(destination)
            currentDestination = destination
        }
    }

    fun navigateBack(): Boolean {
        if (navigationStack.size > 1) {
            navigationStack.removeLast()
            currentDestination = navigationStack.last()
            return true
        }
        return false
    }

    fun canGoBack(): Boolean = navigationStack.size > 1

    fun clearAndNavigateTo(destination: AppDestination) {
        navigationStack.clear()
        navigationStack.add(destination)
        currentDestination = destination
    }
}

@Composable
fun rememberAppNavigator(): AppNavigator = remember { AppNavigator() }
