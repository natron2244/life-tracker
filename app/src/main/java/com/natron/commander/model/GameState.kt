package com.natron.commander.model

data class GameState(
    val playerCount: Int = 4,
    val players: List<Player> = emptyList(),
    val gameStarted: Boolean = false,
    val activeDiceResult: DiceResult? = null,
    val commanderDamageTargetPlayerId: Int? = null,
    val showDiceRoller: Boolean = false,
    val showNewGameDialog: Boolean = false,
    val directInputTargetPlayerId: Int? = null
)
