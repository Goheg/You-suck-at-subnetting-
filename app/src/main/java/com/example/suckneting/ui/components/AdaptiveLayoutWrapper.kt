package com.example.suckneting.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.example.suckneting.navigation.Screen

/**
 * A highly responsive structural wrapper that adapts between BottomBar, NavigationRail,
 * and NavigationDrawer based on the WindowSizeClass.
 */
@Composable
fun AdaptiveLayoutWrapper(
    windowWidthSizeClass: WindowWidthSizeClass,
    currentDestination: NavDestination?,
    onNavigate: (Screen) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val isExpanded = windowWidthSizeClass == WindowWidthSizeClass.Expanded
    val isMedium = windowWidthSizeClass == WindowWidthSizeClass.Medium

    if (isExpanded || isMedium) {
        // Tablet / Desktop / Foldable Landscape: Side Navigation
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                modifier = Modifier.fillMaxHeight(),
                containerColor = MaterialTheme.colorScheme.surface,
                header = {
                    Text(
                        "SubnetPro",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            ) {
                Spacer(Modifier.weight(1f))
                Screen.items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationRailItem(
                        selected = selected,
                        onClick = { onNavigate(screen) },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        alwaysShowLabel = isExpanded
                    )
                }
                Spacer(Modifier.weight(1f))
            }

            Box(modifier = Modifier.weight(1f)) {
                content(PaddingValues(0.dp))
            }
        }
    } else {
        // Phone: Bottom Navigation
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Screen.items.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = { onNavigate(screen) },
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            content(innerPadding)
        }
    }
}
