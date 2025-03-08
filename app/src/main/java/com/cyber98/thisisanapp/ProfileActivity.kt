package com.cyber98.thisisanapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import com.cyber98.thisisanapp.ui.theme.ThisIsAnAppTheme
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userDataStr = intent.getStringExtra("userData") ?: "{}"
        val userData = JSONObject(userDataStr)
        setContent {
            ThisIsAnAppTheme {
                ProfileScreen(userData)
            }
        }
    }
}

@Composable
fun ProfileScreen(userData: JSONObject) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
            val imgName = userData.optString("profileImage", "")
            val profileUrl = "https://api.cyber98.dev/profiles/$imgName"
            AsyncImage(
                model = profileUrl,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Show all fields
        userData.keys().forEach { key ->
            if(key != "password") {
                val value = userData.get(key)
                Text(text = "$key: $value")
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
