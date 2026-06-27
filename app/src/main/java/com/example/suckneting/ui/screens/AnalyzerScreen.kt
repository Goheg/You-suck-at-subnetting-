package com.example.suckneting.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.suckneting.ui.viewmodel.AnalysisResult
import com.example.suckneting.ui.viewmodel.AnalyzerUiState
import com.example.suckneting.ui.viewmodel.AnalyzerViewModel

/**
 * IP Analyzer screen that breaks down an IP address and mask into network details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzerScreen(
    viewModel: AnalyzerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var ipInput by remember { mutableStateOf("192.168.1.15") }
    var maskInput by remember { mutableStateOf("24") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SubnetPro - IP Analyzer") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = ipInput,
                            onValueChange = { ipInput = it },
                            label = { Text("IP Address") },
                            placeholder = { Text("e.g. 192.168.1.1") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = maskInput,
                            onValueChange = { maskInput = it },
                            label = { Text("Subnet Mask (CIDR or Decimal)") },
                            placeholder = { Text("e.g. 24 or 255.255.255.0") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Button(
                            onClick = { viewModel.analyzeIp(ipInput, maskInput) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Analyze Network")
                        }
                    }
                }
            }

            // Results Display
            when (val state = uiState) {
                is AnalyzerUiState.Success -> {
                    item {
                        AnalysisDetailsCard(state.result)
                    }
                    item {
                        BinaryVisualizerCard(state.result)
                    }
                }
                is AnalyzerUiState.InvalidInput -> {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.width(8.dp))
                                Text(state.message, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun AnalysisDetailsCard(result: AnalysisResult) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Network Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider()
            AnalyzerResultRow("Network Address", result.networkAddress)
            AnalyzerResultRow("Broadcast Address", result.broadcastAddress)
            AnalyzerResultRow("Usable Range", "${result.firstUsable} - ${result.lastUsable}")
            AnalyzerResultRow("Wildcard Mask", result.wildcardMask)
            AnalyzerResultRow("Subnet Mask", "${result.subnetMask} (/${result.cidr})")
        }
    }
}

@Composable
fun AnalyzerResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun BinaryVisualizerCard(result: AnalysisResult) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Binary Representation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("IP Address:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                BinaryOctetDisplay(binary = result.ipBinary, cidr = result.cidr)
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text("Subnet Mask:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                BinaryOctetDisplay(binary = result.maskBinary, cidr = result.cidr)
            }
            
            Text(
                "Note: Highlighted bits represent the network prefix (/${result.cidr}).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun BinaryOctetDisplay(binary: String, cidr: Int) {
    val octets = binary.chunked(8)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), MaterialTheme.shapes.small)
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        var globalBitIndex = 0
        octets.forEachIndexed { octetIndex, octet ->
            octet.forEach { bit ->
                globalBitIndex++
                val isNetworkPortion = globalBitIndex <= cidr
                
                Box(
                    modifier = Modifier
                        .padding(horizontal = 1.dp)
                        .background(
                            color = if (isNetworkPortion) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            shape = MaterialTheme.shapes.extraSmall
                        )
                        .padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = bit.toString(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = if (isNetworkPortion) FontWeight.Bold else FontWeight.Normal,
                        color = if (isNetworkPortion) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (octetIndex < 3) {
                Text(
                    text = ".",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 2.dp),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
