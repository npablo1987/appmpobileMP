package org.example.proyectogestionpagos.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import proyectogestionpagos.composeapp.generated.resources.Res
import proyectogestionpagos.composeapp.generated.resources.ic_alternate_email
import proyectogestionpagos.composeapp.generated.resources.ic_mail
import proyectogestionpagos.composeapp.generated.resources.ic_share
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun SocialButton(
    symbol: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val icon: DrawableResource = when (symbol.uppercase()) {
        "G" -> Res.drawable.ic_mail
        "F" -> Res.drawable.ic_alternate_email
        else -> Res.drawable.ic_share
    }

    Box(
        modifier = modifier
            .height(52.dp)
            .border(1.dp, Color(0xFFE3E4EB), RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = "Acceso social",
            tint = tint,
        )
    }
}
