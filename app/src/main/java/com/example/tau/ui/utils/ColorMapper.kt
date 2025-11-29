package com.example.tau.ui.utils

import androidx.compose.ui.graphics.Color
import java.util.Locale

object ColorMapper {
    private val colorMap = mapOf(
        "Rosa" to "#FF1493",
        "Vermelho" to "#FF0000",
        "Laranja" to "#FF8C00",
        "Amarelo" to "#FFFF00",
        "Verde" to "#32CD32",
        "Menta" to "#00FF00",
        "Azul" to "#4169E1",
        "Ciano" to "#00CED1",
        "Roxo" to "#9932CC",
        "Lavanda" to "#BA55D3",
        "Salmão" to "#FF6347",
        "Pêssego" to "#FF7F50"
    )

    fun colorNameToHex(colorName: String): String {
        val normalized = colorName.trim().replace("#", "")
        val directHex = if (normalized.length == 6 || normalized.length == 8) "#$normalized" else null
        if (directHex != null && directHex.matches(Regex("^#[0-9A-Fa-f]{6,8}$"))) return directHex
        val capitalized = colorName.trim().replaceFirstChar { it.uppercase(Locale.ROOT) }
        return colorMap[colorName] ?: colorMap[capitalized] ?: "#000000"
    }

    fun colorNameToColor(colorName: String): Color {
        return try {
            val hex = colorNameToHex(colorName)
            Color(android.graphics.Color.parseColor(hex))
        } catch (_: Exception) {
            Color(0xFF000000)
        }
    }
}
