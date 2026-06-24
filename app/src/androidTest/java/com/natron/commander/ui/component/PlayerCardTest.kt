package com.natron.commander.ui.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.natron.commander.model.EliminationReason
import com.natron.commander.model.Player
import com.natron.commander.model.PlayerColor
import com.natron.commander.ui.theme.CommanderTheme
import org.junit.Rule
import org.junit.Test

class PlayerCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun playerCard_displaysLifeTotal() {
        val player = Player(id = 0, name = "Aragorn", lifeTotal = 40, colorTheme = PlayerColor.ISLAND)
        composeRule.setContent {
            CommanderTheme {
                PlayerCard(
                    player = player,
                    rotationDegrees = 0f,
                    onLifeAdjust = {},
                    onLifeTap = {},
                    onCommanderDamageTap = {},
                    onPoisonAdjust = {}
                )
            }
        }
        composeRule.onNodeWithText("40").assertIsDisplayed()
    }

    @Test
    fun playerCard_displaysPlayerName() {
        val player = Player(id = 0, name = "Legolas", lifeTotal = 40, colorTheme = PlayerColor.FOREST)
        composeRule.setContent {
            CommanderTheme {
                PlayerCard(
                    player = player,
                    rotationDegrees = 0f,
                    onLifeAdjust = {},
                    onLifeTap = {},
                    onCommanderDamageTap = {},
                    onPoisonAdjust = {}
                )
            }
        }
        composeRule.onNodeWithText("Legolas").assertIsDisplayed()
    }

    @Test
    fun playerCard_eliminatedShowsDeathOverlay() {
        val player = Player(
            id = 0,
            name = "Gandalf",
            lifeTotal = 0,
            colorTheme = PlayerColor.PLAINS,
            isEliminated = true,
            eliminationReason = EliminationReason.LIFE
        )
        composeRule.setContent {
            CommanderTheme {
                PlayerCard(
                    player = player,
                    rotationDegrees = 0f,
                    onLifeAdjust = {},
                    onLifeTap = {},
                    onCommanderDamageTap = {},
                    onPoisonAdjust = {}
                )
            }
        }
        composeRule.onNodeWithText("ELIMINATED").assertIsDisplayed()
    }
}
