package com.llm

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import java.io.File
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class InferenceModel private constructor(context: Context, modelID: Int) {
    private var llmInference: LlmInference

    private val modelPath: String = when (modelID) {
        1 -> MODEL_PATH1
        2 -> MODEL_PATH2
        3 -> MODEL_PATH3
        else -> throw IllegalArgumentException("Invalid model ID. Must be 1, 2, or 3.")
    }

    private val modelExists: Boolean
        get() = File(modelPath).exists()

    private val _partialResults = MutableSharedFlow<Pair<String, Boolean>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val partialResults: SharedFlow<Pair<String, Boolean>> = _partialResults.asSharedFlow()

    init {
        if (!modelExists) {
            throw IllegalArgumentException("Model not found at path: $modelPath")
        }

        Log.d("Inference", "Loading Model Path: $modelPath")

        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .setMaxTokens(1024)
            .setResultListener { partialResult, done ->
                _partialResults.tryEmit(partialResult to done)
            }
            .build()

        llmInference = LlmInference.createFromOptions(context, options)
    }

    fun generateResponseAsync(prompt: String) {
        val gemmaPrompt = prompt + "<start_of_turn>model\n"
        llmInference.generateResponseAsync(gemmaPrompt)
    }

    companion object {
        private const val MODEL_PATH1 = "/data/local/tmp/gemma2.bin"
        private const val MODEL_PATH2 = "/data/local/tmp/gemma.bin"
        private const val MODEL_PATH3 = "/data/local/tmp/falcon_gpu.bin"
        private var instance: InferenceModel? = null

        fun getInstance(context: Context, modelId: Int): InferenceModel {
            return if (instance != null) {
                Log.d("Inference", "Loaded already")
                instance!!
            } else {
                InferenceModel(context, modelId).also { instance = it }
            }
        }
    }
}
