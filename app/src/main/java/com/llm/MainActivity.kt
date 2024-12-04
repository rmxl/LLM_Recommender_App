/*
const val START_SCREEN = "start_screen"
const val CHAT_SCREEN = "chat_screen"

class MainActivity : ComponentActivity() {
    private lateinit var currentUser: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        currentUser = sharedPreferences.getString(LoginActivity.CURRENT_USERNAME, "default") ?: "default"
        Log.d("MainActivityUserTag", "Current user inside main activity: $currentUser")

        setContent {
            LLMInferenceTheme {
                val currentContext = remember { this@MainActivity }
                Scaffold(
                    topBar = { MainAppBar(onLogout = ::logout, context = currentContext, username = currentUser, onNewSession = ::resetChatSession) }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        val navController = rememberNavController()
                        NavHost(
                            navController = navController,
                            startDestination = START_SCREEN
                        ) {
                            composable(START_SCREEN) {
                                val id = sharedPreferences.getString(CURRENT_MODEL, "0")?.toInt()
                                if (id != null) {
                                    LoadingRoute(
                                        onModelLoaded = {
                                            navController.navigate(CHAT_SCREEN) {
                                                popUpTo(START_SCREEN) { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        }, id
                                    )
                                }
                            }
                            composable(CHAT_SCREEN) {
                                val id = sharedPreferences.getString(CURRENT_MODEL, "0")?.toInt()
                                Log.d("Inference", "The ID is $id")
                                if (id != null) {
                                    ChatRoute(id)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun logout() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putBoolean("isLoggedIn", false)
            apply()
        }
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

  
private fun resetChatSession() {
    val context = this // `MainActivity` context
    val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
    val modelId = sharedPreferences.getString("current_model", "1")?.toInt() ?: 1
    val inferenceModel = InferenceModel.getInstance(context, modelId)

    // Create the ChatViewModel instance
    val chatViewModel = ChatViewModel(inferenceModel, context)

    // Reset variables and clear UI
    chatViewModel.resetChatVariables() // Reset global variables
    chatViewModel.clearUiState() // Reset chat UI
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainAppBar(onLogout: () -> Unit, context: Context, username: String, onNewSession: () -> Unit) {
    var showHistory by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        TopAppBar(
            title = { Text(stringResource(R.string.app_name)) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
            actions = {
                TextButton(onClick = { showHistory = true }) {
                    Text("History", color = MaterialTheme.colorScheme.primary)
                }
                TextButton(onClick = onNewSession) {
                    Text(stringResource(R.string.new_session), color = MaterialTheme.colorScheme.primary)
                }
                TextButton(onClick = onLogout) {
                    Text(stringResource(R.string.logout), color = MaterialTheme.colorScheme.primary)
                }
            }
        )
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer)) {
            Text(
                text = stringResource(R.string.disclaimer),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }

        if (showHistory) {
            HistoryDialog(
                isOpen = true,
                onDismiss = { showHistory = false },
                context = context,
                username = username
            )
        }
    }
}

@Composable
private fun HistoryDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    context: Context,
    username: String
) {
    if (isOpen) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f) // Increased width (90% of the screen width)
                    .heightIn(min = 500.dp, max = 600.dp) // Increased height (between 500 and 600 dp)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Chat History",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val history = remember {
                        mutableStateOf(loadHistory(context, username))
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, true)
                    ) {
                        items(history.value) { (topic, query, response) ->
                            ChatHistoryItem(topic, query, response)
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}


private fun loadHistory(context: Context, username: String): List<Triple<String, String, String>> {
    return try {
        val file = File(context.filesDir, "$username.json")
        if (file.exists()) {
            val content = file.readText()
            Log.d("MainActivityUserTag", "Loaded history content: $content")
            val rootArray = JSONArray(content)
            val conversations = mutableListOf<Triple<String, String, String>>()

            for (i in 0 until rootArray.length()) {
                val topicObject = rootArray.getJSONObject(i)
                val topic = topicObject.getString("topic")
                val conversationsArray = topicObject.getJSONArray("conversations")

                for (j in 0 until conversationsArray.length()) {
                    val conversation = conversationsArray.getJSONObject(j)
                    val query = conversation.getString("query")
                    val response = conversation.getString("response")
                    conversations.add(Triple(topic, query, response))
                }
            }
            conversations
        } else {
            Log.d("MainActivityUserTag", "No history file found for user: $username")
            emptyList()
        }
    } catch (e: Exception) {
        Log.e("MainActivityUserTag", "Error loading history for user: $username", e)
        emptyList()
    }
}

@Composable
private fun ChatHistoryItem(topic: String, query: String, response: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Topic: $topic",
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = "Query: $query",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Response: $response",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

}

*/

