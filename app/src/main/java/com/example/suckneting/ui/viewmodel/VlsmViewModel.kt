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
    val error: String? = null,
    val totalRequiredHosts: Int = 0,
    val totalAllocatedHosts: Int = 0
)

/**
 * ViewModel for Variable Length Subnet Mask (VLSM) logic.
 */
class VlsmViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(VlsmUiState())
    val uiState: StateFlow<VlsmUiState> = _uiState.asStateFlow()

    fun addSegment(name: String = "", hostCount: Int = 0) {
        _uiState.update { currentState ->
            currentState.copy(
                segments = currentState.segments + (name to hostCount),
                error = null
            )
        }
    }

    fun updateSegment(index: Int, name: String, hostCount: Int) {
        _uiState.update { currentState ->
            val updatedList = currentState.segments.toMutableList()
            if (index in updatedList.indices) {
                updatedList[index] = name to hostCount
            }
            currentState.copy(segments = updatedList)
        }
    }

    fun removeSegment(index: Int) {
        _uiState.update { currentState ->
            val updatedList = currentState.segments.toMutableList().apply {
                if (index in indices) removeAt(index)
            }
            currentState.copy(segments = updatedList)
        }
    }

    fun clearResults() {
        _uiState.update { it.copy(calculationResults = emptyList(), error = null) }
    }

    fun calculateVlsm(baseCidr: String) {
        val segments = _uiState.value.segments.filter { it.first.isNotBlank() && it.second > 0 }
        if (segments.isEmpty()) {
            _uiState.update { it.copy(error = "Please add at least one valid segment.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val results = withContext(Dispatchers.Default) {
                    SubnetEngine.calculateVlsm(baseCidr, segments)
                }
                
                val totalRequired = segments.sumOf { it.second }
                val totalAllocated = results.sumOf { it.totalUsableHosts }

                _uiState.update { it.copy(
                    calculationResults = results, 
                    isLoading = false,
                    totalRequiredHosts = totalRequired,
                    totalAllocatedHosts = totalAllocated
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Calculation failed.") }
            }
        }
    }
}
