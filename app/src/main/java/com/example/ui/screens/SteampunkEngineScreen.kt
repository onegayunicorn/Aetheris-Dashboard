package com.example.ui.screens

import com.example.ui.components.WeightedGearComponent
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Orange400
import com.example.ui.theme.TextSlate100
import com.example.viewmodel.EngineViewModel
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SteampunkEngineScreen(viewModel: EngineViewModel = viewModel()) {
    val metrics by viewModel.metrics.collectAsState()
    
    // Steampunk Engine 3D View State
    var ambientIntensity by remember { mutableFloatStateOf(0.5f) }
    var pointIntensity by remember { mutableFloatStateOf(0.5f) }
    var spotIntensity by remember { mutableFloatStateOf(0.5f) }
    var explodedView by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Steampunk Engine Visualizer", style = MaterialTheme.typography.headlineMedium, color = TextSlate100)
        
        // Interactive weighted gear driven by kinetic energy
        WeightedGearComponent(angularVelocity = metrics.kineticEnergy / 10f)
        
        // Steampunk Engine 3D View
        Card(modifier = Modifier.fillMaxWidth().height(250.dp).padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            SteampunkEngine3DDisplay(
                kineticEnergy = metrics.kineticEnergy,
                ambientIntensity = ambientIntensity,
                pointIntensity = pointIntensity,
                spotIntensity = spotIntensity,
                explodedView = explodedView
            )
        }
        
        // Lighting Controls & Exploded View
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text("Lighting Controls", color = TextSlate100, style = MaterialTheme.typography.labelMedium)
            Slider(value = ambientIntensity, onValueChange = { ambientIntensity = it }, valueRange = 0f..1f)
            Slider(value = pointIntensity, onValueChange = { pointIntensity = it }, valueRange = 0f..1f)
            Slider(value = spotIntensity, onValueChange = { spotIntensity = it }, valueRange = 0f..1f)
            Button(onClick = { explodedView = !explodedView }) {
                Text(if (explodedView) "Collapse View" else "Exploded View")
            }
        }
    }
}

@Composable
fun SteampunkEngine3DDisplay(
    modifier: Modifier = Modifier,
    kineticEnergy: Float,
    ambientIntensity: Float,
    pointIntensity: Float,
    spotIntensity: Float,
    explodedView: Boolean
) {
    // Rotation based on kinetic energy
    val animatedRotation by animateFloatAsState(
        targetValue = kineticEnergy * 10f, // Simplified angular velocity
        animationSpec = tween(100, easing = LinearEasing),
        label = "rotation"
    )
    
    // Exploded view expansion
    val expansion by animateFloatAsState(
        targetValue = if (explodedView) 50f else 0f,
        animationSpec = tween(500),
        label = "expansion"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val size = size.minDimension
        val center = Offset(size / 2, size / 2)
        
        rotate(animatedRotation, pivot = center) {
            // Stylized 3D-like gear
            // Simulate lighting with color modification
            val baseColor = Cyan400
            val litColor = baseColor.copy(alpha = 0.5f + ambientIntensity * 0.5f + pointIntensity * 0.3f)
            
            // Draw gear components (with "explosion")
            drawCircle(
                color = litColor,
                radius = size / 4 + expansion,
                center = center,
                style = Stroke(width = 8f + spotIntensity * 4f)
            )
            // Mercury channel simulation (as small circles)
            drawCircle(
                color = Orange400,
                radius = 10f,
                center = center + Offset(size / 4 + expansion, 0f)
            )
        }
    }
}
