package com.example.suckneting.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.suckneting.ui.screens.AnalyzerScreen
import com.example.suckneting.ui.screens.FlsmScreen
import com.example.suckneting.ui.screens.QuizScreen
import com.example.suckneting.ui.screens.VlsmScreen

/**
 * Centrally managed navigation graph for the SubnetPro application.
 */
@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Flsm.route
    ) {
        composable(Screen.Flsm.route) {
            FlsmScreen()
        }
        composable(Screen.Vlsm.route) {
            VlsmScreen()
        }
        composable(Screen.Quiz.route) {
            QuizScreen()
        }
        composable(Screen.Analyzer.route) {
            AnalyzerScreen()
        }
    }
}
