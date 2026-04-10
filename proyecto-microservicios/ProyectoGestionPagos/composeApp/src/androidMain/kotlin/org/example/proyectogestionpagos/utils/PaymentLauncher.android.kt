package org.example.proyectogestionpagos.utils

import android.content.Intent
import android.net.Uri
import org.example.proyectogestionpagos.MainActivityHolder

actual object PaymentLauncher {
    actual fun openPaymentUrl(url: String) {
        println("[PaymentLauncher][Android] redirección a checkout: $url")
        val activity = MainActivityHolder.activity ?: return
        activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
