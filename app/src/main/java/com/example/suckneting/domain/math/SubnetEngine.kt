package com.example.suckneting.domain.math

import com.example.suckneting.data.model.SubnetResult

/**
 * Pure Kotlin object handling bitwise network math operations.
 * Operates entirely on standard 32-bit signed integers using bitwise operators
 * (and, or, inv, shl, ushr) to ensure maximum compatibility and zero external dependencies.
 */
object SubnetEngine {

    /**
     * Converts a dotted-decimal IP string (e.g., "192.168.1.1") into a 32-bit Int representation.
     */
    fun stringToIp(ipStr: String): Int {
        val parts = ipStr.split(".")
        require(parts.size == 4) { "IP address must contain exactly 4 octets separated by dots." }
        var result = 0
        for (i in 0 until 4) {
            val part = parts[i].trim().toInt()
            require(part in 0..255) { "Each octet must be an integer between 0 and 255." }
            result = (result shl 8) or part
        }
        return result
    }

    /**
     * Converts a 32-bit Int back into its standard dotted-decimal String representation.
     */
    fun ipToString(ip: Int): String {
        val octet1 = (ip ushr 24) and 0xFF
        val octet2 = (ip ushr 16) and 0xFF
        val octet3 = (ip ushr 8) and 0xFF
        val octet4 = ip and 0xFF
        return "$octet1.$octet2.$octet3.$octet4"
    }

    /**
     * Parses a CIDR block string (e.g., "192.168.1.0/24") into a Pair containing
     * the base IP address as an Int and the prefix length as an Int.
     */
    fun parseCidr(cidrStr: String): Pair<Int, Int> {
        val parts = cidrStr.split("/")
        require(parts.size == 2) { "CIDR format must be IP/Prefix (e.g., 192.168.1.0/24)." }
        val ip = stringToIp(parts[0].trim())
        val prefix = parts[1].trim().toInt()
        require(prefix in 0..32) { "Prefix length must be between 0 and 32." }
        return Pair(ip, prefix)
    }

    /**
     * Calculates the VLSM allocation for a given base CIDR and a list of host requirements.
     */
    fun calculateVlsm(baseCidr: String, requirements: List<Pair<String, Int>>): List<SubnetResult> {
        val (baseIp, basePrefix) = parseCidr(baseCidr)
        
        // Ensure we start with the base network address (zero out host bits)
        val mask = if (basePrefix == 0) 0 else (-1 shl (32 - basePrefix))
        var currentIp = baseIp and mask
        
        val maxIp = if (basePrefix == 0) -1 else (currentIp or mask.inv())
        
        // Sort requirements descending by host count to maximize address space efficiency
        val sortedReqs = requirements.sortedByDescending { it.second }
        val results = mutableListOf<SubnetResult>()

        for ((index, req) in sortedReqs.withIndex()) {
            val segmentName = req.first
            val hostsNeeded = req.second
            
            // Required total block size = hosts + network + broadcast
            val totalNeeded = hostsNeeded + 2
            
            // Find minimum power of 2 that accommodates the requirement
            var h = 0
            while ((1 shl h) < totalNeeded && h < 32) {
                h++
            }
            
            val prefix = 32 - h
            val subnetMask = if (prefix == 0) 0 else (-1 shl (32 - prefix))
            val blockSize = 1 shl h

            // Align currentIp to the next appropriate block boundary if necessary
            if (currentIp % blockSize != 0) {
                currentIp = ((currentIp / blockSize) + 1) * blockSize
            }

            // Check if we've exhausted the available address space
            val broadcastIp = currentIp or subnetMask.inv()
            
            // Using unsigned comparison for IP addresses
            val currentIpLong = currentIp.toLong() and 0xFFFFFFFFL
            val broadcastIpLong = broadcastIp.toLong() and 0xFFFFFFFFL
            val maxIpLong = maxIp.toLong() and 0xFFFFFFFFL
            
            if (broadcastIpLong > maxIpLong || currentIpLong > broadcastIpLong) {
                throw IllegalStateException("Address space exhausted at segment '$segmentName'. Not enough space in $baseCidr.")
            }

            val totalUsable = if (h >= 31) 0 else (1 shl h) - 2
            val firstUsable = if (h >= 31) "N/A" else ipToString(currentIp + 1)
            val lastUsable = if (h >= 31) "N/A" else ipToString(broadcastIp - 1)

            results.add(
                SubnetResult(
                    subnetId = index + 1,
                    networkAddress = ipToString(currentIp),
                    firstUsableHost = firstUsable,
                    lastUsableHost = lastUsable,
                    broadcastAddress = ipToString(broadcastIp),
                    subnetMask = "${ipToString(subnetMask)}/$prefix",
                    totalUsableHosts = totalUsable,
                    segmentName = segmentName,
                    requiredHosts = hostsNeeded
                )
            )

            // Advance to the next available IP address
            currentIp = broadcastIp + 1
            if (currentIp == 0 && index < sortedReqs.size - 1) {
                // Wrapped around 32-bit integer, exhausted
                throw IllegalStateException("Address space exhausted. 32-bit boundary reached.")
            }
        }

        return results
    }

