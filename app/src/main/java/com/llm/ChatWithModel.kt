/*

var firstPrompt = true
var secondPrompt = true
var thirdPrompt = true
var firstQuery: String? = null
var secondQuery: String? = null
var thirdQuery: String? = null
var previousQueries: String = ""

class PowerConsumptionCalculator(private val context: Context) {
    private val TAG = "PowerConsumptionCalculator"

    fun estimatePowerConsumption(executionTimeMs: Long): Double {
        try {
            val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
            val powerProfileConstructor = powerProfileClass.getConstructor(Context::class.java)
            val powerProfileInstance = powerProfileConstructor.newInstance(context)

            val getAveragePowerMethod: Method =
                powerProfileClass.getMethod("getAveragePower", String::class.java)

            val cpuActivePower =
                getAveragePowerMethod.invoke(powerProfileInstance, "cpu.active") as Double
            Log.d(TAG, "CPU Active Power: $cpuActivePower mAh/sec")

            val screenPower =
                getAveragePowerMethod.invoke(powerProfileInstance, "screen.on") as Double
            Log.d(TAG, "Screen On Power: $screenPower mAh/sec")

            val wifiPower =
                getAveragePowerMethod.invoke(powerProfileInstance, "wifi.active") as Double
            Log.d(TAG, "WiFi Active Power: $wifiPower mAh/sec")

            val executionTimeSec = executionTimeMs / 1000.0
            val estimatedEnergy = cpuActivePower * executionTimeSec
            Log.d(TAG, "Estimated Energy Consumption: $estimatedEnergy mAh")

            return estimatedEnergy
        } catch (e: Exception) {
            Log.e(TAG, "Error estimating power consumption", e)
        }
        return -1.0
    }
}

class ChatViewModel(
    private val inferenceModel: InferenceModel,
    private val context: Context
) : ViewModel() {

    private val TAG = "ChatViewModel"

    private val currentUser: String
        get() {
            val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            return sharedPreferences.getString("current_username", "default") ?: "default"
        }

    private val modelID: Int
        get(){
            val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val a =  sharedPref.getString("current_model", "1") ?: "1"
            return a.toInt()
        }


    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _textInputEnabled: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isTextInputEnabled: StateFlow<Boolean> = _textInputEnabled.asStateFlow()

    // Get user-specific log file
    private fun getUserLogFile(): File {
        val username = currentUser
        val sanitizedUsername = username.replace("[^a-zA-Z0-9]".toRegex(), "_")
        return File(context.filesDir, "${sanitizedUsername}.json")
    }

    fun resetChatVariables() {
        firstPrompt = true
        secondPrompt = true
        thirdPrompt = true
        firstQuery = null
        secondQuery = null
        thirdQuery = null
        previousQueries = ""
        Log.d("ChatViewModel", "Global chat session variables reset.")
    }


    fun clearUiState() {
    viewModelScope.launch {
        _uiState.value = ChatUiState(messages = mutableListOf()) // Reset messages list
        Log.d(TAG, "UI state cleared.")
    }
}


private fun writeToJsonLog(logEntry: JSONObject) {
    viewModelScope.launch(Dispatchers.IO) {
        try {
            val userLogFile = getUserLogFile()
            val rootArray = if (userLogFile.exists()) {
                val content = userLogFile.readText()
                if (content.isNotEmpty()) JSONArray(content) else JSONArray()
            } else {
                JSONArray()
            }

            logEntry.put("topic", firstQuery)

            // Find the topic entry or create a new one
            val topicObject = (0 until rootArray.length())
                .map { rootArray.getJSONObject(it) }
                .find { it.getString("topic") == firstQuery }
                ?: JSONObject().apply {
                    put("topic", firstQuery)
                    put("conversations", JSONArray())
                    rootArray.put(this)
                }

            // Add the conversation log to the topic's "conversations" array
            val conversationsArray = topicObject.getJSONArray("conversations")
            conversationsArray.put(logEntry)

            // Write back the updated JSON structure to the file
            userLogFile.writeText(rootArray.toString(2))
            Log.d(TAG, "Successfully wrote log entry under topic: $firstQuery")
        } catch (e: Exception) {
            Log.e(TAG, "Error writing to JSON log", e)
        }
    }
}


fun sendMessage(userMessage: String) {
    val username = currentUser
    Log.d(TAG, "Processing new message for user $username: $userMessage")
    viewModelScope.launch(Dispatchers.IO) {
        _uiState.value.addMessage(userMessage, USER_PREFIX)
        var currentMessageId: String? = _uiState.value.createLoadingMessage()
        setInputEnabled(false)

        try {
            val startTime = System.nanoTime()
            val calculator = PowerConsumptionCalculator(context)
            val startTimeMs = System.currentTimeMillis()

            val fullPrompt = if (firstPrompt) {
                """The user will first tell what do they need recommendations on(such as movies). Immediately recommend them a list. USER MESSAGE START: $userMessage""".trimIndent()
            } else {
                Log.d("Inference", "$userMessage")
                "Previous queries for reference: $previousQueries.Give the user recommendation immediately, the user's current query is: $userMessage."
            }
            Log.d("Inference", "$fullPrompt")


            previousQueries += "$userMessage\n"

            if (firstPrompt) {
                firstPrompt = false
                firstQuery = userMessage // Update global firstQuery
            }

            val finalResponse = StringBuilder()
            inferenceModel.generateResponseAsync(fullPrompt)
            inferenceModel.partialResults
                .collectIndexed { index, (partialResult, done) ->
                    currentMessageId?.let {
                        if (index == 0) {
                            _uiState.value.appendMessage(it, partialResult, false)
                            finalResponse.append(partialResult)
                        } else {
                            finalResponse.append(partialResult)
                            if (done) {
                                val endTime = System.nanoTime()
                                val executionTime = (endTime - startTime) / 1_000_000
                                val endTimeMs = System.currentTimeMillis()
                                val executionTimeMs = endTimeMs - startTimeMs
                                val estimatedEnergy = calculator.estimatePowerConsumption(executionTimeMs)

                                val logEntry = JSONObject().apply {
                                    put("timestamp", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
                                    put("query", userMessage)
                                    put("response", finalResponse.toString())
                                    put("executionTime", executionTime)
                                    put("estimatedEnergy", estimatedEnergy)
                                }
                                writeToJsonLog(logEntry)

                                _uiState.value.appendMessageWithMetrics(
                                    it,
                                    "",
                                    executionTime,
                                    Thread.activeCount(),
                                    estimatedEnergy,
                                    done
                                )
                            } else {
                                _uiState.value.appendMessage(it, partialResult, done)
                            }
                        }
                        if (done) {
                            currentMessageId = null
                            setInputEnabled(true)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing message for user $username", e)
            _uiState.value.addMessage(e.localizedMessage ?: "Unknown Error", MODEL_PREFIX)
            setInputEnabled(true)

            val logEntry = JSONObject().apply {
                put("timestamp", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
                put("query", userMessage)
                put("error", e.localizedMessage ?: "Unknown Error")
            }
            writeToJsonLog(logEntry)
        }
    }
}


    private fun setInputEnabled(isEnabled: Boolean) {
        _textInputEnabled.value = isEnabled
    }

    companion object {
        fun getFactory(context: Context, modelId: Int) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val inferenceModel = InferenceModel.getInstance(context, modelId)
                return ChatViewModel(inferenceModel, context) as T
            }
        }
    }
}

*/

