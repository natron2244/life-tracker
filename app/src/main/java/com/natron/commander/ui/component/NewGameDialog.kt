package com.natron.commander.ui.component

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.natron.commander.ui.theme.MythicGold
import com.natron.commander.ui.theme.OnSurface
import com.natron.commander.ui.theme.SurfaceVariant
import com.natron.commander.ui.theme.WarningRed

@Composable
fun NewGameDialog(
    onReset: () -> Unit,
    onNewGame: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Game Options", color = OnSurface) },
        text = { Text("Reset keeps current players. New game returns to setup.", color = OnSurface.copy(alpha = 0.7f)) },
        confirmButton = {
            TextButton(onClick = onReset) {
                Text("Reset", color = MythicGold)
            }
        },
        dismissButton = {
            TextButton(onClick = onNewGame) {
                Text("New game", color = WarningRed)
            }
        },
        containerColor = SurfaceVariant,
        titleContentColor = OnSurface
    )
}
