package com.natron.commander.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.natron.commander.model.Player
import com.natron.commander.ui.theme.OnSurfaceMuted
import com.natron.commander.ui.theme.WarningRed

@Composable
fun PlayerCard(
    player: Player,
    rotationDegrees: Float,
    onLifeAdjust: (Int) -> Unit,
    onLifeTap: () -> Unit,
    onCommanderDamageTap: () -> Unit,
    onPoisonAdjust: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = player.colorTheme
    val totalCommanderDamage = player.commanderDamage.values.sum()
    val commanderWarning = player.commanderDamage.values.any { it >= 15 }

    Box(
        modifier = modifier
            .graphicsLayer { rotationZ = rotationDegrees }
            .background(theme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row: name + commander damage badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = theme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                // Commander damage badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (commanderWarning) WarningRed.copy(alpha = 0.25f)
                            else theme.primary.copy(alpha = 0.2f)
                        )
                        .clickable(onClick = onCommanderDamageTap)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚔ $totalCommanderDamage",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (commanderWarning) WarningRed else theme.primary
                    )
                }
            }

            // Life total + adjust buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LifeAdjustButton(
                    label = "−",
                    delta = -1,
                    onAdjust = onLifeAdjust,
                    buttonColor = theme.primary,
                    contentColor = theme.onSurface,
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                )

                AnimatedContent(
                    targetState = player.lifeTotal,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "life_${player.id}",
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                        .clickable(onClick = onLifeTap)
                ) { life ->
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "$life",
                            style = MaterialTheme.typography.displayLarge,
                            color = theme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                LifeAdjustButton(
                    label = "+",
                    delta = 1,
                    onAdjust = onLifeAdjust,
                    buttonColor = theme.primary,
                    contentColor = theme.onSurface,
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                )
            }

            // Bottom row: poison
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "☣",
                        fontSize = 12.sp,
                        color = OnSurfaceMuted
                    )
                    Text(
                        text = "−",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceMuted,
                        modifier = Modifier.clickable { onPoisonAdjust(-1) }
                    )
                    Text(
                        text = "${player.poisonCounters}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (player.poisonCounters >= 8) WarningRed else OnSurfaceMuted
                    )
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceMuted,
                        modifier = Modifier.clickable { onPoisonAdjust(1) }
                    )
                }
            }
        }

        // Death overlay
        if (player.isEliminated) {
            DeathOverlay(reason = player.eliminationReason)
        }
    }
}
