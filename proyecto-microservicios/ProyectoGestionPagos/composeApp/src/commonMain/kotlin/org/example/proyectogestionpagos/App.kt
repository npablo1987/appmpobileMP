package org.example.proyectogestionpagos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class Screen {
    LOGIN,
    HOME,
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf(Screen.LOGIN) }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF7F7FB),
        ) {
            when (currentScreen) {
                Screen.LOGIN -> LoginScreen(onLoginSuccess = { currentScreen = Screen.HOME })
                Screen.HOME -> HomeScreen(onLogout = { currentScreen = Screen.LOGIN })
            }
        }
    }
}

@Composable
private fun LoginScreen(
    onLoginSuccess: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
        Spacer(modifier = Modifier.height(34.dp))

        Text(
            text = "Login to your\naccount.",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF27306D),
            lineHeight = 44.sp,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Hello, welcome back to your account",
            color = Color(0xFF9AA0AF),
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            placeholder = { Text("example@email.com") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            placeholder = { Text("Your Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                )
                Text(
                    text = "Remember me",
                    color = Color(0xFF8C8F9A),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Text(
                text = "Forgot Password?",
                color = Color(0xFF8C8F9A),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable { },
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onLoginSuccess,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6465F2),
                contentColor = Color.White,
            ),
        ) {
            Text("Login", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(34.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color(0xFFE2E4EA)),
            )
            Text(
                text = "  or sign up with  ",
                color = Color(0xFF9AA0AF),
                style = MaterialTheme.typography.bodySmall,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color(0xFFE2E4EA)),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SocialButton("f", Color(0xFF1877F2), Modifier.weight(1f))
            SocialButton("G", Color(0xFFDB4437), Modifier.weight(1f))
            SocialButton("", Color(0xFF141414), Modifier.weight(1f))
        }
    }
}

@Composable
private fun SocialButton(symbol: String, tint: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(52.dp)
            .border(1.dp, Color(0xFFE3E4EB), RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = symbol,
            color = tint,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun HomeScreen(
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
                Text("Howdy,", color = Color(0xFF4E5270), style = MaterialTheme.typography.titleMedium)
                Text(
                    "Leah White",
                    color = Color(0xFF27306D),
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
                .border(1.dp, Color(0xFFE3E5EB), RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Search", color = Color(0xFFB2B6C2))
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF6465F2)),
                contentAlignment = Alignment.Center,
            ) {
                Text("⌕", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            "Course Categories",
            color = Color(0xFF27306D),
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

        Text(
            "Enrolled courses",
            color = Color(0xFF27306D),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(10.dp))

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
                    color = Color(0xFF27306D),
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
                        subtitle = "Already enrolled",
                        background = Color(0xFF1EA5F5),
                        modifier = Modifier.weight(1f),
                    )
                    InfoPill(
                        title = "18 hours",
                        subtitle = "Over 8 weeks",
                        background = Color(0xFFE6699C),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        CourseStatusRow("Active courses", "3")
        Spacer(modifier = Modifier.height(10.dp))
        CourseStatusRow("Ended courses", "2")

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27306D)),
        ) {
            Text("Cerrar sesión")
        }
    }
}

@Composable
private fun CategoryItem(label: String, background: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(background),
            contentAlignment = Alignment.Center,
        ) {
            Text("◼", color = Color.White)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, color = Color(0xFF575D77), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun InfoPill(
    title: String,
    subtitle: String,
    background: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 12.dp),
    ) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(subtitle, color = Color.White.copy(alpha = 0.95f), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun CourseStatusRow(title: String, counter: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF0F1F6))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = Color(0xFF373D63),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Text(counter, color = Color(0xFF51587A), style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text("⌄", color = Color(0xFF51587A))
    }
}