package com.llm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.llm.ui.theme.LLMInferenceTheme
import org.json.JSONArray
import java.io.File
import android.util.Log
import com.llm.R

const val START_SCREEN = "start_screen"
const val CHAT_SCREEN = "chat_screen"

class MainActivity : ComponentActivity() {
    private lateinit var currentUser: String
    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        currentUser = sharedPreferences.getString(LoginActivity.CURRENT_USERNAME, "default") ?: "default"

        setContent {
            LLMInferenceTheme {
                val currentContext = remember { this@MainActivity }
                val navController = rememberNavController().also { this.navController = it }
                
                Scaffold(
                    topBar = { 
                        MainAppBar(
                            onLogout = ::logout,
                            context = currentContext,
                            username = currentUser,
                            onNewSession = ::resetChatSession
                        )
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = START_SCREEN
                        ) {
                            composable(START_SCREEN) {
                                val id = sharedPreferences.getString(LoginActivity.CURRENT_MODEL, "0")?.toInt()
                                if (id != null) {
                                    LoadingRoute(
                                        onModelLoaded = {
                                            navController.navigate(CHAT_SCREEN) {
                                                popUpTo(START_SCREEN) { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        }, 
                                        id
                                    )
                                }
                            }
                            composable(CHAT_SCREEN) {
                                val id = sharedPreferences.getString(LoginActivity.CURRENT_MODEL, "0")?.toInt()
                                if (id != null) {
                                    ChatRoute(
                                        modelID = id,
                                        onNewSession = { resetChatSession() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun resetChatSession() {
        // Navigate back to start screen and then to chat screen to force a complete reset
        navController?.navigate(START_SCREEN) {
            popUpTo(CHAT_SCREEN) { inclusive = true }
        }
    }

    private fun logout() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putBoolean("isLoggedIn", false)
            apply()
        }
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainAppBar(onLogout: () -> Unit, context: Context, username: String, onNewSession: () -> Unit) {
    var showHistory by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        TopAppBar(
            title = { Text(stringResource(R.string.app_name)) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
            actions = {
                TextButton(onClick = { showHistory = true }) {
                    Text("History", color = MaterialTheme.colorScheme.primary)
                }
                TextButton(onClick = onNewSession) {
                    Text(stringResource(R.string.new_session), color = MaterialTheme.colorScheme.primary)
                }
                TextButton(onClick = onLogout) {
                    Text(stringResource(R.string.logout), color = MaterialTheme.colorScheme.primary)
                }
            }
        )
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer)) {
            Text(
                text = stringResource(R.string.disclaimer),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }

        if (showHistory) {
            HistoryDialog(
                isOpen = true,
                onDismiss = { showHistory = false },
                context = context,
                username = username
            )
        }
    }
}

@Composable
private fun HistoryDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    context: Context,
    username: String
) {
    if (isOpen) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f) // Increased width (90% of the screen width)
                    .heightIn(min = 500.dp, max = 600.dp) // Increased height (between 500 and 600 dp)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Chat History",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val history = remember {
                        mutableStateOf(loadHistory(context, username))
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, true)
                    ) {
                        items(history.value) { (topic, query, response) ->
                            ChatHistoryItem(topic, query, response)
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}


private fun loadHistory(context: Context, username: String): List<Triple<String, String, String>> {
    return try {
        val file = File(context.filesDir, "$username.json")
        if (file.exists()) {
            val content = file.readText()
            Log.d("MainActivityUserTag", "Loaded history content: $content")
            val rootArray = JSONArray(content)
            val conversations = mutableListOf<Triple<String, String, String>>()

            for (i in 0 until rootArray.length()) {
                val topicObject = rootArray.getJSONObject(i)
                val topic = topicObject.getString("topic")
                val conversationsArray = topicObject.getJSONArray("conversations")

                for (j in 0 until conversationsArray.length()) {
                    val conversation = conversationsArray.getJSONObject(j)
                    val query = conversation.getString("query")
                    val response = conversation.getString("response")
                    conversations.add(Triple(topic, query, response))
                }
            }
            conversations
        } else {
            Log.d("MainActivityUserTag", "No history file found for user: $username")
            emptyList()
        }
    } catch (e: Exception) {
        Log.e("MainActivityUserTag", "Error loading history for user: $username", e)
        emptyList()
    }
}

@Composable
private fun ChatHistoryItem(topic: String, query: String, response: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Topic: $topic",
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = "Query: $query",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Response: $response",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

}