package com.llm

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.lang.reflect.Method

var firstPrompt = true
var secondPrompt = true
var thirdPrompt = true
var firstQuery: String? = null
var secondQuery: String? = null
var thirdQuery: String? = null
var previousQueries: String = ""

class PowerConsumptionCalculator(private val context: Context) {
    private val TAG = "PowerConsumptionCalculator"

    fun estimatePowerConsumption(executionTimeMs: Long): Double {
        try {
            val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
            val powerProfileConstructor = powerProfileClass.getConstructor(Context::class.java)
            val powerProfileInstance = powerProfileConstructor.newInstance(context)

            val getAveragePowerMethod: Method =
                powerProfileClass.getMethod("getAveragePower", String::class.java)

            val cpuActivePower =
                getAveragePowerMethod.invoke(powerProfileInstance, "cpu.active") as Double
            Log.d(TAG, "CPU Active Power: $cpuActivePower mAh/sec")

            val screenPower =
                getAveragePowerMethod.invoke(powerProfileInstance, "screen.on") as Double
            Log.d(TAG, "Screen On Power: $screenPower mAh/sec")

            val wifiPower =
                getAveragePowerMethod.invoke(powerProfileInstance, "wifi.active") as Double
            Log.d(TAG, "WiFi Active Power: $wifiPower mAh/sec")

            val executionTimeSec = executionTimeMs / 1000.0
            val estimatedEnergy = cpuActivePower * executionTimeSec
            Log.d(TAG, "Estimated Energy Consumption: $estimatedEnergy mAh")

            return estimatedEnergy
        } catch (e: Exception) {
            Log.e(TAG, "Error estimating power consumption", e)
        }
        return -1.0
    }
}

