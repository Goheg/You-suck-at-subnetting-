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
            // Shift the current result left by 8 bits and bitwise OR the new octet
            result = (result shl 8) or part
        }
        return result
    }

    /**
     * Converts a 32-bit Int back into its standard dotted-decimal String representation.
     */
    fun ipToString(ip: Int): String {
        // Use unsigned right shift (ushr) to avoid sign-extension issues with the highest bit
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
     * Calculates the number of bits required to borrow from the host portion.
     * - For Host Count: Finds 'h' host bits such that 2^h - 2 >= targetCount,
     *   then borrowed bits = (32 - basePrefix) - h.
     * - For Subnet Count: Finds 'n' bits such that 2^n >= targetCount.
     */
    fun calculateBorrowedBits(targetCount: Int, isHostCount: Boolean, basePrefix: Int): Int {
        require(targetCount > 0) { "Target count must be greater than zero." }
        if (isHostCount) {
            val requiredHosts = targetCount + 2
            var h = 0
            // Increment host bits until 2^h can accommodate the required hosts
            while ((1 shl h) < requiredHosts && h < 32) {
                h++
            }
            val remainingHostBits = 32 - basePrefix
            val borrowed = remainingHostBits - h
            return if (borrowed < 0) 0 else borrowed
        } else {
            var n = 0
            // Increment subnet bits until 2^n can accommodate the required subnets
            while ((1 shl n) < targetCount && n < 32) {
                n++
            }
            return n
        }
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

        // Generate the subnet mask for the new prefix length
        // e.g., for /24, -1 shl (32 - 24) = -1 shl 8 = 0xFFFFFF00
        val newMask = if (newPrefix == 0) 0 else (-1 shl (32 - newPrefix))
        val maskStr = ipToString(newMask)

        // Clear the host bits of the base IP to get the true network base address
        val baseNetwork = baseIp and (if (basePrefix == 0) 0 else (-1 shl (32 - basePrefix)))

        // Calculate total number of subnets to generate (2^borrowedBits)
        // Cap at a safe maximum of 2048 to prevent memory overflow in production lists
        val subnetsCount = 1 shl borrowedBits
        val displayCount = minOf(subnetsCount, 2048)

        // Calculate total usable hosts per subnet: 2^(32 - newPrefix) - 2
        val hostBits = 32 - newPrefix
        val totalUsableHosts = if (hostBits >= 31) 0 else (1 shl hostBits) - 2

        val results = mutableListOf<SubnetResult>()

        for (i in 0 until displayCount) {
            // Each subnet's network address is shifted by the size of the new subnet block
            val subnetNetwork = baseNetwork or (i shl (32 - newPrefix))
            // Broadcast address has all host bits set to 1 (bitwise inversion of mask)
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
                    subnetMask = maskStr,
                    totalUsableHosts = totalUsableHosts
                )
            )
        }

        return results
    }
}
