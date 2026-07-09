package com.example.jhdkasjhd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.example.jhdkasjhd.navigation.QuickvntNavHost
import com.example.jhdkasjhd.ui.theme.JhdkasjhdTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JhdkasjhdTheme {
                QuickvntApp()
            }
        }
    }
}

@Composable
fun QuickvntApp() {
    QuickvntNavHost()
}
