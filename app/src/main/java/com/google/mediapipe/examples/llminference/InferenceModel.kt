package com.google.mediapipe.examples.llminference

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import java.io.File
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import android.util.Log

class InferenceModel private constructor(context: Context) {
    private var llmInference: LlmInference
    private val partialResultsList = mutableListOf<Pair<String, Boolean>>()

    private val modelExists: Boolean
        get() = File(MODEL_PATH).exists()

    private val _partialResults = MutableSharedFlow<Pair<String, Boolean>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val partialResults: SharedFlow<Pair<String, Boolean>> = _partialResults.asSharedFlow()
    var tokenCounter = 0
    init {
        if (!modelExists) {
            throw IllegalArgumentException("Model not found at path: $MODEL_PATH")
        }

        val maxTokens = 250


        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(MODEL_PATH)
            .setMaxTokens(maxTokens)
            .setResultListener { partialResult, done ->
                // Split the partial result into tokens and count them
                val tokenCount = partialResult.split("\\s+".toRegex()).size
                tokenCounter += tokenCount

                // Determine if we have reached the maximum tokens
                val isDone = done || tokenCounter >= maxTokens

                // Log the partial result and done state
                Log.d("LLM", "Received partial result: $partialResult, Token Count: $tokenCounter, Done: $isDone")

                // Emit the partial result and done state
                _partialResults.tryEmit(partialResult to isDone)
                partialResultsList.add(partialResult to isDone)
            }
            .build()

        llmInference = LlmInference.createFromOptions(context, options)
    }

    fun generateResponseAsync(prompt: String) {
        tokenCounter = 0
        Log.d("LLM", "Input Prompt: $prompt")
        Log.d("LLM", "Starting inference process")
        llmInference.generateResponseAsync(prompt)
    }

    fun logAllResults() {
        // Log all accumulated partial results
        val resultLog = partialResultsList.joinToString("\n") { (result, done) ->
            "Partial Result: $result, Done: $done"
        }
        Log.d("LLM", "All Partial Results:\n$resultLog")
    }

    companion object {
        private const val MODEL_PATH = "/data/local/tmp/llm/model_gpu.bin"
        private var instance: InferenceModel? = null

        fun getInstance(context: Context): InferenceModel {
            return if (instance != null) {
                instance!!
            } else {
                InferenceModel(context).also { instance = it }
            }
        }
    }
}

