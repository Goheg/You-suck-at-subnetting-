package com.example.suckneting.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.suckneting.data.model.SubnetResult
import com.example.suckneting.ui.viewmodel.FlsmUiState
import com.example.suckneting.ui.viewmodel.FlsmViewModel

/**
 * Main screen for FLSM (Fixed Length Subnet Mask) calculations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlsmScreen(
    viewModel: FlsmViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var baseCidr by remember { mutableStateOf("192.168.1.0/24") }
    var targetCountText by remember { mutableStateOf("4") }
    var isHostCount by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SubnetPro - FLSM Calculator") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = baseCidr,
                        onValueChange = { baseCidr = it },
                        label = { Text("Base Network (IP/CIDR)") },
                        placeholder = { Text("e.g. 192.168.1.0/24") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isHostCount) "Required Hosts per Subnet" else "Required Number of Subnets",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = isHostCount,
                            onCheckedChange = { isHostCount = it }
                        )
                    }

                    OutlinedTextField(
                        value = targetCountText,
                        onValueChange = { targetCountText = it },
                        label = { Text(if (isHostCount) "Host Count" else "Subnet Count") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val count = targetCountText.toIntOrNull() ?: 0
                            viewModel.calculateSubnets(baseCidr, count, isHostCount)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Calculate Subnets")
                    }
                }
            }

            // Results Section
            when (val state = uiState) {
                is FlsmUiState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Enter network details and click calculate.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                is FlsmUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is FlsmUiState.Error -> {
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
                is FlsmUiState.Success -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SummaryCard(state.newSubnetMask, state.results.size)
                        
                        Text(
                            text = "Generated Subnets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(state.results) { result ->
                                SubnetResultCard(result)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(mask: String, count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("New Subnet Mask", style = MaterialTheme.typography.labelMedium)
                Text(mask, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Total Subnets", style = MaterialTheme.typography.labelMedium)
                Text(count.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SubnetResultCard(result: SubnetResult) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Subnet #${result.subnetId}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${result.totalUsableHosts} Usable Hosts",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            ResultRow("Network", result.networkAddress)
            ResultRow("First Host", result.firstUsableHost)
            ResultRow("Last Host", result.lastUsableHost)
            ResultRow("Broadcast", result.broadcastAddress)
        }
    }
}

@Composable
fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
