package org.example.proyectogestionpagos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.proyectogestionpagos.data.session.SessionManager
import org.example.proyectogestionpagos.navigation.AppDestination
import org.example.proyectogestionpagos.navigation.rememberAppNavigator
import org.example.proyectogestionpagos.ui.components.MenuTransitionScreen
import org.example.proyectogestionpagos.ui.components.TopBanner
import org.example.proyectogestionpagos.ui.screens.HomeScreen
import org.example.proyectogestionpagos.ui.screens.InvoiceDetailScreen
import org.example.proyectogestionpagos.ui.screens.LoginScreen
import org.example.proyectogestionpagos.ui.screens.PaymentScreen
import org.example.proyectogestionpagos.ui.screens.PaymentDirectScreen
import org.example.proyectogestionpagos.ui.screens.ProfileScreen

@Composable
fun App() {
    MaterialTheme {
        val navigator = rememberAppNavigator()
        var showTransition by remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF7F7FB),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopBanner()

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 10.dp),
                    color = Color(0xFFF7F7FB),
                ) {
                    if (showTransition) {
                        MenuTransitionScreen(
                            onTransitionComplete = {
                                showTransition = false
                            }
                        )
                    } else {
                        when (navigator.currentDestination) {
                            AppDestination.Login -> LoginScreen(
                                onLoginSuccess = {
                                    showTransition = true
                                    navigator.clearAndNavigateTo(AppDestination.Home)
                                },
                            )

                            AppDestination.Home -> HomeScreen(
                                onLogout = {
                                    SessionManager.clearSession()
                                    navigator.clearAndNavigateTo(AppDestination.Login)
                                },
                                onProfileClick = { navigator.navigateTo(AppDestination.Profile) },
                                onOpenInvoiceDetail = { navigator.navigateTo(AppDestination.InvoiceDetail) },
                                onGoToPayment = { navigator.navigateTo(AppDestination.Payment) },
                                onGoToPaymentDirect = { navigator.navigateTo(AppDestination.PaymentDirect) },
                                onBack = { navigator.navigateBack() },
                            )

                            AppDestination.Profile -> ProfileScreen(
                                onBackToHome = { navigator.navigateBack() },
                                onLogout = {
                                    SessionManager.clearSession()
                                    navigator.clearAndNavigateTo(AppDestination.Login)
                                },
                            )

                            AppDestination.InvoiceDetail -> InvoiceDetailScreen(
                                onBack = { navigator.navigateBack() },
                                onGoToPayment = { navigator.navigateTo(AppDestination.Payment) },
                            )

                            AppDestination.Payment -> PaymentScreen(
                                onBack = { navigator.navigateBack() },
                            )

                            AppDestination.PaymentDirect -> PaymentDirectScreen(
                                onBack = { navigator.navigateBack() },
                                onPaymentSuccess = { navigator.navigateBack() },
                            )
                        }
                    }
                }
            }
        }
    }
}
