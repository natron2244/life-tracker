@file:OptIn(ExperimentalStdlibApi::class)

package com.natron.commander.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.natron.commander.model.GameState
import com.natron.commander.model.Player
import com.natron.commander.model.PlayerColor
import com.natron.commander.ui.theme.*
import com.natron.commander.ui.theme.CommanderTheme
import com.natron.commander.viewmodel.GameViewModel

@Composable
fun SetupScreen(viewModel: GameViewModel, state: GameState) {
    val focusManager = LocalFocusManager.current

    val players = remember(state.playerCount, state.players) {
        val colors = PlayerColor.entries
        (0 until state.playerCount).map { i ->
            state.players.getOrNull(i) ?: Player(
                id = i,
                name = "",
                colorTheme = colors[i % colors.size]
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        Text(
            text = "COMMANDER",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize * 1.8f),
            color = MythicGold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Life Tracker",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceMuted,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        Text(
            "Players",
            style = MaterialTheme.typography.titleMedium,
            color = OnSurface,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Player count segmented control
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (2..6).forEach { count ->
                val selected = count == state.playerCount
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) MythicGold else SurfaceVariant)
                        .clickable { viewModel.setPlayerCount(count) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) CardBack else OnSurfaceMuted
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Player config rows
        players.forEach { player ->
            val id = player.id
            key(id) {
                PlayerSetupRow(
                    player = player,
                    onNameChange = { viewModel.setPlayerName(id, it) },
                    onColorChange = { viewModel.setPlayerColor(id, it) },
                    focusManager = focusManager
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel.startGame() },
            modifier = Modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MythicGold,
                contentColor = CardBack
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                "Start Game",
                style = MaterialTheme.typography.titleMedium,
                color = CardBack
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun PlayerSetupRow(
    player: Player,
    onNameChange: (String) -> Unit,
    onColorChange: (PlayerColor) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    var showColorMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceVariant)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Color picker swatch
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(player.colorTheme.primary)
                .border(2.dp, OnSurfaceMuted.copy(alpha = 0.4f), CircleShape)
                .clickable { showColorMenu = true },
            contentAlignment = Alignment.Center
        ) {}

        DropdownMenu(
            expanded = showColorMenu,
            onDismissRequest = { showColorMenu = false }
        ) {
            PlayerColor.entries.forEach { color ->
                key(color) {
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(color.primary)
                                )
                                Text(color.displayName, color = OnSurface)
                            }
                        },
                        onClick = {
                            onColorChange(color)
                            showColorMenu = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = player.name,
            onValueChange = onNameChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    "Player ${player.id + 1}",
                    color = OnSurfaceMuted
                )
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MythicGold,
                unfocusedBorderColor = OnSurfaceMuted.copy(alpha = 0.3f),
                focusedTextColor = OnSurface,
                unfocusedTextColor = OnSurface,
                cursorColor = MythicGold
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
private fun SetupScreenPreview() {
    val viewModel = GameViewModel()
    val state = GameState(
        playerCount = 4,
        players = listOf(
            Player(id = 0, name = "Aragorn", lifeTotal = 40, colorTheme = PlayerColor.ISLAND),
            Player(id = 1, name = "Legolas", lifeTotal = 40, colorTheme = PlayerColor.FOREST),
            Player(id = 2, name = "Gandalf", lifeTotal = 40, colorTheme = PlayerColor.PLAINS),
            Player(id = 3, name = "Gimli", lifeTotal = 40, colorTheme = PlayerColor.MOUNTAIN)
        )
    )
    CommanderTheme {
        SetupScreen(viewModel = viewModel, state = state)
    }
}
