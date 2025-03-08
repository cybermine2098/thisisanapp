package com.cyber98.thisisanapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import com.cyber98.thisisanapp.ui.theme.ThisIsAnAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalSoftwareKeyboardController // added import
import androidx.compose.foundation.lazy.rememberLazyListState

class MessagesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the sender address from the intent extras:
        val sender = intent.getStringExtra("from") ?: ""
        setContent {
            ThisIsAnAppTheme {
                Surface {
                    MessagesScreen(sender)
                }
            }
        }
    }
}

// Updated data class using 'to' and 'form' (sender)
data class Message(val content: String, val to: String, val from: String)

// Modify MessagesScreen to work with Message objects instead of plain Strings.
@Composable
fun MessagesScreen(sender: String) {
    val ctx = LocalContext.current
    val pref = ctx.getSharedPreferences("loginCache", Context.MODE_PRIVATE)
    val loggedInUser = pref.getString("username", "") ?: ""
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var messages by remember { mutableStateOf(listOf<Message>()) }
    var newMessage by remember { mutableStateOf("") }  // new state for message input
    val scope = rememberCoroutineScope()  // add coroutine scope here
    val keyboardController = LocalSoftwareKeyboardController.current // new line inserted
    val listState = rememberLazyListState() // new state for scrolling
    
    // Initial load and refresh triggered by sender change
    LaunchedEffect(sender) {
        val result = loadMessagesFromSender(ctx, sender)
        if(result == null) {
            errorMessage = "Unable to load messages."
        } else {
            if(result.optBoolean("success", false)) {
                val dataArray = result.optJSONArray("data")
                val msgs = mutableListOf<Message>()
                if(dataArray != null) {
                    for(i in 0 until dataArray.length()) {
                        val msgObj = dataArray.getJSONObject(i)
                        val content = msgObj.optString("content", "")
                        val to = msgObj.optString("to", "")
                        val from = msgObj.optString("from", "")
                        msgs.add(Message(content, to, from))
                    }
                }
                messages = msgs
                errorMessage = null
            } else {
                errorMessage = result.optString("message", "Failed to load messages.")
            }
        }
        loading = false
    }
    // Auto refresh every 5 seconds
    LaunchedEffect(Unit) {
        while(true) {
            delay(5000L)
            val result = loadMessagesFromSender(ctx, sender)
            if(result?.optBoolean("success", false)==true) {
                val dataArray = result.optJSONArray("data")
                val msgs = mutableListOf<Message>()
                if(dataArray != null) {
                    for(i in 0 until dataArray.length()) {
                        val msgObj = dataArray.getJSONObject(i)
                        val content = msgObj.optString("content", "")
                        val to = msgObj.optString("to", "")
                        val from = msgObj.optString("from", "")
                        msgs.add(Message(content, to, from))
                    }
                }
                messages = msgs
            }
        }
    }
    
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        // Message list column takes available space
        Column(modifier = Modifier.weight(1f)) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Messages from $sender", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            when {
                loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
                messages.isEmpty() -> {
                    Text("No messages from this sender.")
                }
                else -> {
                    LazyColumn(state = listState) { // pass state to LazyColumn
                        items(messages) { msg ->
                            val isSelf = msg.from == (loggedInUser + "@cyber98.dev")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = if (isSelf) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = msg.content,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    LaunchedEffect(messages) { // scroll to bottom when messages update
                        if (messages.isNotEmpty()) {
                            listState.animateScrollToItem(messages.size - 1)
                        }
                    }
                }
            }
        }
        // Bottom input area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message") },
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    keyboardController?.hide() // close the keyboard
                    scope.launch {
                        val sentUsername = pref.getString("username", "") ?: ""
                        val sentPassword = pref.getString("password", "") ?: ""
                        if(newMessage.isNotBlank()){
                            val result = sendMessage(sender, newMessage, sentUsername, sentPassword)
                            if(result != null && result.optBoolean("success", false)) {
                                newMessage = ""
                                // Refresh messages after sending.
                                val refreshResult = loadMessagesFromSender(ctx, sender)
                                if(refreshResult?.optBoolean("success", false)==true) {
                                    val dataArray = refreshResult.optJSONArray("data")
                                    val msgs = mutableListOf<Message>()
                                    if(dataArray != null) {
                                        for(i in 0 until dataArray.length()) {
                                            val msgObj = dataArray.getJSONObject(i)
                                            val content = msgObj.optString("content", "")
                                            val to = msgObj.optString("to", "")
                                            val from = msgObj.optString("from", "")
                                            msgs.add(Message(content, to, from))
                                        }
                                    }
                                    messages = msgs
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.height(56.dp)  // fairly small button
            ) {
                Text("Send")
            }
        }
    }
}

// New function for sending message; similar to postMessage in ConversationActivity.
suspend fun sendMessage(to: String, content: String, username: String, password: String): JSONObject? {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.cyber98.dev/post")
            val conn = url.openConnection() as HttpsURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.doOutput = true
            val jsonParam = JSONObject().apply {
                put("to", to)
                put("content", content)
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

// Changed function to post username, password, and address to /getmessagesfrom.
suspend fun loadMessagesFromSender(context: Context, address: String): JSONObject? {
    return withContext(Dispatchers.IO) {
        try {
            val pref = context.getSharedPreferences("loginCache", Context.MODE_PRIVATE)
            val username = pref.getString("username", "") ?: ""
            val password = pref.getString("password", "") ?: ""
            val url = URL("https://api.cyber98.dev/getmessagesfrom")
            val conn = url.openConnection() as HttpsURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.doOutput = true
            val jsonParam = JSONObject().apply {
                put("username", username)
                put("password", password)
                put("address", address)
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
