package com.natron.commander.ui.component

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.natron.commander.ui.theme.CommanderTheme
import androidx.compose.ui.text.input.KeyboardType
import com.natron.commander.ui.theme.CardBack
import com.natron.commander.ui.theme.MythicGold
import com.natron.commander.ui.theme.OnSurface
import com.natron.commander.ui.theme.SurfaceVariant

@Composable
fun DirectInputDialog(
    currentLife: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(currentLife.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Life Total", color = OnSurface) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter { c -> c.isDigit() || c == '-' } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MythicGold,
                    unfocusedBorderColor = OnSurface.copy(alpha = 0.3f),
                    focusedTextColor = OnSurface,
                    unfocusedTextColor = OnSurface,
                    cursorColor = MythicGold
                )
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val value = text.toIntOrNull() ?: currentLife
                onConfirm(value)
            }) {
                Text("Set life", color = MythicGold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = OnSurface.copy(alpha = 0.6f))
            }
        },
        containerColor = SurfaceVariant,
        titleContentColor = OnSurface
    )
}

@Preview(showBackground = true)
@Composable
private fun DirectInputDialogPreview() {
    CommanderTheme {
        DirectInputDialog(currentLife = 28, onConfirm = {}, onDismiss = {})
    }
}
