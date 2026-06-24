package com.natron.commander.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.natron.commander.model.GameState
import com.natron.commander.model.Player
import com.natron.commander.ui.component.*
import com.natron.commander.ui.theme.*
import com.natron.commander.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel, state: GameState) {
    val players = state.players

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CardBack)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Minimal top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.showNewGameDialog() }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = OnSurfaceMuted)
                }
                TextButton(onClick = { viewModel.showDiceRoller() }) {
                    Text("🎲", style = MaterialTheme.typography.titleMedium)
                }
            }

            // Player grid fills remaining space
            PlayerGrid(
                players = players,
                modifier = Modifier.weight(1f),
                onLifeAdjust = { id, delta -> viewModel.adjustLife(id, delta) },
                onLifeTap = { id -> viewModel.openDirectInput(id) },
                onCommanderDamageTap = { id -> viewModel.openCommanderDamageSheet(id) },
                onPoisonAdjust = { id, delta -> viewModel.adjustPoison(id, delta) }
            )
        }

        // Sheets and dialogs
        if (state.showDiceRoller) {
            DiceRollerSheet(
                activeDiceResult = state.activeDiceResult,
                onRoll = { viewModel.rollDie(it) },
                onDismiss = { viewModel.hideDiceRoller() }
            )
        }

        state.commanderDamageTargetPlayerId?.let { targetId ->
            val targetPlayer = players.find { it.id == targetId }
            if (targetPlayer != null) {
                CommanderDamageSheet(
                    targetPlayer = targetPlayer,
                    allPlayers = players,
                    onAdjust = { sourceId, delta ->
                        viewModel.adjustCommanderDamage(targetId, sourceId, delta)
                    },
                    onDismiss = { viewModel.closeCommanderDamageSheet() }
                )
            }
        }

        state.directInputTargetPlayerId?.let { targetId ->
            val targetPlayer = players.find { it.id == targetId }
            if (targetPlayer != null) {
                DirectInputDialog(
                    currentLife = targetPlayer.lifeTotal,
                    onConfirm = { value ->
                        viewModel.setLifeDirect(targetId, value)
                        viewModel.closeDirectInput()
                    },
                    onDismiss = { viewModel.closeDirectInput() }
                )
            }
        }

        if (state.showNewGameDialog) {
            NewGameDialog(
                onReset = { viewModel.resetGame() },
                onNewGame = { viewModel.newGame() },
                onDismiss = { viewModel.hideNewGameDialog() }
            )
        }
    }
}

@Composable
private fun PlayerGrid(
    players: List<Player>,
    modifier: Modifier = Modifier,
    onLifeAdjust: (Int, Int) -> Unit,
    onLifeTap: (Int) -> Unit,
    onCommanderDamageTap: (Int) -> Unit,
    onPoisonAdjust: (Int, Int) -> Unit
) {
    @Composable
    fun Card(player: Player, rotation: Float, mod: Modifier) {
        com.natron.commander.ui.component.PlayerCard(
            player = player,
            rotationDegrees = rotation,
            onLifeAdjust = { delta -> onLifeAdjust(player.id, delta) },
            onLifeTap = { onLifeTap(player.id) },
            onCommanderDamageTap = { onCommanderDamageTap(player.id) },
            onPoisonAdjust = { delta -> onPoisonAdjust(player.id, delta) },
            modifier = mod
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val w = maxWidth
        val h = maxHeight

        when (players.size) {
            2 -> Column(Modifier.fillMaxSize()) {
                Card(players[1], 180f, Modifier.weight(1f).fillMaxWidth())
                HorizontalDivider(thickness = 2.dp, color = CardBack)
                Card(players[0], 0f, Modifier.weight(1f).fillMaxWidth())
            }

            3 -> Column(Modifier.fillMaxSize()) {
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    Card(players[2], 180f, Modifier.weight(1f).fillMaxHeight())
                    VerticalDivider(thickness = 2.dp, color = CardBack)
                    Card(players[1], 180f, Modifier.weight(1f).fillMaxHeight())
                }
                HorizontalDivider(thickness = 2.dp, color = CardBack)
                Card(players[0], 0f, Modifier.weight(1f).fillMaxWidth())
            }

            4 -> Column(Modifier.fillMaxSize()) {
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    Card(players[3], 180f, Modifier.weight(1f).fillMaxHeight())
                    VerticalDivider(thickness = 2.dp, color = CardBack)
                    Card(players[2], 180f, Modifier.weight(1f).fillMaxHeight())
                }
                HorizontalDivider(thickness = 2.dp, color = CardBack)
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    Card(players[0], 0f, Modifier.weight(1f).fillMaxHeight())
                    VerticalDivider(thickness = 2.dp, color = CardBack)
                    Card(players[1], 0f, Modifier.weight(1f).fillMaxHeight())
                }
            }

            5 -> Column(Modifier.fillMaxSize()) {
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    Card(players[4], 180f, Modifier.weight(1f).fillMaxHeight())
                    VerticalDivider(thickness = 2.dp, color = CardBack)
                    Card(players[3], 180f, Modifier.weight(1f).fillMaxHeight())
                }
                HorizontalDivider(thickness = 2.dp, color = CardBack)
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    Card(players[2], 180f, Modifier.weight(1f).fillMaxHeight())
                    VerticalDivider(thickness = 2.dp, color = CardBack)
                    Card(players[1], 180f, Modifier.weight(1f).fillMaxHeight())
                }
                HorizontalDivider(thickness = 2.dp, color = CardBack)
                Card(players[0], 0f, Modifier.weight(1f).fillMaxWidth())
            }

            6 -> Column(Modifier.fillMaxSize()) {
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    Card(players[5], 180f, Modifier.weight(1f).fillMaxHeight())
                    VerticalDivider(thickness = 2.dp, color = CardBack)
                    Card(players[4], 180f, Modifier.weight(1f).fillMaxHeight())
                }
                HorizontalDivider(thickness = 2.dp, color = CardBack)
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    Card(players[3], 180f, Modifier.weight(1f).fillMaxHeight())
                    VerticalDivider(thickness = 2.dp, color = CardBack)
                    Card(players[2], 180f, Modifier.weight(1f).fillMaxHeight())
                }
                HorizontalDivider(thickness = 2.dp, color = CardBack)
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    Card(players[0], 0f, Modifier.weight(1f).fillMaxHeight())
                    VerticalDivider(thickness = 2.dp, color = CardBack)
                    Card(players[1], 0f, Modifier.weight(1f).fillMaxHeight())
                }
            }

            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No players", color = com.natron.commander.ui.theme.OnSurfaceMuted)
            }
        }
    }
}
