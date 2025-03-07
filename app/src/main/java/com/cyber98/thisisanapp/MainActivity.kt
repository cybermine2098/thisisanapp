package com.cyber98.thisisanapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cyber98.thisisanapp.ui.theme.ThisIsAnAppTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight


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
        while (true) {
            currentTime.value = LocalDateTime.now()
            delay(1000) // Update every second
        }
    }

    // Load custom font
    val customFont = FontFamily(Font(R.font.my_custom_font, FontWeight.Normal))
    //val customFont = FontFamily.Default
    // Time and date strings
    val timeString = currentTime.value.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    val dateString = currentTime.value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    // Layout for centering the clock
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween, // Places date at bottom
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f)) // Pushes time to center

        // Large, centered time display
        Text(
            text = timeString,
            fontFamily = customFont,
            fontSize = 64.sp, // Adjust as needed
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = TextStyle(letterSpacing = 4.sp) // Optional for spacing
        )

        Spacer(modifier = Modifier.weight(1f)) // Pushes date to bottom

        // Small date display at bottom
        Text(
            text = dateString,
            fontFamily = customFont,
            fontSize = 24.sp, // Adjust as needed
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

