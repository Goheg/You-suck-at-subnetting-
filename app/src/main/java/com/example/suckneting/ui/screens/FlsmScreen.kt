package com.example.suckneting.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
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
import com.example.suckneting.ui.viewmodel.FlsmUiState
import com.example.suckneting.ui.viewmodel.FlsmViewModel

// Custom Professional Dark Palette matching the screenshot
private val DarkBackground = Color(0xFF050C16)
private val CardBackground = Color(0xFF0D1625)
private val AccentPurple = Color(0xFF5D5FEF)
private val TextSecondary = Color(0xFF94A3B8)
private val SuccessGreen = Color(0xFF4ADE80)
private val SuccessContainer = Color(0xFF1B3B36)
private val BorderColor = Color(0xFF1E293B)

@Composable
fun FlsmScreen(
    viewModel: FlsmViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var baseCidr by remember { mutableStateOf("192.168.1.0/24") }
    var targetCountText by remember { mutableStateOf("4") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Section - Dynamic based on state
            if (uiState is FlsmUiState.Success) {
                ResultsHeader(
                    title = "FLSM Results",
                    onBack = { viewModel.reset() },
                    onDownload = { /* Export logic */ }
                )
            } else {
                HeaderSection()
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState !is FlsmUiState.Success) {
                    item {
                        Column {
                            Text(
                                text = "FLSM Calculator",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Fixed Length Subnet Mask calculation for static network segmentation.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    // Network Parameters Input Card
                    item {
                        NetworkParametersCard(
                            baseCidr = baseCidr,
                            onBaseCidrChange = { baseCidr = it },
                            subnetCount = targetCountText,
                            onSubnetCountChange = { targetCountText = it },
                            onCalculate = {
                                val count = targetCountText.toIntOrNull() ?: 0
                                viewModel.calculateSubnets(baseCidr, count, false)
                            }
                        )
                    }

                    // Quick Action Chips
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(icon = Icons.Outlined.Storage, label = "Class C")
                            FilterChip(icon = Icons.Outlined.Lock, label = "Private Range")
                        }
                    }
                }

                // Dynamic Result Section based on UI State
                when (val state = uiState) {
                    is FlsmUiState.Idle -> {
                        item { EmptyState() }
                    }
                    is FlsmUiState.Success -> {
                        item {
                            SummarySection(state.newSubnetMask, state.results.size)
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Allocated Subnets",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("${state.results.size} Subnets", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                        items(state.results) { result ->
                            SubnetCard(result)
                        }
                        item {
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { viewModel.reset() },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPurple),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentPurple),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Adjust Parameters")
                            }
                        }
                    }
                    is FlsmUiState.Loading -> {
                        item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = AccentPurple)
                            }
                        }
                    }
                    is FlsmUiState.Error -> {
                        item {
                            Text(state.message, color = Color.Red, modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection() {
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
            Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = TextSecondary)
        }
    }
}

@Composable
fun ResultsHeader(title: String, onBack: () -> Unit, onDownload: () -> Unit) {
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
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        IconButton(onClick = onDownload) {
            Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White)
        }
    }
}

@Composable
fun NetworkParametersCard(
    baseCidr: String,
    onBaseCidrChange: (String) -> Unit,
    subnetCount: String,
    onSubnetCountChange: (String) -> Unit,
    onCalculate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "NETWORK PARAMETERS",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(20.dp))

            CustomTextField(
                label = "BASE NETWORK (IP/CIDR)",
                value = baseCidr,
                onValueChange = onBaseCidrChange,
                icon = Icons.Default.Dns,
                placeholder = "192.168.1.0/24"
            )

            Spacer(Modifier.height(16.dp))

            CustomTextField(
                label = "SUBNET COUNT",
                value = subnetCount,
                onValueChange = onSubnetCountChange,
                icon = Icons.Default.GridView,
                placeholder = "4",
                showRequired = true
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onCalculate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Calculate Subnets", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String,
    showRequired: Boolean = false
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            if (showRequired) {
                Surface(
                    color = AccentPurple.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "REQUIRED",
                        color = Color.White,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
            placeholder = { Text(placeholder, color = TextSecondary.copy(alpha = 0.4f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = AccentPurple,
                unfocusedBorderColor = BorderColor,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = AccentPurple
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = if (label.contains("COUNT")) KeyboardType.Number else KeyboardType.Text)
        )
    }
}

@Composable
fun FilterChip(icon: ImageVector, label: String) {
    Surface(
        color = CardBackground,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .background(CardBackground.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .border(1.dp, TextSecondary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Terminal,
                    contentDescription = null,
                    tint = TextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text("Ready to Segment", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Enter your network parameters and click calculate to generate your optimized FLSM subnet table.",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun SummarySection(mask: String, count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text("NEW SUBNET MASK", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(4.dp))
                Text(mask, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("TOTAL SUBNETS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(4.dp))
                Text(count.toString(), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SubnetCard(result: SubnetResult) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Subnet #${result.subnetId}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Surface(
                    color = SuccessContainer,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
                ) {
                    Text(
                        "${result.totalUsableHosts} Usable Hosts",
                        color = SuccessGreen,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            SubnetDetailRow("Network", result.networkAddress)
            SubnetDetailRow("First Host", result.firstUsableHost)
            SubnetDetailRow("Last Host", result.lastUsableHost)
            SubnetDetailRow("Broadcast", result.broadcastAddress)
        }
    }
}

@Composable
fun SubnetDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
