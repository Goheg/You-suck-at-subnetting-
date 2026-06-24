package com.example.suckneting.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.suckneting.data.model.SubnetResult
import com.example.suckneting.domain.math.SubnetEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UI State for the FLSM screen.
 */
sealed class FlsmUiState {
    object Idle : FlsmUiState()
    object Loading : FlsmUiState()
    data class Success(
        val results: List<SubnetResult>,
        val newSubnetMask: String
    ) : FlsmUiState()
    data class Error(val message: String) : FlsmUiState()
}

/**
 * ViewModel for FLSM (Fixed Length Subnet Mask) logic.
 * Orchestrates calls to the SubnetEngine and manages UI state.
 */
class FlsmViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<FlsmUiState>(FlsmUiState.Idle)
    val uiState: StateFlow<FlsmUiState> = _uiState.asStateFlow()

    /**
     * Executes the subnet calculation.
     * Uses Dispatchers.Default to ensure the main thread remains responsive
     * during complex bitwise calculations and list generation.
     */
    fun calculateSubnets(baseCidr: String, targetCount: Int, isHostCount: Boolean) {
        viewModelScope.launch {
            _uiState.value = FlsmUiState.Loading
            try {
                // Perform math on a background thread
                val results = withContext(Dispatchers.Default) {
                    SubnetEngine.generateSubnets(baseCidr, targetCount, isHostCount)
                }
                
                if (results.isEmpty()) {
                    _uiState.value = FlsmUiState.Error("No subnets generated.")
                } else {
                    _uiState.value = FlsmUiState.Success(
                        results = results,
                        newSubnetMask = results.first().subnetMask
                    )
                }
            } catch (e: Exception) {
                _uiState.value = FlsmUiState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }
}
