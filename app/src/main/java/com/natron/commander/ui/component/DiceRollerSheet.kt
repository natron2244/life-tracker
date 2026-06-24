@file:OptIn(ExperimentalStdlibApi::class)

package com.natron.commander.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.tooling.preview.Preview
import com.natron.commander.ui.theme.CommanderTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.natron.commander.model.Die
import com.natron.commander.model.DiceResult
import com.natron.commander.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiceRollerSheet(
    activeDiceResult: DiceResult?,
    onRoll: (Die) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceVariant,
        dragHandle = { BottomSheetDefaults.DragHandle(color = OnSurfaceMuted) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Roll Dice",
                style = MaterialTheme.typography.titleLarge,
                color = MythicGold
            )

            // Die buttons
            val allDice = Die.entries
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allDice.take(4).forEach { die ->
                    key(die) {
                        DieButton(die = die, onClick = { onRoll(die) }, modifier = Modifier.weight(1f))
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allDice.drop(4).forEach { die ->
                    key(die) {
                        DieButton(die = die, onClick = { onRoll(die) }, modifier = Modifier.weight(1f))
                    }
                }
                // Spacers to keep alignment with row above
                repeat(4 - allDice.drop(4).size) {
                    Spacer(Modifier.weight(1f))
                }
            }

            // Result display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = activeDiceResult,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "dice_result"
                ) { result ->
                    if (result != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${result.value}",
                                style = MaterialTheme.typography.displayMedium,
                                color = MythicGold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = result.die.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceMuted
                            )
                        }
                    } else {
                        Text(
                            text = "—",
                            style = MaterialTheme.typography.displayMedium,
                            color = OnSurfaceMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DieButton(die: Die, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurface),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            // Use default outlined border styling
        )
    ) {
        Text(die.label, style = MaterialTheme.typography.labelMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun DiceRollerSheetIdlePreview() {
    CommanderTheme {
        DiceRollerSheet(activeDiceResult = null, onRoll = {}, onDismiss = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun DiceRollerSheetWithResultPreview() {
    CommanderTheme {
        DiceRollerSheet(
            activeDiceResult = DiceResult(die = Die.D20, value = 17),
            onRoll = {},
            onDismiss = {}
        )
    }
}
