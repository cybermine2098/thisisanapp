package com.cyber98.thisisanapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cyber98.thisisanapp.ui.theme.ThisIsAnAppTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThisIsAnAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ClockDisplay(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun ClockDisplay(modifier: Modifier = Modifier) {
    val currentTime = remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while(true) {
            currentTime.value = LocalDateTime.now()
            delay(1000)
        }
    }
    Text(
        text = currentTime.value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
        modifier = modifier
    )
}

