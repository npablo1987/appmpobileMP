package org.example.proyectogestionpagos

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.example.proyectogestionpagos.data.session.SessionManager
import org.example.proyectogestionpagos.navigation.AppDestination
import org.example.proyectogestionpagos.navigation.rememberAppNavigator
import org.example.proyectogestionpagos.ui.screens.HomeScreen
import org.example.proyectogestionpagos.ui.screens.LoginScreen
import org.example.proyectogestionpagos.ui.screens.ProfileScreen

@Composable
fun App() {
    MaterialTheme {
        val navigator = rememberAppNavigator()

        Surface(
            modifier = Modifier,
            color = Color(0xFFF7F7FB),
        ) {
            when (navigator.currentDestination) {
                AppDestination.Login -> LoginScreen(
                    onLoginSuccess = { navigator.navigateTo(AppDestination.Home) },
                )

                AppDestination.Home -> HomeScreen(
                    onLogout = {
                        SessionManager.clearSession()
                        navigator.navigateTo(AppDestination.Login)
                    },
                    onProfileClick = { navigator.navigateTo(AppDestination.Profile) },
                )

                AppDestination.Profile -> ProfileScreen(
                    onBackToHome = { navigator.navigateTo(AppDestination.Home) },
                    onLogout = {
                        SessionManager.clearSession()
                        navigator.navigateTo(AppDestination.Login)
                    },
                )
            }
        }
    }
}
