package org.example.proyectogestionpagos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import proyectogestionpagos.composeapp.generated.resources.Res
import proyectogestionpagos.composeapp.generated.resources.ic_arrow_back
import proyectogestionpagos.composeapp.generated.resources.ic_logout
import proyectogestionpagos.composeapp.generated.resources.ic_person
import proyectogestionpagos.composeapp.generated.resources.ic_star
import proyectogestionpagos.composeapp.generated.resources.ic_verified
import org.example.proyectogestionpagos.ui.components.AppBottomBar
import org.example.proyectogestionpagos.ui.theme.AppColors
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun ProfileScreen(
    onBackToHome: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(Res.drawable.ic_person),
                contentDescription = "Perfil",
                tint = AppColors.PrimaryDark,
            )
            Text(
                text = "Perfil profesional",
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.PrimaryDark,
                fontWeight = FontWeight.Bold,
            )
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileItem(icon = Res.drawable.ic_person, label = "Nombre", value = "Leah White")
                ProfileItem(icon = Res.drawable.ic_verified, label = "Rol", value = "UX Designer")
                ProfileItem(icon = Res.drawable.ic_star, label = "Plan actual", value = "Pro")
                ProfileItem(icon = Res.drawable.ic_verified, label = "Estado", value = "Activo")
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        AppBottomBar(
            currentSection = "profile",
            onHomeClick = onBackToHome,
            onProfileClick = {},
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_back),
                contentDescription = "Volver al inicio",
                modifier = Modifier.padding(end = 8.dp),
            )
            Text("Volver al inicio")
        }

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryDark),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_logout),
                contentDescription = "Cerrar sesión",
                modifier = Modifier.padding(end = 8.dp),
            )
            Text("Cerrar sesión")
        }
    }
}

@Composable
private fun ProfileItem(
    icon: DrawableResource,
    label: String,
    value: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(icon),
            contentDescription = label,
            tint = Color(0xFF5F678B),
        )
        Text(
            text = "$label: $value",
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.PrimaryDark,
        )
    }
}
