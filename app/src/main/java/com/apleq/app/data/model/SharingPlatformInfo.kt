package com.apleq.app.data.model

import androidx.compose.ui.graphics.Color
import com.apleq.app.data.local.SharingPlatformEntity

data class SharingPlatformInfo(
    val name: String,
    val baseColor: Color,
    val badgeBgColor: Color,
    val badgeTextColor: Color
)

object SharingPlatforms {

    val defaultList = listOf(
        SharingPlatformEntity(id = 1, name = "Together Price", colorHex = "#6D28D9", displayOrder = 0),
        SharingPlatformEntity(id = 2, name = "Sharingful", colorHex = "#DB2777", displayOrder = 1),
        SharingPlatformEntity(id = 3, name = "Spliiit", colorHex = "#059669", displayOrder = 2),
        SharingPlatformEntity(id = 4, name = "GamsGo", colorHex = "#D97706", displayOrder = 3),
        SharingPlatformEntity(id = 5, name = "Sharesub", colorHex = "#0284C7", displayOrder = 4),
        SharingPlatformEntity(id = 6, name = "Directo / Familia", colorHex = "#C2410C", displayOrder = 5)
    )

    fun isPlatformMatch(candidate: String, target: String): Boolean {
        val c = candidate.trim()
        val t = target.trim()
        if (c.equals(t, ignoreCase = true)) return true

        // Together Price / Price Together aliases
        val isCandidateTogetherPrice = c.equals("Together Price", ignoreCase = true) || c.equals("Price Together", ignoreCase = true)
        val isTargetTogetherPrice = t.equals("Together Price", ignoreCase = true) || t.equals("Price Together", ignoreCase = true)
        if (isCandidateTogetherPrice && isTargetTogetherPrice) return true

        // Directo / Familia aliases
        val isCandidateDirecto = c.startsWith("Directo", ignoreCase = true) || c.startsWith("Familia", ignoreCase = true)
        val isTargetDirecto = t.startsWith("Directo", ignoreCase = true) || t.startsWith("Familia", ignoreCase = true)
        if (isCandidateDirecto && isTargetDirecto) return true

        return false
    }

    fun parseColor(hex: String, defaultColor: Color = Color(0xFF6366F1)): Color {
        return try {
            val cleanHex = hex.trim().removePrefix("#")
            val colorLong = when (cleanHex.length) {
                6 -> ("FF$cleanHex").toLong(16)
                8 -> cleanHex.toLong(16)
                else -> return defaultColor
            }
            Color(colorLong)
        } catch (e: Exception) {
            defaultColor
        }
    }

    /**
     * Calculates high contrast text color for any background color.
     * Returns White for dark backgrounds, Black for light backgrounds.
     */
    fun getContrastTextColor(backgroundColor: Color): Color {
        // Standard perceived luminance: 0.299*R + 0.587*G + 0.114*B
        val luminance = 0.299f * backgroundColor.red + 0.587f * backgroundColor.green + 0.114f * backgroundColor.blue
        return if (luminance < 0.55f) Color.White else Color(0xFF111827)
    }

    fun fromEntity(entity: SharingPlatformEntity): SharingPlatformInfo {
        val baseColor = parseColor(entity.colorHex)
        val textColor = getContrastTextColor(baseColor)
        return SharingPlatformInfo(
            name = entity.name,
            baseColor = baseColor,
            badgeBgColor = baseColor,
            badgeTextColor = textColor
        )
    }

    fun getInfo(platform: String, customPlatforms: List<SharingPlatformEntity>? = null): SharingPlatformInfo {
        val trimmed = platform.trim()
        if (trimmed.isBlank()) {
            return SharingPlatformInfo("General", Color(0xFF64748B), Color(0xFF64748B), Color.White)
        }

        // 1. Check custom active platforms from database/configuration first
        if (!customPlatforms.isNullOrEmpty()) {
            val match = customPlatforms.find { isPlatformMatch(it.name, trimmed) }
            if (match != null) {
                val baseColor = parseColor(match.colorHex)
                val textColor = getContrastTextColor(baseColor)
                return SharingPlatformInfo(
                    name = trimmed,
                    baseColor = baseColor,
                    badgeBgColor = baseColor,
                    badgeTextColor = textColor
                )
            }
        }

        // 2. Check defaults
        val defaultMatch = defaultList.find { isPlatformMatch(it.name, trimmed) }
        if (defaultMatch != null) {
            val baseColor = parseColor(defaultMatch.colorHex)
            val textColor = getContrastTextColor(baseColor)
            return SharingPlatformInfo(
                name = trimmed,
                baseColor = baseColor,
                badgeBgColor = baseColor,
                badgeTextColor = textColor
            )
        }

        // 3. Fallback generic palette derived deterministically from name hash
        val colors = listOf(
            Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFEC4899),
            Color(0xFF059669), Color(0xFFD97706), Color(0xFF0284C7),
            Color(0xFF06B6D4), Color(0xFF84CC16), Color(0xFFDC2626)
        )
        val colorIndex = Math.abs(trimmed.hashCode()) % colors.size
        val baseColor = colors[colorIndex]
        val textColor = getContrastTextColor(baseColor)
        return SharingPlatformInfo(
            name = trimmed,
            baseColor = baseColor,
            badgeBgColor = baseColor,
            badgeTextColor = textColor
        )
    }

    val list: List<SharingPlatformInfo>
        get() = defaultList.map { fromEntity(it) }
}

