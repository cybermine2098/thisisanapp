package com.cyber98.thisisanapp

import android.content.Context
import android.content.Intent
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
        // New top row with profile image and button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = "https://api.cyber98.dev/profiles/${initialData.optString("profileImage", "default.jpg")}",
                contentDescription = "Profile Image",
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .fillMaxWidth()
                    .size(128.dp)
            )
        }
        Text(
            text = "Please log in to the web portal to change your profile image.",
            style = TextStyle(fontFamily = customFont)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        // Extract specific fields
        val username = initialData.optString("username", "Unknown User")
        val fullname = initialData.optString("fullname", "")
        val bio = initialData.optString("bio", "")
        
        // Username (already implemented)
        Text(
            text = username,
            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = customFont)
        )
        // Fullname: normal text size, centered
        if(fullname.isNotEmpty()){
            Text(
                text = fullname,
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = customFont),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Label for bio, centered
        Text(
            text = "Bio:",
            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = customFont),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        // Bio text, justified
        Text(
            text = bio,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = customFont),
            textAlign = androidx.compose.ui.text.style.TextAlign.Justify
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Display email (username + "@cyber98.dev")
        Text(
            text = "$username@cyber98.dev",
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = customFont),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                val pref = context.getSharedPreferences("loginCache", Context.MODE_PRIVATE)
                pref.edit().clear().apply()
                // Relaunch MainActivity to refresh home screen after sign out.
                context.startActivity(
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )
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
