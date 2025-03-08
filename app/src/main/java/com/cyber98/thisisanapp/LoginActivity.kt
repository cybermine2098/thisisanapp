package com.cyber98.thisisanapp

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
import android.content.Context
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.LocalConfiguration  // added import
import com.cyber98.thisisanapp.ui.theme.ThisIsAnAppTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThisIsAnAppTheme {
                Scaffold { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        LoginScreen()
                    }
                }
            }
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
    val configuration = LocalConfiguration.current                         // new
    val screenHeight = configuration.screenHeightDp.dp                      // new

    Box(modifier = Modifier.fillMaxSize()) {                               // new container
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)                               // align to top center
                .padding(top = screenHeight * 0.25f)                      // form starts at top 1/4 of the screen
                .padding(horizontal = 16.dp),                             // retain existing horizontal padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation() // Hides password characters
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                scope.launch {
                    val result = loginRequest(username, password)
                    if(result?.getBoolean("success") == true){
                        val userData = result.getJSONObject("data")
                        // Do not remove "profileImage" so it can be used in the main screen.
                        // Save username and password to cache (SharedPreferences)
                        val pref = context.getSharedPreferences("loginCache", Context.MODE_PRIVATE)
                        pref.edit().apply {
                            putString("username", username)
                            putString("password", password)
                            apply()
                        }
                        // Instead of launching a new activity, finish LoginActivity to return to MainActivity.
                        (context as? ComponentActivity)?.finish()
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
