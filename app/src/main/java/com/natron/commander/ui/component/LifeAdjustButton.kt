package com.natron.commander.ui.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.natron.commander.model.PlayerColor
import com.natron.commander.ui.theme.CommanderTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LifeAdjustButton(
    label: String,
    delta: Int,
    onAdjust: (Int) -> Unit,
    buttonColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(500L)
            var tick = 0
            while (isPressed) {
                val step = if (tick > 20) 5 else 1
                onAdjust(delta * step)
                delay(100L)
                tick++
            }
        }
    }

    Surface(
        modifier = modifier
            .pointerInput(delta) {
                detectTapGestures(
                    onTap = { onAdjust(delta) },
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        shape = RoundedCornerShape(8.dp),
        color = buttonColor.copy(alpha = 0.25f),
        contentColor = contentColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.displaySmall,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 80, heightDp = 80)
@Composable
private fun LifeAdjustButtonDecreasePreview() {
    CommanderTheme {
        LifeAdjustButton(
            label = "−",
            delta = -1,
            onAdjust = {},
            buttonColor = PlayerColor.ISLAND.primary,
            contentColor = PlayerColor.ISLAND.onSurface
        )
    }
}

@Preview(showBackground = true, widthDp = 80, heightDp = 80)
@Composable
private fun LifeAdjustButtonIncreasePreview() {
    CommanderTheme {
        LifeAdjustButton(
            label = "+",
            delta = 1,
            onAdjust = {},
            buttonColor = PlayerColor.ISLAND.primary,
            contentColor = PlayerColor.ISLAND.onSurface
        )
    }
}
