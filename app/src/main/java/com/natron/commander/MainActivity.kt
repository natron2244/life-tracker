package com.natron.commander

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.natron.commander.navigation.AppNavigation
import com.natron.commander.ui.theme.CommanderTheme
import com.natron.commander.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CommanderTheme {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}
