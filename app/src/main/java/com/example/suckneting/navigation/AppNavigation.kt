package com.example.suckneting.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.suckneting.ui.screens.AnalyzerScreen
import com.example.suckneting.ui.screens.FlsmScreen
import com.example.suckneting.ui.screens.QuizScreen
import com.example.suckneting.ui.screens.VlsmScreen

/**
 * Centrally managed navigation graph for SubnetPro with premium fluid transitions.
 */
@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Flsm.route,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { 400 },
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -400 },
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -400 },
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { 400 },
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        }
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
