package com.cyber98.thisisanapp

import android.os.Bundle
import android.content.Context
import android.content.Intent
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Build
import android.Manifest

data class Conversation(val from: String, val to: String, val content: String, val preview: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)  // <-- Ensure super.onCreate is called
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permPref = this.getSharedPreferences("notificationPrefs", Context.MODE_PRIVATE)
            val alreadyRequested = permPref.getBoolean("notificationRequested", false)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED && !alreadyRequested) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
                permPref.edit().putBoolean("notificationRequested", true).apply()
            }
        }
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM token failed", task.exception)
                    return@addOnCompleteListener
                }

                // Get the new FCM token
                val token = task.result
                Log.d("FCM Token", token ?: "No Token")

                // Send token to your server
                sendTokenToServer(token)
            }
        // New: Fetch device info from API on app load and save it.
        lifecycleScope.launch {
            fetchDeviceInfo()
        }
    }

    // Moved inside MainActivity and updated getSharedPreferences call.
    private fun sendTokenToServer(token: String?) {
        if (token == null) return
        val sharedPref = this.getSharedPreferences("loginCache", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", null) ?: "anonymous"
        val password = sharedPref.getString("password", null) ?: "anonymous"  // added password retrieval
        val url = "https://api.cyber98.dev/fcm"  // updated endpoint
        val jsonObject = JSONObject().apply {
            put("username", username)
            put("password", password)
            put("fcmToken", token)
        }
        val request = JsonObjectRequest(Request.Method.POST, url, jsonObject,
            { response -> Log.d("FCM", "FCM info sent successfully: $response") },
            { error -> Log.e("FCM", "Error sending FCM info", error) })
        Volley.newRequestQueue(this).add(request)
    }

    private suspend fun fetchDeviceInfo() {
        return withContext(Dispatchers.IO) {
            try {
                val pref = this@MainActivity.getSharedPreferences("loginCache", Context.MODE_PRIVATE)
                val url = URL("https://api.cyber98.dev/device")
                val conn = url.openConnection() as HttpsURLConnection
                conn.requestMethod = "GET"
                val response = InputStreamReader(conn.inputStream, "UTF-8").readText()
                conn.disconnect()
                pref.edit().putString("deviceInfo", response).apply()
                Log.d("DeviceInfo", "Device info saved: $response")
            } catch (e: Exception) {
                e.printStackTrace()
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
    var conversationError by remember { mutableStateOf<String?>(null) }
    var conversations by remember { mutableStateOf(listOf<Conversation>()) }
    val scope = rememberCoroutineScope()
    
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
            val convResult = loadConversations(username, password)
            if (convResult == null || !convResult.optBoolean("success", false)) {
                conversationError = convResult?.optString("message") ?: "Failed to load conversations"
                conversations = emptyList()
            } else {
                conversationError = null
                val dataArray = convResult.optJSONArray("data")
                val convMap = mutableMapOf<String, Conversation>()
                if (dataArray != null) {
                    for (i in (dataArray.length() - 1) downTo 0) {
                        val msgObj = dataArray.getJSONObject(i)
                        val from = msgObj.optString("from", "")
                        val to = msgObj.optString("to", "")
                        val content = msgObj.optString("content", "")
                        val preview = msgObj.optString("preview", "")
                        if (from.isNotEmpty() && from !in convMap) {
                            convMap[from] = Conversation(from, to, content, preview)
                        }
                    }
                }
                conversations = convMap.values.toList()
            }
        } else {
            pref.edit().clear().apply()
            errorMessage = result.getString("message") ?: "Login failed"
        }
        loading = false
    }
    
    // Removed automatic background refresh every 5 seconds.
    
    when {
        loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        userData != null -> {
            // Logged in state with top row, conversation list, and refresh button at the bottom.
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val intent = Intent(context, ConversationActivity::class.java)
                            context.startActivity(intent)
                        }
                    ) {
                        Text("Start Conversation", style = TextStyle(fontFamily = customFont))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    AsyncImage(
                        model = "https://api.cyber98.dev/profiles/${userData!!.optString("profileImage", "")}",
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable {
                                val intent = Intent(context, ProfileActivity::class.java)
                                intent.putExtra("userData", userData.toString())
                                context.startActivity(intent)
                            }
                    )
                }
                // Replace SwipeRefresh with a plain Column for conversation list.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    if (conversationError != null) {
                        Text(
                            text = conversationError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = TextStyle(fontFamily = customFont),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else if (conversations.isEmpty()) {
                        Text(
                            "no conversations",
                            style = TextStyle(fontFamily = customFont),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        LazyColumn {
                            items(conversations) { convo ->
                                Button(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    onClick = {
                                        // Create an intent to launch the MessagesActivity filtering by 'from'
                                        val intent = Intent(context, MessagesActivity::class.java)
                                        intent.putExtra("from", convo.from)
                                        context.startActivity(intent)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        AsyncImage(
                                            model = "https://api.cyber98.dev/profiles/${convo.preview}",
                                            contentDescription = "Profile Image",
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = convo.from,
                                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = customFont)
                                            )
                                            Text(
                                                text = convo.content,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = customFont)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { refreshTrigger++ }
                    ) {
                        Text("Refresh", style = TextStyle(fontFamily = customFont))
                    }
                }
            }
        }
        else -> {
            // Not logged in state.
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
                            Intent(context, LoginActivity::class.java)
                        )
                    }
                ) {
                    Text("Log in", style = TextStyle(fontFamily = customFont))
                }
            }
        }
    }
}

suspend fun loadConversations(username: String, password: String): JSONObject? {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.cyber98.dev/getmessages")
            val conn = url.openConnection() as HttpsURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.doOutput = true
            val jsonParam = JSONObject().apply {
                put("username", username)
                put("password", password)
            }
            val writer = OutputStreamWriter(conn.outputStream, "UTF-8")
            writer.write(jsonParam.toString())
            writer.flush()
            writer.close()
            val response = InputStreamReader(conn.inputStream, "UTF-8").readText()
            conn.disconnect()
            JSONObject(response)
        } catch (e: Exception) {
            e.printStackTrace()
            null
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