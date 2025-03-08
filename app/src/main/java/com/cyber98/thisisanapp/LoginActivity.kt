package com.cyber98.thisisanapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen()
        }
    }
}

@Composable
fun LoginScreen() {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()  // Added coroutine scope

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            scope.launch {
                val result = loginRequest(username, password)
                if(result?.getBoolean("success") == true){
                    val userData = result.getJSONObject("data")
                    // Remove profileImage
                    userData.remove("profileImage")
                    val intent = Intent(context, UserActivity::class.java)
                    intent.putExtra("userData", userData.toString())
                    context.startActivity(intent)
                } else {
                    message = result?.getString("message") ?: "Login failed"
                }
            }
        }) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message)
    }
}

suspend fun loginRequest(username: String, password: String): JSONObject? {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.cyber98.dev/verify")
            val conn = url.openConnection() as HttpsURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.doOutput = true
            val jsonParam = JSONObject()
            jsonParam.put("username", username)
            jsonParam.put("password", password)
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
