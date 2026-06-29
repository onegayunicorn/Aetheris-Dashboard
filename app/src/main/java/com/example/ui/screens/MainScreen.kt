package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.BuildConfig
import com.example.ui.theme.*
import com.example.viewmodel.EngineViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

// --- Gemini API Setup ---

data class ChatRequest(val contents: List<Content>)
data class Content(val parts: List<Part>)
data class Part(val text: String)
data class ChatResponse(val candidates: List<Candidate>)
data class Candidate(val content: Content)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: ChatRequest
    ): ChatResponse
}

val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
val retrofit = Retrofit.Builder()
    .baseUrl("https://generativelanguage.googleapis.com/")
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()
val geminiService = retrofit.create(GeminiApiService::class.java)

@Composable
fun MainScreen(viewModel: EngineViewModel = viewModel()) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.alerts.collect { alert ->
            snackbarHostState.showSnackbar(alert)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(containerColor = SecondaryBackground) {
                NavigationBarItem(
                    selected = true,
                    onClick = { navController.navigate("dashboard") },
                    label = { Text("Engine") },
                    icon = { Text("📊") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("steampunk") },
                    label = { Text("Steampunk") },
                    icon = { Text("⚙️") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") { DashboardScreen(viewModel) }
            composable("steampunk") { SteampunkEngineScreen() }
        }
    }
}

@Composable
fun DashboardScreen(viewModel: EngineViewModel) {
    val metrics by viewModel.metrics.collectAsState()
    val scope = rememberCoroutineScope()
    var messages by remember { mutableStateOf(listOf<String>()) }
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text(text = "Sovereign System", style = MaterialTheme.typography.labelSmall, color = Cyan400, modifier = Modifier.padding(bottom = 4.dp))
        Text(text = "Aetheris Engine", style = MaterialTheme.typography.headlineMedium, color = TextSlate100)
        
        val cards = listOf(
            Pair("Heavy Gear", "${metrics.kineticEnergy} SU"),
            Pair("Fractal Cryo", metrics.chdsStatus),
            Pair("M-Flux", "${metrics.flux} ΔS"),
            Pair("T-Lock", "${metrics.stability} Hz")
        )
        
        cards.chunked(2).forEach { rowCards ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowCards.forEach { card ->
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(card.first, style = MaterialTheme.typography.labelMedium, color = TextSlate400)
                            Text(card.second, style = MaterialTheme.typography.headlineSmall, color = TextSlate100)
                        }
                    }
                }
            }
        }
        
        // Simulation Scenarios
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.triggerScenario("temp") }) { Text("Temp") }
            Button(onClick = { viewModel.triggerScenario("impact") }) { Text("Impact") }
            Button(onClick = { viewModel.triggerScenario("quantum") }) { Text("Quantum") }
        }
        
        // Chat
        Row(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                enabled = !isGenerating,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground
                )
            )
            Button(
                onClick = {
                    val prompt = inputText
                    messages = messages + "User: $prompt"
                    inputText = ""
                    isGenerating = true
                    
                    scope.launch {
                        try {
                            val response = withContext(Dispatchers.IO) {
                                geminiService.generateContent(
                                    apiKey = BuildConfig.GEMINI_API_KEY,
                                    request = ChatRequest(listOf(Content(listOf(Part(prompt)))))
                                )
                            }
                            val reply = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response"
                            messages = messages + "Gemini: $reply"
                        } catch (e: Exception) {
                            messages = messages + "Gemini: Error: ${e.message}"
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                enabled = !isGenerating && inputText.isNotBlank()
            ) {
                Text("Send")
            }
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { message ->
                Text(text = message, modifier = Modifier.padding(4.dp), color = TextSlate100)
            }
        }
    }
}


