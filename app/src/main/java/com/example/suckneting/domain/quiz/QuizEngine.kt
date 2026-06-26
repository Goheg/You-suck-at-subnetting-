package com.example.suckneting.domain.quiz

import com.example.suckneting.domain.math.SubnetEngine
import kotlin.random.Random

data class SubnetQuestion(
    val theQuestion: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)

object QuizEngine {

    fun generateQuestion(): SubnetQuestion {
        return when (Random.nextInt(3)) {
            0 -> generateNetworkAddressQuestion()
            1 -> generateBroadcastAddressQuestion()
            else -> generateHostCountQuestion()
        }
    }

    private fun generateNetworkAddressQuestion(): SubnetQuestion {
        val cidr = Random.nextInt(24, 31)
        val ipInt = generateRandomIp()
        val ipStr = SubnetEngine.ipToString(ipInt)
        
        val mask = if (cidr == 0) 0 else (-1 shl (32 - cidr))
        val networkInt = ipInt and mask
        val correctAnswer = SubnetEngine.ipToString(networkInt)
        
        val options = mutableListOf(correctAnswer)
        while (options.size < 4) {
            val distractor = SubnetEngine.ipToString(generateRandomIp() and mask)
            if (distractor !in options) options.add(distractor)
        }
        options.shuffle()
        
        return SubnetQuestion(
            theQuestion = "What is the Network Address for the IP $ipStr/$cidr?",
            options = options,
            correctOptionIndex = options.indexOf(correctAnswer),
            explanation = "To find the network address, perform a bitwise AND between the IP address and the subnet mask ($cidr = ${SubnetEngine.ipToString(mask)})."
        )
    }

    private fun generateBroadcastAddressQuestion(): SubnetQuestion {
        val cidr = Random.nextInt(24, 31)
        val ipInt = generateRandomIp()
        val ipStr = SubnetEngine.ipToString(ipInt)
        
        val mask = if (cidr == 0) 0 else (-1 shl (32 - cidr))
        val networkInt = ipInt and mask
        val broadcastInt = networkInt or mask.inv()
        val correctAnswer = SubnetEngine.ipToString(broadcastInt)
        
        val options = mutableListOf(correctAnswer)
        while (options.size < 4) {
            val distractor = SubnetEngine.ipToString((generateRandomIp() and mask) or mask.inv())
            if (distractor !in options) options.add(distractor)
        }
        options.shuffle()
        
        return SubnetQuestion(
            theQuestion = "What is the Broadcast Address for the IP $ipStr/$cidr?",
            options = options,
            correctOptionIndex = options.indexOf(correctAnswer),
            explanation = "To find the broadcast address, take the network address and set all host bits (the last ${32 - cidr} bits) to 1."
        )
    }

    private fun generateHostCountQuestion(): SubnetQuestion {
        val cidr = Random.nextInt(24, 31)
        val hostBits = 32 - cidr
        val totalHosts = 1 shl hostBits
        val usableHosts = if (hostBits >= 2) totalHosts - 2 else 0
        val correctAnswer = usableHosts.toString()
        
        val options = mutableSetOf(correctAnswer)
        options.add(totalHosts.toString()) // Distractor: Total hosts instead of usable
        options.add((usableHosts + 2).toString()) // Distractor
        while (options.size < 4) {
            val randomCidr = Random.nextInt(24, 31)
            val h = 32 - randomCidr
            val dist = if (h >= 2) (1 shl h) - 2 else 0
            options.add(dist.toString())
        }
        val shuffledOptions = options.toList().shuffled()
        
        return SubnetQuestion(
            theQuestion = "How many usable hosts are available in a /$cidr network?",
            options = shuffledOptions,
            correctOptionIndex = shuffledOptions.indexOf(correctAnswer),
            explanation = "Usable hosts = 2^(32 - prefix) - 2. For /$cidr, that is 2^$hostBits - 2 = $totalHosts - 2 = $usableHosts."
        )
    }

    private fun generateRandomIp(): Int {
        // Generate IPs in common private ranges for realism
        val firstOctet = 192
        val secondOctet = 168
        val thirdOctet = Random.nextInt(256)
        val fourthOctet = Random.nextInt(256)
        
        return (firstOctet shl 24) or (secondOctet shl 16) or (thirdOctet shl 8) or fourthOctet
    }
}
