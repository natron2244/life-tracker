package com.natron.commander.viewmodel

import com.natron.commander.model.EliminationReason
import com.natron.commander.model.PlayerColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GameViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() {
        viewModel = GameViewModel()
    }

    // region Setup

    @Test
    fun `setPlayerCount clamps to valid range`() {
        viewModel.setPlayerCount(1)
        assertEquals(2, viewModel.state.value.playerCount)

        viewModel.setPlayerCount(7)
        assertEquals(6, viewModel.state.value.playerCount)

        viewModel.setPlayerCount(4)
        assertEquals(4, viewModel.state.value.playerCount)
    }

    @Test
    fun `startGame creates players with default life and transitions isGameStarted`() {
        viewModel.setPlayerCount(3)
        viewModel.startGame()
        val state = viewModel.state.value
        assertTrue(state.isGameStarted)
        assertEquals(3, state.players.size)
        state.players.forEach { assertEquals(40, it.lifeTotal) }
    }

    @Test
    fun `startGame assigns unique IDs starting at zero`() {
        viewModel.setPlayerCount(4)
        viewModel.startGame()
        val ids = viewModel.state.value.players.map { it.id }
        assertEquals(listOf(0, 1, 2, 3), ids)
    }

    @Test
    fun `setPlayerName after game reset persists into next startGame`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.setPlayerName(0, "Gandalf")
        viewModel.newGame()
        viewModel.startGame()
        assertEquals("Gandalf", viewModel.state.value.players[0].name)
    }

    @Test
    fun `setPlayerColor after game reset persists into next startGame`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.setPlayerColor(0, PlayerColor.MOUNTAIN)
        viewModel.newGame()
        viewModel.startGame()
        assertEquals(PlayerColor.MOUNTAIN, viewModel.state.value.players[0].colorTheme)
    }

    // endregion

    // region adjustLife

    @Test
    fun `adjustLife reduces life total`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.adjustLife(playerId = 0, delta = -5)
        assertEquals(35, viewModel.state.value.players[0].lifeTotal)
    }

    @Test
    fun `adjustLife increases life total`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.adjustLife(playerId = 0, delta = -10)
        viewModel.adjustLife(playerId = 0, delta = 5)
        assertEquals(35, viewModel.state.value.players[0].lifeTotal)
    }

    @Test
    fun `adjustLife to exactly zero eliminates player with LIFE reason`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.adjustLife(playerId = 0, delta = -40)
        val player = viewModel.state.value.players[0]
        assertTrue(player.isEliminated)
        assertEquals(EliminationReason.LIFE, player.eliminationReason)
    }

    @Test
    fun `adjustLife below zero eliminates player`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.adjustLife(playerId = 0, delta = -50)
        assertTrue(viewModel.state.value.players[0].isEliminated)
    }

    @Test
    fun `adjustLife does not affect other players`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.adjustLife(playerId = 0, delta = -10)
        assertEquals(40, viewModel.state.value.players[1].lifeTotal)
    }

    // endregion

    // region setLifeDirect

    @Test
    fun `setLifeDirect sets life to exact value`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.setLifeDirect(playerId = 1, value = 25)
        assertEquals(25, viewModel.state.value.players[1].lifeTotal)
    }

    @Test
    fun `setLifeDirect to zero eliminates player`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.setLifeDirect(playerId = 1, value = 0)
        val player = viewModel.state.value.players[1]
        assertTrue(player.isEliminated)
        assertEquals(EliminationReason.LIFE, player.eliminationReason)
    }

    @Test
    fun `setLifeDirect to negative eliminates player`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.setLifeDirect(playerId = 1, value = -1)
        assertTrue(viewModel.state.value.players[1].isEliminated)
    }

    // endregion

    // region adjustPoison

    @Test
    fun `adjustPoison increments poison counter`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.adjustPoison(playerId = 0, delta = 3)
        assertEquals(3, viewModel.state.value.players[0].poisonCounters)
    }

    @Test
    fun `adjustPoison does not go below zero`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.adjustPoison(playerId = 0, delta = -5)
        assertEquals(0, viewModel.state.value.players[0].poisonCounters)
    }

    @Test
    fun `adjustPoison to 10 eliminates player with POISON reason`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.adjustPoison(playerId = 0, delta = 10)
        val player = viewModel.state.value.players[0]
        assertTrue(player.isEliminated)
        assertEquals(EliminationReason.POISON, player.eliminationReason)
    }

    @Test
    fun `adjustPoison above 10 eliminates player`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.adjustPoison(playerId = 0, delta = 11)
        assertTrue(viewModel.state.value.players[0].isEliminated)
    }

    // endregion

    // region adjustCommanderDamage

    @Test
    fun `adjustCommanderDamage tracks per-source damage`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.adjustCommanderDamage(receivingPlayerId = 0, sourcePlayerId = 1, delta = 5)
        assertEquals(5, viewModel.state.value.players[0].commanderDamage[1])
    }

    @Test
    fun `adjustCommanderDamage accumulates across multiple calls`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.adjustCommanderDamage(receivingPlayerId = 0, sourcePlayerId = 1, delta = 10)
        viewModel.adjustCommanderDamage(receivingPlayerId = 0, sourcePlayerId = 1, delta = 8)
        assertEquals(18, viewModel.state.value.players[0].commanderDamage[1])
    }

    @Test
    fun `adjustCommanderDamage does not go below zero`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.adjustCommanderDamage(receivingPlayerId = 0, sourcePlayerId = 1, delta = -5)
        assertEquals(0, viewModel.state.value.players[0].commanderDamage[1])
    }

    @Test
    fun `adjustCommanderDamage to 21 eliminates player with COMMANDER reason`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.adjustCommanderDamage(receivingPlayerId = 0, sourcePlayerId = 1, delta = 21)
        val player = viewModel.state.value.players[0]
        assertTrue(player.isEliminated)
        assertEquals(EliminationReason.COMMANDER, player.eliminationReason)
    }

    @Test
    fun `adjustCommanderDamage above 21 eliminates player`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.adjustCommanderDamage(receivingPlayerId = 0, sourcePlayerId = 1, delta = 22)
        assertTrue(viewModel.state.value.players[0].isEliminated)
    }

    // endregion

    // region Dice

    @Test
    fun `rollDie sets activeDiceResult with correct die and in-range value`() {
        viewModel.rollDie(com.natron.commander.model.Die.D20)
        val result = viewModel.state.value.activeDiceResult
        assertNotNull(result)
        assertEquals(com.natron.commander.model.Die.D20, result!!.die)
        assertTrue(result.value in 1..20)
    }

    @Test
    fun `clearDiceResult removes activeDiceResult`() {
        viewModel.rollDie(com.natron.commander.model.Die.D6)
        viewModel.clearDiceResult()
        assertNull(viewModel.state.value.activeDiceResult)
    }

    @Test
    fun `showDiceRoller sets showDiceRoller flag`() {
        viewModel.showDiceRoller()
        assertTrue(viewModel.state.value.showDiceRoller)
    }

    @Test
    fun `hideDiceRoller clears showDiceRoller and activeDiceResult`() {
        viewModel.showDiceRoller()
        viewModel.rollDie(com.natron.commander.model.Die.D6)
        viewModel.hideDiceRoller()
        val state = viewModel.state.value
        assertFalse(state.showDiceRoller)
        assertNull(state.activeDiceResult)
    }

    // endregion

    // region Game management

    @Test
    fun `resetGame restores all players to 40 life`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.adjustLife(playerId = 0, delta = -15)
        viewModel.adjustPoison(playerId = 1, delta = 5)
        viewModel.resetGame()
        val state = viewModel.state.value
        state.players.forEach {
            assertEquals(40, it.lifeTotal)
            assertEquals(0, it.poisonCounters)
            assertTrue(it.commanderDamage.isEmpty())
            assertFalse(it.isEliminated)
            assertNull(it.eliminationReason)
        }
    }

    @Test
    fun `resetGame closes new game dialog`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.showNewGameDialog()
        viewModel.resetGame()
        assertFalse(viewModel.state.value.showNewGameDialog)
    }

    @Test
    fun `newGame sets isGameStarted to false`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.newGame()
        assertFalse(viewModel.state.value.isGameStarted)
    }

    @Test
    fun `showNewGameDialog and hideNewGameDialog toggle flag`() {
        viewModel.showNewGameDialog()
        assertTrue(viewModel.state.value.showNewGameDialog)
        viewModel.hideNewGameDialog()
        assertFalse(viewModel.state.value.showNewGameDialog)
    }

    @Test
    fun `openDirectInput and closeDirectInput toggle targetId`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.openDirectInput(1)
        assertEquals(1, viewModel.state.value.directInputTargetPlayerId)
        viewModel.closeDirectInput()
        assertNull(viewModel.state.value.directInputTargetPlayerId)
    }

    @Test
    fun `openCommanderDamageSheet and closeCommanderDamageSheet toggle targetId`() {
        viewModel.setPlayerCount(2)
        viewModel.startGame()
        viewModel.openCommanderDamageSheet(0)
        assertEquals(0, viewModel.state.value.commanderDamageTargetPlayerId)
        viewModel.closeCommanderDamageSheet()
        assertNull(viewModel.state.value.commanderDamageTargetPlayerId)
    }

    // endregion
}
