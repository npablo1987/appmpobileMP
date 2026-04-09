package org.example.proyectogestionpagos.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import proyectogestionpagos.composeapp.generated.resources.Res
import proyectogestionpagos.composeapp.generated.resources.ic_login

@Composable
actual fun LottieAnimation(
    modifier: Modifier,
    iterations: Int
) {
    Image(
        painter = painterResource(Res.drawable.ic_login),
        contentDescription = "Logo",
        modifier = modifier
    )
}
