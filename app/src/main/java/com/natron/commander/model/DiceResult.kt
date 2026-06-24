package com.natron.commander.model

enum class Die(val faces: Int, val label: String) {
    D4(4, "d4"),
    D6(6, "d6"),
    D8(8, "d8"),
    D10(10, "d10"),
    D12(12, "d12"),
    D20(20, "d20"),
    D100(100, "d100")
}

data class DiceResult(val die: Die, val value: Int)
