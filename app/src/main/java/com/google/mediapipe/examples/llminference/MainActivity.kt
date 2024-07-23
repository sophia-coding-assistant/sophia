package com.google.mediapipe.examples.llminference

import androidx.compose.runtime.getValue
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.mediapipe.examples.llminference.ui.theme.LLMInferenceTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp


import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.ui.platform.LocalContext

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


// Your existing imports might also include:
import androidx.compose.runtime.remember
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.Icons





const val START_SCREEN = "start_screen"
const val CHAT_SCREEN = "chat_screen"
const val INITIAL_SCREEN = "initial_screen"



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LLMInferenceTheme {
                Scaffold(
                    topBar = { AppBar() }
                ) { innerPadding ->
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        val navController = rememberNavController()

                        NavHost(
                            navController = navController,
                            startDestination = INITIAL_SCREEN
                        ) {
                            composable(INITIAL_SCREEN) {
                                InitialScreen(
                                    onImageClick = {
                                        navController.navigate(START_SCREEN) {
                                            popUpTo(INITIAL_SCREEN) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }

                            composable(START_SCREEN) {
                                LoadingRoute(
                                    onModelLoaded = {
                                        navController.navigate(CHAT_SCREEN) {
                                            popUpTo(START_SCREEN) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }

                            composable(CHAT_SCREEN) {
                                ChatoRoute()
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun InitialScreen(onImageClick: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.initial_screen_title),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.sophia), // Replace with your image resource
                contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .clickable(onClick = onImageClick)
            )
        }
    }


    @Composable
    fun ChatoRoute(model: InferenceModel = InferenceModel.getInstance(context = LocalContext.current)) {
        var userInput by remember { mutableStateOf("") }
        var chatMessages by remember { mutableStateOf(listOf<ChatMessage1>()) }
        var currentPartialResult by remember { mutableStateOf("") }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Display chat messages using LazyColumn
            LazyColumn(
                modifier = Modifier.weight(2f)
            ) {
                items(chatMessages.size) { // Use the size of the list
                    val message = chatMessages[it] // Access message by index
                    when (message.type) {
                        MessageType.User -> UserMessage(message.content)
                        MessageType.PartialResult -> PartialResultMessage(message.content)
                    }
                }
            }

            // Input field
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                TextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    placeholder = { Text("Type your message...") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.weight(2f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (userInput.isNotBlank()) {
                            chatMessages = chatMessages + ChatMessage1(MessageType.User, userInput)
                            model.generateResponseAsync(userInput)
                            userInput = ""
                        }
                    }
                ) {
                    Text("Send")
                }
            }
        }

        // Observe inference results
        LaunchedEffect(model.partialResults) {
            model.partialResults.collect { (result, done) ->
                currentPartialResult += result
                if (done) {
                    chatMessages =
                        chatMessages + ChatMessage1(MessageType.PartialResult, currentPartialResult)
                    currentPartialResult = ""
                }
            }
        }
    }

    // TopAppBar is marked as experimental in Material 3
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AppBar() {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    Image(
                        painter = painterResource(id = R.drawable.ntua),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(40.dp)
                    )
                }
            )

        }
    }
}
    sealed class MessageType {
        object User : MessageType()
        object PartialResult : MessageType()
    }


@Composable
fun UserMessage(content: String) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .background(Color.LightGray, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .padding(16.dp) // Uniform padding inside the box
    ) {
        Text(
            text = content,
            textAlign = TextAlign.End,
            color = Color.Black,
        )
    }
}

@Composable
fun PartialResultMessage(content: String) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .background(Color(0xFF31642F), shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .padding(16.dp) // Uniform padding inside the box
    ) {
        Text(
            text = content,
            textAlign = TextAlign.Start,
            color = Color.White,
        )
    }
}


    data class ChatMessage1(val type: MessageType, val content: String)


