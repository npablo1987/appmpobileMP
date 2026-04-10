package org.example.proyectogestionpagos.utils

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual object PaymentLauncher {
    actual fun openPaymentUrl(url: String) {
        println("[PaymentLauncher][iOS] redirección a checkout: $url")
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(nsUrl)
    }
}