    /**
     * Core generation function that executes FLSM subnetting math and outputs a list of SubnetResult.
     */
    fun generateSubnets(baseCidr: String, targetCount: Int, isHostCount: Boolean): List<SubnetResult> {
        val (baseIp, basePrefix) = parseCidr(baseCidr)
        val borrowedBits = calculateBorrowedBits(targetCount, isHostCount, basePrefix)
        val newPrefix = basePrefix + borrowedBits
        
        require(newPrefix <= 32) { 
            "Not enough bits remaining in network prefix /$basePrefix to satisfy the requested criteria." 
        }

        val newMask = if (newPrefix == 0) 0 else (-1 shl (32 - newPrefix))
        val maskStr = ipToString(newMask)
        val baseNetwork = baseIp and (if (basePrefix == 0) 0 else (-1 shl (32 - basePrefix)))

        val subnetsCount = 1 shl borrowedBits
        val displayCount = minOf(subnetsCount, 2048)

        val hostBits = 32 - newPrefix
        val totalUsableHosts = if (hostBits >= 31) 0 else (1 shl hostBits) - 2

        val results = mutableListOf<SubnetResult>()

        for (i in 0 until displayCount) {
            val subnetNetwork = baseNetwork or (i shl (32 - newPrefix))
            val subnetBroadcast = subnetNetwork or newMask.inv()

            val firstUsable = if (newPrefix >= 31) "N/A" else ipToString(subnetNetwork + 1)
            val lastUsable = if (newPrefix >= 31) "N/A" else ipToString(subnetBroadcast - 1)

            results.add(
                SubnetResult(
                    subnetId = i + 1,
                    networkAddress = ipToString(subnetNetwork),
                    firstUsableHost = firstUsable,
                    lastUsableHost = lastUsable,
                    broadcastAddress = ipToString(subnetBroadcast),
                    subnetMask = "$maskStr/$newPrefix",
                    totalUsableHosts = totalUsableHosts
                )
            )
        }

        return results
    }

    private fun calculateBorrowedBits(targetCount: Int, isHostCount: Boolean, basePrefix: Int): Int {
        require(targetCount > 0) { "Target count must be greater than zero." }
        if (isHostCount) {
            val requiredHosts = targetCount + 2
            var h = 0
            while ((1 shl h) < requiredHosts && h < 32) {
                h++
            }
            val remainingHostBits = 32 - basePrefix
            val borrowed = remainingHostBits - h
            return if (borrowed < 0) 0 else borrowed
        } else {
            var n = 0
            while ((1 shl n) < targetCount && n < 32) {
                n++
            }
            return n
        }
    }
}
