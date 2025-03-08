package com.cyber98.thisisanapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

class UserActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userDataStr = intent.getStringExtra("userData") ?: "{}"
        val userData = JSONObject(userDataStr)
        setContent {
            UserScreen(userData)
        }
    }
}

@Composable
fun UserScreen(userData: JSONObject) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Spacer(modifier = Modifier.weight(1.0f))
        val keys = userData.keys()
        val output = ""
        while(keys.hasNext()){
            val key = keys.next()
            // In case profileImage is still present, skip it.
            if(key == "profileImage") continue
            Text(text = "$key: ${userData.get(key)}")
            Spacer(modifier = Modifier.height(8.dp))
        }
        Spacer(modifier = Modifier.weight(1.0f))
    }
}
