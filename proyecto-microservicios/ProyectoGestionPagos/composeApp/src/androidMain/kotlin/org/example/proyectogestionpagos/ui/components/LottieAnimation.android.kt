package org.example.proyectogestionpagos.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import org.example.proyectogestionpagos.R

@Composable
actual fun LottieAnimation(
    modifier: Modifier,
    iterations: Int
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.roketflight)
    )
    
    LottieAnimation(
        composition = composition,
        modifier = modifier,
        iterations = LottieConstants.IterateForever
    )
}
