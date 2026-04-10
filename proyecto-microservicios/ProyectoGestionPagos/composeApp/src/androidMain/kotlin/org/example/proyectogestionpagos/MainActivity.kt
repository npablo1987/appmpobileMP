package org.example.proyectogestionpagos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        MainActivityHolder.activity = this

        setContent {
            App()
        }
    }
    override fun onDestroy() {
        MainActivityHolder.activity = null
        super.onDestroy()
    }

}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}