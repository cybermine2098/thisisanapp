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
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme

class DateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()  // Ensures edge-to-edge rendering with dark mode values
        setContent {
            ThisIsAnAppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    val customFont = FontFamily(Font(R.font.my_custom_font, FontWeight.Normal))
                    val currentDate = remember { LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) }
                    val isFinished = remember { mutableStateOf(false) } // new state to flag finish call
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures { _, dragAmount ->
                                    // Left-to-right swipe: return to time page only once
                                    if (dragAmount > 50f && !isFinished.value) {
                                        isFinished.value = true
                                        finish()
                                    }
                                }
                            }
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row {
                            Text(
                                text = currentDate,
                                fontFamily = customFont,
                                fontSize = 64.sp,
                                modifier = Modifier
                                .align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
        }
    }
}