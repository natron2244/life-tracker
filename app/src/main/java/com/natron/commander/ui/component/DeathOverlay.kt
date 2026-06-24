package com.natron.commander.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.natron.commander.model.EliminationReason
import com.natron.commander.ui.theme.OnSurface
import com.natron.commander.ui.theme.WarningRed

@Composable
fun DeathOverlay(reason: EliminationReason?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "☠",
                fontSize = 48.sp,
                color = WarningRed
            )
            Text(
                text = "ELIMINATED",
                style = MaterialTheme.typography.titleLarge,
                color = WarningRed,
                textAlign = TextAlign.Center
            )
            val reasonText = when (reason) {
                EliminationReason.LIFE -> "Life total reached 0"
                EliminationReason.POISON -> "10 poison counters"
                EliminationReason.COMMANDER -> "21 commander damage"
                null -> ""
            }
            if (reasonText.isNotEmpty()) {
                Text(
                    text = reasonText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
