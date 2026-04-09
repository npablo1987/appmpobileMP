package org.example.proyectogestionpagos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import proyectogestionpagos.composeapp.generated.resources.Res
import proyectogestionpagos.composeapp.generated.resources.ic_home
import proyectogestionpagos.composeapp.generated.resources.ic_person
import org.jetbrains.compose.resources.painterResource

@Composable
fun AppBottomBar(
    currentSection: String,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomBarItem(
            label = "Inicio",
            isSelected = currentSection == "home",
            onClick = onHomeClick,
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_home),
                    contentDescription = "Inicio",
                )
            },
        )
        BottomBarItem(
            label = "Perfil",
            isSelected = currentSection == "profile",
            onClick = onProfileClick,
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_person),
                    contentDescription = "Perfil",
                )
            },
        )
    }
}

@Composable
private fun BottomBarItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    val tint = if (isSelected) Color(0xFF27306D) else Color(0xFF8C8F9A)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 8.dp),
    ) {
        CompositionLocalProvider(LocalContentColor provides tint) {
            icon()
        }
        Text(
            text = label,
            color = tint,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
