package org.example.proyectogestionpagos.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import proyectogestionpagos.composeapp.generated.resources.Res
import proyectogestionpagos.composeapp.generated.resources.top_banner

@Composable
fun TopBanner() {
    Image(
        painter = painterResource(Res.drawable.top_banner),
        contentDescription = "Top Banner",
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp),
        contentScale = ContentScale.FillBounds
    )
}
