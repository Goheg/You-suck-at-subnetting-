package com.example.suckneting.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.suckneting.ui.viewmodel.AnalysisResult
import com.example.suckneting.ui.viewmodel.AnalyzerUiState
import com.example.suckneting.ui.viewmodel.AnalyzerViewModel

// Custom Professional Dark Palette matching the "SubnetPro" theme
private val DarkBackground = Color(0xFF050C16)
private val CardBackground = Color(0xFF0D1625)
private val AccentPurple = Color(0xFF5D5FEF)
private val TextSecondary = Color(0xFF94A3B8)
private val BorderColor = Color(0xFF1E293B)
private val TipTeal = Color(0xFF2DD4BF)

@Composable
fun AnalyzerScreen(
    viewModel: AnalyzerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var ipInput by remember { mutableStateOf("192.168.1.13") }
    var maskInput by remember { mutableStateOf("24") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header matching the UI Design
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SubnetPro", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = TextSecondary)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Network Input Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AnalyzerInputField(
                            label = "IP ADDRESS",
                            value = ipInput,
                            onValueChange = { ipInput = it },
                            placeholder = "192.168.1.13"
                        )
                        AnalyzerInputField(
                            label = "SUBNET MASK (CIDR / DEC)",
                            value = maskInput,
                            onValueChange = { maskInput = it },
                            placeholder = "24"
                        )
                        Button(
                            onClick = { viewModel.analyzeIp(ipInput, maskInput) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Analyze Network", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Calculation Results Dynamic Content
                when (val state = uiState) {
                    is AnalyzerUiState.Success -> {
                        item { NetworkDetailsCard(state.result) }
                        item { TotalHostsCard(state.result) }
                        item { BinaryRepresentationCard(state.result) }
                    }
                    is AnalyzerUiState.InvalidInput -> {
                        item {
                            Text(state.message, color = Color.Red, modifier = Modifier.padding(8.dp))
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun AnalyzerInputField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Column {
        Text(label, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = TextSecondary.copy(alpha = 0.4f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentPurple,
                unfocusedBorderColor = BorderColor,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
    }
}

@Composable
fun NetworkDetailsCard(result: AnalysisResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storage, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Network Details", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(24.dp))
            AnalysisRow("Network Address", result.networkAddress)
            AnalysisRow("Broadcast Address", result.broadcastAddress)
            
            // Usable Range split into two lines for precision mapping to screenshot
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("Usable", color = TextSecondary, fontSize = 15.sp)
                    Text("Range", color = TextSecondary, fontSize = 15.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${result.firstUsable} -", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text(result.lastUsable, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }
            
            AnalysisRow("Subnet Mask", "${result.subnetMask} (/${result.cidr})")
        }
    }
}

@Composable
fun AnalysisRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 15.sp)
        Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun TotalHostsCard(result: AnalysisResult) {
    val hostBits = 32 - result.cidr
    val totalHosts = if (result.cidr >= 31) 0 else (1 shl hostBits) - 2
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("TOTAL HOSTS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.height(4.dp))
            Text(totalHosts.toString(), color = TipTeal, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("2^$hostBits - 2 Available", color = TextSecondary, fontSize = 12.sp)
            }
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { 0.7f }, // Illustrative static progress matching screenshot
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = TipTeal,
                trackColor = BorderColor
            )
        }
    }
}

@Composable
fun BinaryRepresentationCard(result: AnalysisResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ViewWeek, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Binary Representation", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Surface(
                    color = BorderColor.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "BIT LEVEL VIEW",
                        color = TextSecondary.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("IP Address: ${result.ipAddress}", color = TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            BinaryOctetGrid(result.ipBinary, highlightColor = AccentPurple)

            Spacer(Modifier.height(24.dp))
            Text("Subnet Mask: ${result.subnetMask}", color = TipTeal, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            BinaryOctetGrid(result.maskBinary, highlightColor = TipTeal)

            Spacer(Modifier.height(24.dp))
            Text(
                "Note: Highlighted bits represent the network prefix (/${result.cidr}).",
                color = TextSecondary.copy(alpha = 0.4f),
                fontSize = 11.sp,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
fun BinaryOctetGrid(binary: String, highlightColor: Color) {
    val octets = binary.chunked(8)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        octets.forEach { octet ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                octet.forEach { bit ->
                    BitCell(bit.toString(), isHighlighted = bit == '1', color = highlightColor)
                }
            }
        }
    }
}

@Composable
fun BitCell(bit: String, isHighlighted: Boolean, color: Color) {
    Box(
        modifier = Modifier
            .size(width = 30.dp, height = 36.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isHighlighted) color else BorderColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = bit,
            color = if (isHighlighted) Color.White else TextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
