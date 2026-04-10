package org.example.proyectogestionpagos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import org.example.proyectogestionpagos.R

@Composable
actual fun MenuTransitionScreen(onTransitionComplete: () -> Unit) {
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(900)
        isVisible = false
        onTransitionComplete()
    }

    if (isVisible) {
        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(R.raw.menupagetransition)
        )

        LottieAnimation(
            composition = composition,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            iterations = 1,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    }
}
