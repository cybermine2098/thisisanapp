package com.cyber98.thisisanapp

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import org.json.JSONObject
import com.cyber98.thisisanapp.ui.theme.ThisIsAnAppTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThisIsAnAppTheme {
                Scaffold { innerPadding -> 
                    Box(modifier = Modifier.padding(innerPadding)) {
                        OverviewScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val customFont = FontFamily(Font(R.font.my_custom_font))
    var userData by remember { mutableStateOf<JSONObject?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    
    LaunchedEffect(refreshTrigger) {
        val pref = context.getSharedPreferences("loginCache", Context.MODE_PRIVATE)
        val username = pref.getString("username", null)
        val password = pref.getString("password", null)
        if (username == null || password == null) {
            loading = false
            return@LaunchedEffect
        }
        val result = loginRequest(username, password)
        if (result == null) {
            errorMessage = "you are offline"
        } else if (result.getBoolean("success")) {
            userData = result.getJSONObject("data")
            errorMessage = null
        } else {
            pref.edit().clear().apply()
            errorMessage = result.getString("message") ?: "Login failed"
        }
        loading = false
    }
    
    when {
        loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        userData != null -> {
            // Logged in state: use the outer context for clickable calls.
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = "https://api.cyber98.dev/profiles/${userData!!.optString("profileImage", "")}",
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .align(Alignment.TopEnd)
                        .clickable {
                            val intent = android.content.Intent(context, ProfileActivity::class.java)
                            intent.putExtra("userData", userData.toString())
                            context.startActivity(intent)
                        }
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome, ${userData?.optString("username", "User")}!",
                        style = TextStyle(fontFamily = customFont)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        val pref = context.getSharedPreferences("loginCache", Context.MODE_PRIVATE)
                        pref.edit().clear().apply()
                        userData = null
                    }) {
                        Text("Sign Out", style = TextStyle(fontFamily = customFont))
                    }
                }
            }
        }
        else -> {
            // Not logged in state: no profile icon, remove refresh button and use a long Log in button at the bottom.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(errorMessage ?: "You are not logged in", style = TextStyle(fontFamily = customFont))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        context.startActivity(
                            android.content.Intent(context, LoginActivity::class.java)
                        )
                    }
                ) {
                    Text("Log in", style = TextStyle(fontFamily = customFont))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewOverview() {
    ThisIsAnAppTheme {
        OverviewScreen()
    }
}

