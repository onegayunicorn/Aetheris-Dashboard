package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset

@Composable
fun WeightedGearComponent(modifier: Modifier = Modifier, angularVelocity: Float = 0f) {
    // Animate rotation based on velocity for hardware-accelerated smoothness
    var rotation by remember { mutableStateOf(0f) }
    
    // Increment rotation based on velocity
    LaunchedEffect(angularVelocity) {
        while(true) {
            rotation += angularVelocity
            kotlinx.coroutines.delay(16) // ~60fps
        }
    }

    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(durationMillis = 16, easing = LinearEasing),
        label = "gear_rotation"
    )

    Canvas(
        modifier = modifier
            .size(200.dp)
            .rotate(animatedRotation)
    ) {
        // Draw gear
        drawCircle(
            color = Color(0xFFFB923C), // Orange400
            style = Stroke(width = 20f)
        )
        // Add "weight"
        drawCircle(
            color = Color(0xFFFB923C),
            radius = 20f,
            center = Offset(size.width / 2, 20f)
        )
    }
}
