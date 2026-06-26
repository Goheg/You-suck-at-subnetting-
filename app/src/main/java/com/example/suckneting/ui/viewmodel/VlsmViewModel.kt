package com.example.suckneting.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.suckneting.data.model.SubnetResult
import com.example.suckneting.domain.math.SubnetEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UI State for the VLSM screen.
 */
data class VlsmUiState(
    val segments: List<Pair<String, Int>> = emptyList(),
    val calculationResults: List<SubnetResult> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for Variable Length Subnet Mask (VLSM) logic.
 * Manages the dynamic list of segment requirements and triggers the engine's calculation.
 */
class VlsmViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(VlsmUiState())
    val uiState: StateFlow<VlsmUiState> = _uiState.asStateFlow()

    /**
     * Adds a new network segment requirement to the state.
     */
    fun addSegment(name: String, hostCount: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                segments = currentState.segments + (name to hostCount),
                error = null
            )
        }
    }

    /**
     * Removes a segment requirement at a specific index.
     */
    fun removeSegment(index: Int) {
        _uiState.update { currentState ->
            val updatedList = currentState.segments.toMutableList().apply {
                removeAt(index)
            }
            currentState.copy(segments = updatedList)
        }
    }

    /**
     * Executes the VLSM calculation based on the current segments and a base CIDR block.
     */
    fun calculateVlsm(baseCidr: String) {
        val segments = _uiState.value.segments
        if (segments.isEmpty()) {
            _uiState.update { it.copy(error = "Please add at least one segment.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val results = withContext(Dispatchers.Default) {
                    SubnetEngine.calculateVlsm(baseCidr, segments)
                }
                _uiState.update { it.copy(calculationResults = results, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Calculation failed.") }
            }
        }
    }
}
