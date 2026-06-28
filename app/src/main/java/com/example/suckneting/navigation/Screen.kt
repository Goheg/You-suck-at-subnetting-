package com.example.suckneting.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schema
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Type-safe navigation routes for SubnetPro.
 */
sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Flsm : Screen(
        route = "flsm",
        label = "FLSM",
        icon = Icons.Default.GridView
    )

    object Vlsm : Screen(
        route = "vlsm",
        label = "VLSM",
        icon = Icons.Default.Schema
    )

    object Quiz : Screen(
        route = "quiz",
        label = "Quiz",
        icon = Icons.Default.Psychology
    )

    object Analyzer : Screen(
        route = "analyzer",
        label = "Analyzer",
        icon = Icons.Default.Assessment
    )

    companion object {
        val items = listOf(Flsm, Vlsm, Quiz, Analyzer)
    }
}
