package com.natron.commander.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.natron.commander.ui.screen.GameScreen
import com.natron.commander.ui.screen.SetupScreen
import com.natron.commander.viewmodel.GameViewModel

sealed class Screen(val route: String) {
    object Setup : Screen("setup")
    object Game : Screen("game")
}

@Composable
fun AppNavigation(viewModel: GameViewModel) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsState()

    NavHost(navController = navController, startDestination = Screen.Setup.route) {
        composable(Screen.Setup.route) {
            LaunchedEffect(state.gameStarted) {
                if (state.gameStarted) {
                    navController.navigate(Screen.Game.route)
                }
            }
            SetupScreen(viewModel = viewModel, state = state)
        }
        composable(Screen.Game.route) {
            LaunchedEffect(state.gameStarted) {
                if (!state.gameStarted) {
                    navController.navigate(Screen.Setup.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            GameScreen(viewModel = viewModel, state = state)
        }
    }
}