class ChatViewModel(
    private val inferenceModel: InferenceModel,
    private val context: Context
) : ViewModel() {

    private val TAG = "ChatViewModel"

    private val currentUser: String
        get() {
            val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            return sharedPreferences.getString("current_username", "default") ?: "default"
        }

    private val modelID: Int
        get(){
            val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val a =  sharedPref.getString("current_model", "1") ?: "1"
            return a.toInt()
        }

    // State management
    private val _uiState = MutableStateFlow<UiState>(ChatUiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _textInputEnabled = MutableStateFlow(true)
    val isTextInputEnabled: StateFlow<Boolean> = _textInputEnabled.asStateFlow()

    private var currentSessionId = System.currentTimeMillis()

    fun resetChatVariables() {
        viewModelScope.launch {
            firstPrompt = true
            previousQueries = ""
            currentSessionId = System.currentTimeMillis()
            clearUiState()
        }
    }

    fun clearUiState() {
        viewModelScope.launch {
            // Create a completely new ChatUiState instance
            _uiState.value = ChatUiState(messages = mutableListOf())
            // Re-enable text input
            _textInputEnabled.value = true
            Log.d(TAG, "UI state cleared completely. Session ID: $currentSessionId")
        }
    }

    private fun getUserLogFile(): File {
        val username = currentUser
        val sanitizedUsername = username.replace("[^a-zA-Z0-9]".toRegex(), "_")
        return File(context.filesDir, "${sanitizedUsername}.json")
    }

private fun writeToJsonLog(logEntry: JSONObject) {
    viewModelScope.launch(Dispatchers.IO) {
        try {
            val userLogFile = getUserLogFile()
            val rootArray = if (userLogFile.exists()) {
                val content = userLogFile.readText()
                if (content.isNotEmpty()) JSONArray(content) else JSONArray()
            } else {
                JSONArray()
            }

            logEntry.put("topic", firstQuery)

            // Find the topic entry or create a new one
            val topicObject = (0 until rootArray.length())
                .map { rootArray.getJSONObject(it) }
                .find { it.getString("topic") == firstQuery }
                ?: JSONObject().apply {
                    put("topic", firstQuery)
                    put("conversations", JSONArray())
                    rootArray.put(this)
                }

            // Add the conversation log to the topic's "conversations" array
            val conversationsArray = topicObject.getJSONArray("conversations")
            conversationsArray.put(logEntry)

            // Write back the updated JSON structure to the file
            userLogFile.writeText(rootArray.toString(2))
            Log.d(TAG, "Successfully wrote log entry under topic: $firstQuery")
        } catch (e: Exception) {
            Log.e(TAG, "Error writing to JSON log", e)
        }
    }
}

fun sendMessage(userMessage: String) {
    val username = currentUser
    Log.d(TAG, "Processing new message for user $username: $userMessage")
    viewModelScope.launch(Dispatchers.IO) {
        _uiState.value.addMessage(userMessage, USER_PREFIX)
        var currentMessageId: String? = _uiState.value.createLoadingMessage()
        setInputEnabled(false)

        try {
            val startTime = System.nanoTime()
            val calculator = PowerConsumptionCalculator(context) 
            val startTimeMs = System.currentTimeMillis()

            val fullPrompt = if (firstPrompt) {
                """The user will first tell what do they need recommendations on(such as movies). Immediately recommend them a list. USER MESSAGE START: $userMessage""".trimIndent()
            } else {
                Log.d("Inference", "$userMessage")
                "Previous queries and responses for reference: $previousQueries. Give the user recommendation immediately, the user's current query is: $userMessage."
            }
            Log.d("Inference", "$fullPrompt")


            previousQueries += "query: $userMessage\n"

            if (firstPrompt) {
                firstPrompt = false
                firstQuery = userMessage // Update global firstQuery
            }

            val finalResponse = StringBuilder()
            inferenceModel.generateResponseAsync(fullPrompt)
            inferenceModel.partialResults
                .collectIndexed { index, (partialResult, done) ->
                    currentMessageId?.let {
                        if (index == 0) {
                            _uiState.value.appendMessage(it, partialResult, false)
                            finalResponse.append(partialResult)
                        } else {
                            finalResponse.append(partialResult)
                            if (done) {
                                val endTime = System.nanoTime()
                                val executionTime = (endTime - startTime) / 1_000_000
                                val endTimeMs = System.currentTimeMillis()
                                val executionTimeMs = endTimeMs - startTimeMs
                                val estimatedEnergy = calculator.estimatePowerConsumption(executionTimeMs)

                                val logEntry = JSONObject().apply {
                                    put("timestamp", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
                                    put("query", userMessage)
                                    put("response", finalResponse.toString())
                                    put("executionTime", executionTime)
                                    put("estimatedEnergy", estimatedEnergy)
                                }
                                Log.d("Inference", finalResponse.toString())
                                previousQueries += "response: $finalResponse.toString()\n"
                                Log.d("Inference", previousQueries)
                                writeToJsonLog(logEntry)

                                _uiState.value.appendMessageWithMetrics(
                                    it,
                                    "",
                                    executionTime,
                                    Thread.activeCount(),
                                    estimatedEnergy,
                                    done
                                )
                            } else {
                                _uiState.value.appendMessage(it, partialResult, done)
                            }
                        }
                        if (done) {
                            currentMessageId = null
                            setInputEnabled(true)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing message for user $username", e)
            _uiState.value.addMessage(e.localizedMessage ?: "Unknown Error", MODEL_PREFIX)
            setInputEnabled(true)

            val logEntry = JSONObject().apply {
                put("timestamp", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
                put("query", userMessage)
                put("error", e.localizedMessage ?: "Unknown Error")
            }
            writeToJsonLog(logEntry)
        }
    }
}

//    private fun getCurrentUser(): String {
//        return context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
//            .getString("current_username", "default") ?: "default"
//    }

    private suspend fun handlePartialResult(
        messageId: String,
        partialResult: String,
        done: Boolean,
        finalResponse: StringBuilder,
        startTime: Long,
        startTimeMs: Long,
        calculator: PowerConsumptionCalculator,
        userMessage: String
    ) {
        finalResponse.append(partialResult)

        if (done) {
            val executionTime = (System.nanoTime() - startTime) / 1_000_000
            val executionTimeMs = System.currentTimeMillis() - startTimeMs
            val estimatedEnergy = calculator.estimatePowerConsumption(executionTimeMs)

            _uiState.value.appendMessageWithMetrics(
                messageId,
                "",
                executionTime,
                Thread.activeCount(),
                estimatedEnergy,
                true
            )
            setInputEnabled(true)
        } else {
            _uiState.value.appendMessage(messageId, partialResult, false)
        }
    }

    private fun handleError(e: Exception, userMessage: String) {
        Log.e(TAG, "Error processing message", e)
        _uiState.value.addMessage(e.localizedMessage ?: "Unknown Error", MODEL_PREFIX)
        setInputEnabled(true)
    }

    private fun setInputEnabled(isEnabled: Boolean) {
        _textInputEnabled.value = isEnabled
    }

companion object {
        fun getFactory(context: Context, modelId: Int) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val inferenceModel = InferenceModel.getInstance(context, modelId)
                return ChatViewModel(inferenceModel, context) as T
            }
        }
    }
}
