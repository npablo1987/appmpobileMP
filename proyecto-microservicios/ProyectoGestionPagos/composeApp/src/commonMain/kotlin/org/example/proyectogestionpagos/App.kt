package org.example.proyectogestionpagos

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import org.example.proyectogestionpagos.navigation.AppRoute
import org.example.proyectogestionpagos.navigation.rememberAppRouter
import org.example.proyectogestionpagos.ui.screens.HomeScreen
import org.example.proyectogestionpagos.ui.screens.LoginScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        val router = rememberAppRouter(startDestination = AppRoute.Login)

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF7F7FB),
        ) {
            when (router.currentRoute) {
                AppRoute.Login -> LoginScreen(
                    onLoginSuccess = { router.navigateTo(AppRoute.Home) },
                )

                AppRoute.Home -> HomeScreen(
                    onLogout = { router.replace(AppRoute.Login) },
                )
            }
        }
    }
}
