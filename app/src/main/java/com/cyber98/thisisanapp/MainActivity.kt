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
    var userData by remember { mutableStateOf<JSONObject?>(null) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) } // new refresh trigger

    LaunchedEffect(refreshTrigger) {
        val pref = context.getSharedPreferences("loginCache", Context.MODE_PRIVATE)
        val username = pref.getString("username", null)
        val password = pref.getString("password", null)
        if (username == null || password == null) {
            loading = false
            return@LaunchedEffect
        }
        // Contact the api on launch to verify credentials
        val result = loginRequest(username, password)
        if (result == null) {
            errorMessage = "you are offline"
        } else if (result.getBoolean("success")) {
            userData = result.getJSONObject("data")
            errorMessage = null
        } else {
            // Clear invalid credentials
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
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Signed in as:")
                Spacer(modifier = Modifier.height(16.dp))
                val keys = userData!!.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    if (key == "profileImage") continue  // Skip profileImage
                    Text(text = "$key: ${userData!!.get(key)}")
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.weight(1f))
                // Sign out button
                Button(
                    onClick = {
                        val pref = context.getSharedPreferences("loginCache", Context.MODE_PRIVATE)
                        pref.edit().clear().apply()
                        userData = null
                        errorMessage = "You are not logged in"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign out")
                }
            }
        }
        else -> {
            // Not logged in: show sign in and refresh buttons.
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(errorMessage ?: "You are not logged in")
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(
                        onClick = {
                            context.startActivity(android.content.Intent(context, LoginActivity::class.java))
                        }
                    ) {
                        Text("Sign in")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            loading = true
                            refreshTrigger++  // trigger refresh
                        }
                    ) {
                        Text("Refresh")
                    }
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

