package com.example.suckneting.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.suckneting.domain.math.SubnetEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AnalysisResult(
    val ipAddress: String,
    val networkAddress: String,
    val broadcastAddress: String,
    val firstUsable: String,
    val lastUsable: String,
    val wildcardMask: String,
    val subnetMask: String,
    val cidr: Int,
    val ipBinary: String,
    val maskBinary: String
)

sealed class AnalyzerUiState {
    object Idle : AnalyzerUiState()
    data class Success(val result: AnalysisResult) : AnalyzerUiState()
    data class InvalidInput(val message: String) : AnalyzerUiState()
}

class AnalyzerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AnalyzerUiState>(AnalyzerUiState.Idle)
    val uiState: StateFlow<AnalyzerUiState> = _uiState.asStateFlow()

    fun analyzeIp(ipAddress: String, maskInput: String) {
        try {
            val ipInt = SubnetEngine.stringToIp(ipAddress)
            
            // Handle both CIDR (e.g. "24") and Dotted Decimal (e.g. "255.255.255.0")
            val (maskInt, prefix) = if (maskInput.contains(".")) {
                val mInt = SubnetEngine.stringToIp(maskInput)
                val p = countLeadingOnes(mInt)
                mInt to p
            } else {
                val p = maskInput.toIntOrNull() ?: throw IllegalArgumentException("Invalid mask format")
                require(p in 0..32) { "CIDR must be 0-32" }
                val mInt = if (p == 0) 0 else (-1 shl (32 - p))
                mInt to p
            }

            val networkInt = ipInt and maskInt
            val wildcardInt = maskInt.inv()
            val broadcastInt = networkInt or wildcardInt

            val firstUsable = if (prefix >= 31) "N/A" else SubnetEngine.ipToString(networkInt + 1)
            val lastUsable = if (prefix >= 31) "N/A" else SubnetEngine.ipToString(broadcastInt - 1)

            val result = AnalysisResult(
                ipAddress = ipAddress,
                networkAddress = SubnetEngine.ipToString(networkInt),
                broadcastAddress = SubnetEngine.ipToString(broadcastInt),
                firstUsable = firstUsable,
                lastUsable = lastUsable,
                wildcardMask = SubnetEngine.ipToString(wildcardInt),
                subnetMask = SubnetEngine.ipToString(maskInt),
                cidr = prefix,
                ipBinary = toFullBinaryString(ipInt),
                maskBinary = toFullBinaryString(maskInt)
            )
            
            _uiState.value = AnalyzerUiState.Success(result)
        } catch (e: Exception) {
            _uiState.value = AnalyzerUiState.InvalidInput(e.message ?: "Invalid IP or Mask")
        }
    }

    private fun countLeadingOnes(mask: Int): Int {
        var count = 0
        var m = mask
        for (i in 0 until 32) {
            if ((m and (1 shl (31 - i))) != 0) {
                count++
            } else {
                break
            }
        }
        return count
    }

    private fun toFullBinaryString(value: Int): String {
        return String.format("%32s", Integer.toBinaryString(value)).replace(' ', '0')
    }
}
