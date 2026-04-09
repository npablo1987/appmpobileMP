package org.example.proyectogestionpagos.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun SocialButton(
    symbol: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val icon: ImageVector = when (symbol.uppercase()) {
        "G" -> Icons.Filled.Mail
        "F" -> Icons.Filled.AlternateEmail
        else -> Icons.Filled.Share
    }

    Box(
        modifier = modifier
            .height(52.dp)
            .border(1.dp, Color(0xFFE3E4EB), RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Acceso social",
            tint = tint,
        )
    }
}
