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
//import androidx.compose.foundation.clip

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



//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            LLMInferenceTheme {
//                Scaffold(
//                    topBar = { AppBar() }
//                ) { innerPadding ->
//                    // A surface container using the 'background' color from the theme
//                    Surface(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(innerPadding),
//                        color = MaterialTheme.colorScheme.background,
//                    ) {
//                        val navController = rememberNavController()
//
//                        NavHost(
//                            navController = navController,
//                            startDestination = START_SCREEN
//                        ) {
//                            composable(START_SCREEN) {
//                                LoadingRoute(
//                                    onModelLoaded = {
//                                        navController.navigate(CHAT_SCREEN) {
//                                            popUpTo(START_SCREEN) { inclusive = true }
//                                            launchSingleTop = true
//                                        }
//                                    }
//                                )
//                            }
//
//                            composable(CHAT_SCREEN) {
//                                ChatoRoute()
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

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

//            Box(
//                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
//            ) {
//                Text(
//                    text = stringResource(R.string.disclaimer),
//                    textAlign = TextAlign.Center,
//                    style = MaterialTheme.typography.bodyMedium,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp)
//                )
//            }
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


//    @Composable
//    fun UserMessage(content: String) {
//        Text(
//            text = content,
//            textAlign = TextAlign.End,
//            modifier = Modifier
//                .padding(8.dp)
//                .background(Color.LightGray) // Replace with your desired background color
//        )
//    }
//
//
//
//    @Composable
//    fun PartialResultMessage(content: String) {
//        Text(
//            text = content,
//            textAlign = TextAlign.Start,
//            color = Color(0xFF000000),
//            modifier = Modifier
//                .padding(1.dp)
//                .background(Color(0xFFFF5722)) // Replace with your desired background color
//        )
//    }

    data class ChatMessage1(val type: MessageType, val content: String)



//package com.google.mediapipe.examples.llminference
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.os.Environment
//import android.util.Log
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.core.content.ContextCompat
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import com.google.mediapipe.examples.llminference.ui.theme.LLMInferenceTheme
//import java.io.File
//import java.io.FileInputStream
//import java.io.FileOutputStream
//import java.io.IOException
//
//const val START_SCREEN = "start_screen"
//const val CHAT_SCREEN = "chat_screen"
//
//class MainActivity : ComponentActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            LLMInferenceTheme {
//                Scaffold(
//                    topBar = { AppBar() }
//                ) { innerPadding ->
//                    Surface(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(innerPadding),
//                        color = MaterialTheme.colorScheme.background,
//                    ) {
//                        val navController = rememberNavController()
//
//                        NavHost(
//                            navController = navController,
//                            startDestination = START_SCREEN
//                        ) {
//                            composable(START_SCREEN) {
//                                LoadingRoute(
//                                    onModelLoaded = {
//                                        navController.navigate(CHAT_SCREEN) {
//                                            popUpTo(START_SCREEN) { inclusive = true }
//                                            launchSingleTop = true
//                                        }
//                                    }
//                                )
//                            }
//
//                            composable(CHAT_SCREEN) {
//                                ChatRoute()
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        // Check for permissions and move the file
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            when {
//                ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.READ_EXTERNAL_STORAGE
//                ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//                ) == PackageManager.PERMISSION_GRANTED -> {
//                    moveModelFile()
//                }
//                else -> {
//                    requestPermissionsLauncher.launch(
//                        arrayOf(
//                            Manifest.permission.READ_EXTERNAL_STORAGE,
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE
//                        )
//                    )
//                }
//            }
//        } else {
//            moveModelFile()
//        }
//    }
//
//    private val requestPermissionsLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestMultiplePermissions()
//    ) { permissions ->
//        if (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true &&
//            permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) {
//            moveModelFile()
//        } else {
//            Log.e("MainActivity", "Permissions not granted!")
//        }
//    }
//
//    private fun moveModelFile() {
//        val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//        val sourceFile = File(downloadsFolder, "phi2_cpu.bin")
//        val destFile = File("/data/local/tmp/llm", "model.bin")
//
//        if (!destFile.parentFile.exists()) {
//            destFile.parentFile.mkdirs()
//        }
//
//        try {
//            FileInputStream(sourceFile).use { input ->
//                FileOutputStream(destFile).use { output ->
//                    val buffer = ByteArray(1024)
//                    var length: Int
//                    while (input.read(buffer).also { length = it } > 0) {
//                        output.write(buffer, 0, length)
//                    }
//                    Log.i("MainActivity", "Model file moved successfully!")
//                }
//            }
//        } catch (e: IOException) {
//            Log.e("MainActivity", "Error moving file", e)
//        }
//    }
//
//    @OptIn(ExperimentalMaterial3Api::class)
//    @Composable
//    fun AppBar() {
//        Column(
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            TopAppBar(
//                title = { Text(stringResource(R.string.app_name)) },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primaryContainer,
//                    titleContentColor = MaterialTheme.colorScheme.primary,
//                ),
//            )
//            Box(
//                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
//            ) {
//                Text(
//                    text = stringResource(R.string.disclaimer),
//                    textAlign = TextAlign.Center,
//                    style = MaterialTheme.typography.bodyMedium,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp)
//                )
//            }
//        }
//    }
//}
