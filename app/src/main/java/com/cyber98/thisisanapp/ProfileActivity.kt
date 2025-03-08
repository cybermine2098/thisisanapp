package com.cyber98.thisisanapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import com.cyber98.thisisanapp.ui.theme.ThisIsAnAppTheme
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userDataStr = intent.getStringExtra("userData") ?: "{}"
        val initialData = JSONObject(userDataStr)
        setContent {
            ThisIsAnAppTheme {
                Scaffold { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        ProfileScreen(initialData)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(initialData: JSONObject) {
    val context = LocalContext.current
    val customFont = FontFamily(Font(R.font.my_custom_font))
    var userData by remember { mutableStateOf(initialData) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        val pref = context.getSharedPreferences("loginCache", Context.MODE_PRIVATE)
        val username = pref.getString("username", null)
        val password = pref.getString("password", null)
        if (username != null && password != null) {
            loading = true
            val result = loginRequest(username, password)
            if (result != null && result.getBoolean("success")) {
                userData = result.getJSONObject("data")
                errorMessage = null
            } else {
                errorMessage = result?.getString("message") ?: "Login failed"
            }
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        AsyncImage(
            model = "https://api.cyber98.dev/profiles/${initialData.optString("profileImage", "default.jpg")}",
            contentDescription = "Profile Image",
            modifier = Modifier
                .fillMaxWidth()           // image fills width
                .aspectRatio(1f)          // maintains a square aspect ratio
                .clip(MaterialTheme.shapes.medium)
        )
        Text(text = "Please log in to the web portal to change your profile image.",style = MaterialTheme.typography.headlineSmall.copy(fontFamily = customFont))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = initialData.optString("username", "Unknown User"),
            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = customFont)
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                style = TextStyle(fontFamily = customFont)
            )
        } else {
            // Display all fields except 'password'
            userData.keys().forEach { key ->
                if (key != "password") {
                    val value = userData.get(key)
                    Text(text = "$key: $value", style = TextStyle(fontFamily = customFont))
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                val pref = context.getSharedPreferences("loginCache", Context.MODE_PRIVATE)
                pref.edit().clear().apply()
                (context as? ComponentActivity)?.finish()
            }) {
                Text(text = "Sign out", style = TextStyle(fontFamily = customFont))
            }
            Button(onClick = {
                refreshTrigger++
            }) {
                Text(text = "Refresh", style = TextStyle(fontFamily = customFont))
            }
        }
    }
}
