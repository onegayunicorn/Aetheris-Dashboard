package com.example.simulation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class EngineMetrics(
    val kineticEnergy: Float,
    val entropy: Float,
    val stability: Float,
    val flux: Float,
    val chdsStatus: String
)

class SimulationEngine {
    private val _metrics = MutableStateFlow(EngineMetrics(85.38f, 0.39f, 0.92f, 0.80f, "Stable"))
    val metrics = _metrics.asStateFlow()

    private val _alerts = MutableSharedFlow<String>()
    val alerts = _alerts.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.Default)

    fun runExtremeTemperatureFluctuation() {
        _metrics.value = _metrics.value.copy(chdsStatus = "Fluctuating", entropy = _metrics.value.entropy + 0.1f)
    }
    
    fun runHighImpactStressTest() {
        val newStability = _metrics.value.stability - 0.2f
        _metrics.value = _metrics.value.copy(stability = newStability, kineticEnergy = _metrics.value.kineticEnergy + 10f)
        if (newStability < 0.5f) {
            scope.launch { _alerts.emit("Alert: Stability critically low!") }
        }
    }

    fun runQuantumEntanglementDegradation() {
        val newFlux = _metrics.value.flux - 0.3f
        _metrics.value = _metrics.value.copy(flux = newFlux, stability = _metrics.value.stability - 0.1f)
        if (newFlux < 0.2f) {
            scope.launch { _alerts.emit("Alert: Quantum decoherence detected!") }
        }
    }
}
