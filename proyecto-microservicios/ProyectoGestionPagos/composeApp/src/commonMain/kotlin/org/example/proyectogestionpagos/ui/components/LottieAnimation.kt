package org.example.proyectogestionpagos.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun LottieAnimation(
    modifier: Modifier = Modifier,
    iterations: Int = Int.MAX_VALUE
)
