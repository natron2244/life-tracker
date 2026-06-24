package com.natron.commander.model

import androidx.compose.ui.graphics.Color

data class Player(
    val id: Int,
    val name: String,
    val colorTheme: PlayerColor,
    val lifeTotal: Int = 40,
    val poisonCounters: Int = 0,
    val commanderDamage: Map<Int, Int> = emptyMap(),
    val isEliminated: Boolean = false,
    val eliminationReason: EliminationReason? = null
)

enum class EliminationReason { LIFE, POISON, COMMANDER }

enum class PlayerColor(
    val displayName: String,
    val primary: Color,
    val surface: Color,
    val onSurface: Color
) {
    PLAINS(
        "Plains",
        Color(0xFFF9F6DC),
        Color(0xFF3A3520),
        Color(0xFFE8E2C0)
    ),
    ISLAND(
        "Island",
        Color(0xFF5B8FD8),
        Color(0xFF0E1E38),
        Color(0xFFB8CFF0)
    ),
    SWAMP(
        "Swamp",
        Color(0xFFB08BC8),
        Color(0xFF1E1228),
        Color(0xFFD4BEE8)
    ),
    MOUNTAIN(
        "Mountain",
        Color(0xFFE05A45),
        Color(0xFF2E100A),
        Color(0xFFF4B8AE)
    ),
    FOREST(
        "Forest",
        Color(0xFF4AAD6A),
        Color(0xFF0C2415),
        Color(0xFFAAD8B8)
    ),
    GOLD(
        "Gold",
        Color(0xFFC4A84F),
        Color(0xFF26200A),
        Color(0xFFE8D898)
    )
}
