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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import android.content.Intent

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
    val hour = currentTime.value.format(DateTimeFormatter.ofPattern("HH"))
    val minute = currentTime.value.format(DateTimeFormatter.ofPattern("mm"))
    val second = currentTime.value.format(DateTimeFormatter.ofPattern("ss"))

    val displayedMinute = remember { mutableStateOf(minute) }
    val displayedSecond = remember { mutableStateOf(second) }
    val displayedHour = remember { mutableStateOf(hour) }
    val minuteRotation = remember { Animatable(0f) }
    val secondRotation = remember { Animatable(0f) }
    val hourRotation = remember { Animatable(0f) }
    val rotationtime = 150 // Time in milliseconds for rotation
    LaunchedEffect(minute) {
        minuteRotation.snapTo(0f)
        minuteRotation.animateTo(180f, animationSpec = tween(rotationtime))
        displayedMinute.value = minute
        minuteRotation.animateTo(360f, animationSpec = tween(rotationtime))
        minuteRotation.snapTo(0f)
    }
    LaunchedEffect(second) {
        secondRotation.snapTo(0f)
        secondRotation.animateTo(180f, animationSpec = tween(rotationtime))
        displayedSecond.value = second
        secondRotation.animateTo(360f, animationSpec = tween(rotationtime))
        secondRotation.snapTo(0f)
    }
    LaunchedEffect(hour){
        hourRotation.snapTo(0f)
        hourRotation.animateTo(180f, animationSpec = tween(rotationtime))
        displayedHour.value = hour
        hourRotation.animateTo(360f, animationSpec = tween(rotationtime))
        hourRotation.snapTo(0f)
    }

    val context = LocalContext.current

    // Layout for centering the clock
    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    // Swiped right launches DateActivity
                    if (dragAmount < 50f) {
                        context.startActivity(
                            Intent(context, DateActivity::class.java)
                        )
                    }
                }
            }
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween, // Places date at bottom
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f)) // Pushes time to center

        Row {
            Text(
                text = hour,
                fontFamily = customFont,
                fontSize = 64.sp, // Adjust as needed
                modifier = Modifier.align(Alignment.CenterVertically),
                style = TextStyle(letterSpacing = 4.sp) // Optional for spacing
            )
            Text(
                text = ":",
                fontFamily = customFont,
                fontSize = 64.sp, // Adjust as needed
                modifier = Modifier.align(Alignment.CenterVertically),
                style = TextStyle(letterSpacing = 4.sp) // Optional for spacing
            )
            Text(
                text = displayedMinute.value,
                fontFamily = customFont,
                fontSize = 64.sp, // Adjust as needed
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .graphicsLayer {
                        rotationX = minuteRotation.value
                        alpha = if (minuteRotation.value % 360f in 90f..270f) 0f else 1f
                    },
                style = TextStyle(letterSpacing = 4.sp) // Optional for spacing
            )
            Text(
                text = ":"+displayedSecond.value,
                fontFamily = customFont,
                fontSize = 64.sp, // Adjust as needed
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .graphicsLayer {
                        rotationX = secondRotation.value
                        alpha = if (secondRotation.value % 360f in 90f..270f) 0f else 1f
                    },
                style = TextStyle(letterSpacing = 4.sp) // Optional for spacing
            )
        }

        Spacer(modifier = Modifier.weight(1f)) // Pushes date to bottom
    }
}

