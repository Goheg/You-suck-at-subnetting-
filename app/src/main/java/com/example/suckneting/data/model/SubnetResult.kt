package com.example.suckneting.data.model

/**
 * Data model representing the result of a single subnet calculation.
 * Encapsulates all standard networking parameters for a specific subnet block.
 */
data class SubnetResult(
    val subnetId: Int,
    val networkAddress: String,
    val firstUsableHost: String,
    val lastUsableHost: String,
    val broadcastAddress: String,
    val subnetMask: String,
    val totalUsableHosts: Int,
    val segmentName: String = ""
)
