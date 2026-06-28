package com.example.suckneting.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.suckneting.data.model.SubnetResult
import com.example.suckneting.ui.viewmodel.VlsmViewModel

// Professional Dark Palette matching the VLSM screenshots
private val DarkBackground = Color(0xFF050C16)
private val CardBackground = Color(0xFF0D1625)
private val AccentPurple = Color(0xFF5D5FEF)
private val TextSecondary = Color(0xFF94A3B8)
private val BorderColor = Color(0xFF1E293B)
private val TipTeal = Color(0xFF2DD4BF)
private val DeleteRed = Color(0xFF991B1B)

@Composable
fun VlsmScreen(
    viewModel: VlsmViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var baseCidr by remember { mutableStateOf("192.168.1.0/24") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header - Changes based on result state
            if (uiState.calculationResults.isEmpty()) {
                DefaultHeader()
            } else {
                ResultsHeader(
                    onBack = { viewModel.clearResults() },
                    onDownload = { /* Implement download logic */ }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.calculationResults.isEmpty()) {
                    // --- INPUT MODE ---
                    item {
                        Column {
                            Text("VLSM Calculator", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("Variable Length Subnet Masking for efficient IP allocation.", color = TextSecondary, fontSize = 14.sp)
                        }
                    }

                    item {
                        VlsmInputCard(
                            baseCidr = baseCidr,
                            onBaseCidrChange = { baseCidr = it },
                            segments = uiState.segments,
                            onAddSegment = { viewModel.addSegment() },
                            onRemoveSegment = { viewModel.removeSegment(it) },
                            onUpdateSegment = { index, name, hosts -> viewModel.updateSegment(index, name, hosts) }
                        )
                    }

                    item {
                        Button(
                            onClick = { viewModel.calculateVlsm(baseCidr) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Calculate VLSM Layout", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    item {
                        NetworkTipSection()
                    }
                } else {
                    // --- RESULTS MODE ---
                    item {
                        VlsmSummaryCard(
                            baseCidr = baseCidr,
                            required = uiState.totalRequiredHosts,
                            allocated = uiState.totalAllocatedHosts
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Allocated Segments", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("${uiState.calculationResults.size} Segments", color = TextSecondary, fontSize = 12.sp)
                        }
                    }

                    itemsIndexed(uiState.calculationResults) { _, result ->
                        VlsmResultCard(result)
                    }
                    
                    item {
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { viewModel.clearResults() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPurple),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentPurple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Adjust Requirements")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DefaultHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Hub, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text("SubnetPro", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        IconButton(onClick = {}) {
            Icon(Icons.Outlined.AccountCircle, contentDescription = "Profile", tint = TextSecondary)
        }
    }
}

@Composable
fun ResultsHeader(onBack: () -> Unit, onDownload: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("VLSM Results", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        IconButton(onClick = onDownload) {
            Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White)
        }
    }
}

@Composable
fun VlsmInputCard(
    baseCidr: String,
    onBaseCidrChange: (String) -> Unit,
    segments: List<Pair<String, Int>>,
    onAddSegment: () -> Unit,
    onRemoveSegment: (Int) -> Unit,
    onUpdateSegment: (Int, String, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("BASE NETWORK (IP/CIDR)", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = baseCidr,
                onValueChange = onBaseCidrChange,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Dns, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPurple,
                    unfocusedBorderColor = BorderColor,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ADD REQUIREMENTS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("MAX 10 SEGMENTS", color = TextSecondary.copy(alpha = 0.6f), fontSize = 10.sp)
            }
            Spacer(Modifier.height(12.dp))

            segments.forEachIndexed { index, segment ->
                SegmentRow(
                    name = segment.first,
                    hosts = if (segment.second == 0) "" else segment.second.toString(),
                    onNameChange = { onUpdateSegment(index, it, segment.second) },
                    onHostsChange = { onUpdateSegment(index, segment.first, it.toIntOrNull() ?: 0) },
                    onDelete = { onRemoveSegment(index) }
                )
                Spacer(Modifier.height(8.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Transparent)
                    .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable { onAddSegment() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add New Segment", color = TextSecondary, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun SegmentRow(
    name: String,
    hosts: String,
    onNameChange: (String) -> Unit,
    onHostsChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Segment Name", fontSize = 14.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentPurple,
                unfocusedBorderColor = BorderColor,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
        OutlinedTextField(
            value = hosts,
            onValueChange = onHostsChange,
            modifier = Modifier.width(80.dp),
            placeholder = { Text("Hosts", fontSize = 14.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentPurple,
                unfocusedBorderColor = BorderColor,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(DeleteRed.copy(alpha = 0.2f))
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun NetworkTipSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TipTeal.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TipTeal.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = TipTeal)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("NETWORK TIP", color = TipTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "VLSM allows you to use different masks for each subnet, maximizing address space efficiency. List your segments from largest to smallest for optimal results.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun VlsmSummaryCard(baseCidr: String, required: Int, allocated: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("BASE NETWORK", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(baseCidr, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Surface(color = AccentPurple.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp)) {
                    Text("Optimal", color = AccentPurple, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total Required", color = TextSecondary, fontSize = 10.sp)
                    Text("$required Hosts", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total Allocated", color = TextSecondary, fontSize = 10.sp)
                    Text("$allocated Hosts", color = TipTeal, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun VlsmResultCard(result: SubnetResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(result.segmentName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Required: ${result.requiredHosts} Hosts", color = TextSecondary, fontSize = 13.sp)
                }
                Surface(color = BorderColor, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        "/"+result.subnetMask.split("/").last(), 
                        color = TextSecondary, 
                        fontSize = 12.sp, 
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            VlsmDetailRow("Network Address", result.networkAddress, valueColor = TipTeal)
            VlsmDetailRow("Subnet Mask", result.subnetMask.split("/").first())
            VlsmDetailRow("Usable Range", "${result.firstUsableHost} - .${result.lastUsableHost.split(".").last()}")
            VlsmDetailRow("Broadcast", result.broadcastAddress)
        }
    }
}

@Composable
fun VlsmDetailRow(label: String, value: String, valueColor: Color = Color.White) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
