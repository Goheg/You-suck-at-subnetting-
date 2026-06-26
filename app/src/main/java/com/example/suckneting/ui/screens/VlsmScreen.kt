package com.example.suckneting.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.suckneting.data.model.SubnetResult
import com.example.suckneting.ui.viewmodel.VlsmViewModel

/**
 * Main screen for VLSM (Variable Length Subnet Mask) calculations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VlsmScreen(
    viewModel: VlsmViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var baseCidr by remember { mutableStateOf("192.168.1.0/24") }
    var newSegmentName by remember { mutableStateOf("") }
    var newSegmentHosts by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SubnetPro - VLSM Calculator") },
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
            // 1. Base IP/CIDR Block Input
            item {
                OutlinedTextField(
                    value = baseCidr,
                    onValueChange = { baseCidr = it },
                    label = { Text("Base Network (IP/CIDR)") },
                    placeholder = { Text("e.g. 10.0.0.0/8") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // 2. Add Segment Interface
            item {
                Text(
                    text = "Add Requirements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newSegmentName,
                                onValueChange = { newSegmentName = it },
                                label = { Text("Segment Name") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = newSegmentHosts,
                                onValueChange = { newSegmentHosts = it },
                                label = { Text("Hosts") },
                                modifier = Modifier.width(100.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            IconButton(
                                onClick = {
                                    val hosts = newSegmentHosts.toIntOrNull() ?: 0
                                    if (newSegmentName.isNotBlank() && hosts > 0) {
                                        viewModel.addSegment(newSegmentName, hosts)
                                        newSegmentName = ""
                                        newSegmentHosts = ""
                                    }
                                },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Segment")
                            }
                        }
                    }
                }
            }

            // 3. Current Segments List
            itemsIndexed(uiState.segments) { index, segment ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(segment.first, fontWeight = FontWeight.Bold)
                            Text("${segment.second} Hosts Required", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { viewModel.removeSegment(index) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // 4. Action Button
            item {
                Button(
                    onClick = { viewModel.calculateVlsm(baseCidr) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Calculate VLSM Layout", style = MaterialTheme.typography.titleMedium)
                }
            }

            // 5. Error Display
            uiState.error?.let { errorMessage ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(8.dp))
                            Text(errorMessage, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            // 6. Loading Indicator
            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            // 7. Results Section
            if (uiState.calculationResults.isNotEmpty()) {
                item {
                    Text(
                        text = "Calculated Allocation",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(uiState.calculationResults) { result ->
                    VlsmResultCard(result)
                }
            }
        }
    }
}

@Composable
fun VlsmResultCard(result: SubnetResult) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = result.segmentName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Subnet Mask: ${result.subnetMask}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "${result.totalUsableHosts} Usable",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            VlsmResultRow("Network Address", result.networkAddress)
            VlsmResultRow("Usable Range", "${result.firstUsableHost} - ${result.lastUsableHost}")
            VlsmResultRow("Broadcast", result.broadcastAddress)
        }
    }
}

@Composable
fun VlsmResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
