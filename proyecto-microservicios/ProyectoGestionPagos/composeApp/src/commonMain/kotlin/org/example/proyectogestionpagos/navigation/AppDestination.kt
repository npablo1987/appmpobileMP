package org.example.proyectogestionpagos.navigation

sealed interface AppDestination {
    data object Login : AppDestination
    data object Home : AppDestination
    data object Profile : AppDestination
    data object InvoiceDetail : AppDestination
    data object Payment : AppDestination
}
