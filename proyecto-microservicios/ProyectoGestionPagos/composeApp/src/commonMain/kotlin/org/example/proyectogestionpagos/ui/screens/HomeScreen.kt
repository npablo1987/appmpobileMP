package org.example.proyectogestionpagos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.proyectogestionpagos.ui.components.AppBottomBar
import org.example.proyectogestionpagos.ui.components.CategoryItem
import org.example.proyectogestionpagos.ui.components.CourseStatusRow
import org.example.proyectogestionpagos.ui.components.InfoPill
import org.example.proyectogestionpagos.ui.theme.AppColors

@Composable
fun HomeScreen(
    onOpenProfile: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("Hola,", color = Color(0xFF4E5270), style = MaterialTheme.typography.titleMedium)
                Text(
                    "Leah White",
                    color = AppColors.PrimaryDark,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF57C0D8)),
                contentAlignment = Alignment.Center,
            ) {
                Text("LW", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AppColors.Border, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Buscar", color = Color(0xFFB2B6C2))
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.Primary),
                contentAlignment = Alignment.Center,
            ) {
                Text("⌕", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            "Categorías",
            color = AppColors.PrimaryDark,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 4,
        ) {
            CategoryItem("UX", Color(0xFF8D85DB))
            CategoryItem("HCI", Color(0xFFF2BB2A))
            CategoryItem("Design", Color(0xFF4CCED1))
            CategoryItem("Motion", Color(0xFFEE7092))
        }

        Spacer(modifier = Modifier.height(22.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(14.dp),
            tonalElevation = 2.dp,
            shadowElevation = 1.dp,
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Jane Martin / Teacher",
                    color = AppColors.PrimaryDark,
                    fontWeight = FontWeight.SemiBold,
                )
                Text("UX Designer", color = Color(0xFF9498A6), style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Human-Computer Interaction - HCI",
                    color = Color(0xFF2F355D),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoPill(
                        title = "216,513",
                        subtitle = "Inscritos",
                        background = Color(0xFF1EA5F5),
                        modifier = Modifier.weight(1f),
                    )
                    InfoPill(
                        title = "18 horas",
                        subtitle = "8 semanas",
                        background = Color(0xFFE6699C),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        CourseStatusRow("Cursos activos", "3")
        Spacer(modifier = Modifier.height(10.dp))
        CourseStatusRow("Cursos finalizados", "2")

        Spacer(modifier = Modifier.height(24.dp))
        AppBottomBar(
            currentSection = "home",
            onHomeClick = {},
            onProfileClick = onOpenProfile,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryDark),
        ) {
            Text("Cerrar sesión")
        }
    }
}
