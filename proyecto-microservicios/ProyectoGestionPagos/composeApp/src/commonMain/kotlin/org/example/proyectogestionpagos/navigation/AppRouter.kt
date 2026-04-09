package org.example.proyectogestionpagos.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

sealed interface AppRoute {
    data object Login : AppRoute
    data object Home : AppRoute
}

@Stable
class AppRouter(
    startDestination: AppRoute,
) {
    private val backStack = mutableStateListOf(startDestination)

    val currentRoute: AppRoute
        get() = backStack.last()

    fun navigateTo(route: AppRoute) {
        if (route != currentRoute) {
            backStack += route
        }
    }

    fun replace(route: AppRoute) {
        backStack.clear()
        backStack += route
    }

    fun canPop(): Boolean = backStack.size > 1

    fun pop(): Boolean {
        if (!canPop()) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }
}

@Composable
fun rememberAppRouter(
    startDestination: AppRoute = AppRoute.Login,
): AppRouter = remember { AppRouter(startDestination = startDestination) }
