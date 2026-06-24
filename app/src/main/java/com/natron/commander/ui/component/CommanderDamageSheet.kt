package com.natron.commander.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.natron.commander.model.Player
import com.natron.commander.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommanderDamageSheet(
    targetPlayer: Player,
    allPlayers: List<Player>,
    onAdjust: (sourcePlayerId: Int, delta: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val opponents = allPlayers.filter { it.id != targetPlayer.id }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Commander Damage",
                style = MaterialTheme.typography.titleLarge,
                color = MythicGold
            )
            Text(
                "Received by: ${targetPlayer.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceMuted
            )

            HorizontalDivider(color = OnSurfaceMuted.copy(alpha = 0.2f))

            opponents.forEach { source ->
                val damage = targetPlayer.commanderDamage[source.id] ?: 0
                val isWarning = damage >= 15
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .let {
                                    it // color indicator would go here if needed
                                }
                        )
                        Text(
                            "From ${source.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isWarning) WarningRed else OnSurface
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(onClick = { onAdjust(source.id, -1) }) {
                            Text("−", style = MaterialTheme.typography.titleLarge, color = OnSurface)
                        }
                        Text(
                            "$damage",
                            style = MaterialTheme.typography.displaySmall,
                            color = if (isWarning) WarningRed else MythicGold,
                            modifier = Modifier.widthIn(min = 40.dp)
                        )
                        IconButton(onClick = { onAdjust(source.id, 1) }) {
                            Text("+", style = MaterialTheme.typography.titleLarge, color = OnSurface)
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MythicGold,
                    contentColor = CardBack
                )
            ) {
                Text("Done")
            }
        }
    }
}
