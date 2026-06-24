@file:OptIn(ExperimentalStdlibApi::class)

package com.natron.commander.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.ViewModel
import com.natron.commander.model.Die
import com.natron.commander.model.DiceResult
import com.natron.commander.model.EliminationReason
import com.natron.commander.model.GameState
import com.natron.commander.model.Player
import com.natron.commander.model.PlayerColor

class GameViewModel : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    // region Setup

    fun setPlayerCount(count: Int) {
        val clamped = count.coerceIn(2, 6)
        _state.update { it.copy(playerCount = clamped) }
    }

    fun setPlayerName(id: Int, name: String) {
        _state.update { state ->
            state.copy(players = state.players.map { p ->
                if (p.id == id) p.copy(name = name) else p
            })
        }
    }

    fun setPlayerColor(id: Int, color: PlayerColor) {
        _state.update { state ->
            state.copy(players = state.players.map { p ->
                if (p.id == id) p.copy(colorTheme = color) else p
            })
        }
    }

    fun startGame() {
        val count = _state.value.playerCount
        val existingPlayers = _state.value.players
        val colors = PlayerColor.entries
        val players = (0 until count).map { i ->
            val existing = existingPlayers.getOrNull(i)
            Player(
                id = i,
                name = existing?.name?.takeIf { it.isNotBlank() } ?: "Player ${i + 1}",
                colorTheme = existing?.colorTheme ?: colors[i % colors.size],
                lifeTotal = 40,
                poisonCounters = 0,
                commanderDamage = emptyMap(),
                isEliminated = false,
                eliminationReason = null
            )
        }
        _state.update { it.copy(players = players, isGameStarted = true) }
    }

    // endregion

    // region In-game

    fun adjustLife(playerId: Int, delta: Int) {
        updatePlayer(playerId) { p ->
            checkElimination(p.copy(lifeTotal = p.lifeTotal + delta))
        }
    }

    fun setLifeDirect(playerId: Int, value: Int) {
        updatePlayer(playerId) { p ->
            checkElimination(p.copy(lifeTotal = value))
        }
    }

    fun adjustCommanderDamage(receivingPlayerId: Int, sourcePlayerId: Int, delta: Int) {
        updatePlayer(receivingPlayerId) { p ->
            val current = p.commanderDamage[sourcePlayerId] ?: 0
            val newVal = (current + delta).coerceAtLeast(0)
            checkElimination(p.copy(commanderDamage = p.commanderDamage + (sourcePlayerId to newVal)))
        }
    }

    fun adjustPoison(playerId: Int, delta: Int) {
        updatePlayer(playerId) { p ->
            checkElimination(p.copy(poisonCounters = (p.poisonCounters + delta).coerceAtLeast(0)))
        }
    }

    fun openCommanderDamageSheet(playerId: Int) {
        _state.update { it.copy(commanderDamageTargetPlayerId = playerId) }
    }

    fun closeCommanderDamageSheet() {
        _state.update { it.copy(commanderDamageTargetPlayerId = null) }
    }

    fun openDirectInput(playerId: Int) {
        _state.update { it.copy(directInputTargetPlayerId = playerId) }
    }

    fun closeDirectInput() {
        _state.update { it.copy(directInputTargetPlayerId = null) }
    }

    // endregion

    // region Dice

    fun rollDie(die: Die) {
        val value = (1..die.faces).random()
        _state.update { it.copy(activeDiceResult = DiceResult(die, value)) }
    }

    fun clearDiceResult() {
        _state.update { it.copy(activeDiceResult = null) }
    }

    fun showDiceRoller() {
        _state.update { it.copy(showDiceRoller = true) }
    }

    fun hideDiceRoller() {
        _state.update { it.copy(showDiceRoller = false, activeDiceResult = null) }
    }

    // endregion

    // region Game management

    fun showNewGameDialog() {
        _state.update { it.copy(showNewGameDialog = true) }
    }

    fun hideNewGameDialog() {
        _state.update { it.copy(showNewGameDialog = false) }
    }

    fun resetGame() {
        val players = _state.value.players.map { p ->
            p.copy(
                lifeTotal = 40,
                poisonCounters = 0,
                commanderDamage = emptyMap(),
                isEliminated = false,
                eliminationReason = null
            )
        }
        _state.update { it.copy(players = players, showNewGameDialog = false, activeDiceResult = null) }
    }

    fun newGame() {
        _state.update {
            it.copy(
                isGameStarted = false,
                showNewGameDialog = false,
                activeDiceResult = null,
                commanderDamageTargetPlayerId = null,
                showDiceRoller = false
            )
        }
    }

    // endregion

    private fun checkElimination(player: Player): Player {
        val eliminated = player.lifeTotal <= 0
            || player.poisonCounters >= 10
            || player.commanderDamage.values.any { it >= 21 }
        val reason = when {
            player.lifeTotal <= 0 -> EliminationReason.LIFE
            player.poisonCounters >= 10 -> EliminationReason.POISON
            player.commanderDamage.values.any { it >= 21 } -> EliminationReason.COMMANDER
            else -> null
        }
        return player.copy(isEliminated = eliminated, eliminationReason = reason)
    }

    private fun updatePlayer(id: Int, transform: (Player) -> Player) {
        _state.update { state ->
            state.copy(players = state.players.map { p ->
                if (p.id == id) transform(p) else p
            })
        }
    }
}
