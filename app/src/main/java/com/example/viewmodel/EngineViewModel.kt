package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.simulation.SimulationEngine
import kotlinx.coroutines.flow.asSharedFlow

class EngineViewModel : ViewModel() {
    private val engine = SimulationEngine()
    val metrics = engine.metrics
    val alerts = engine.alerts

    fun triggerScenario(scenario: String) {
        when(scenario) {
            "temp" -> engine.runExtremeTemperatureFluctuation()
            "impact" -> engine.runHighImpactStressTest()
            "quantum" -> engine.runQuantumEntanglementDegradation()
        }
    }
}
