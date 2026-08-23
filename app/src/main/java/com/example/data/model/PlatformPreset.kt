package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class PlatformPreset(
    val name: String,
    val defaultPlan: String,
    val defaultCost: Double,
    val defaultSplitSuggested: Double,
    val maxMembersSuggested: Int,
    val primaryColor: Color,
    val accentColor: Color,
    val category: String,
    val icon: ImageVector
)

object PlatformPresets {
    val list = listOf(
        PlatformPreset(
            name = "Crunchyroll",
            defaultPlan = "Mega Fan (4 dispositivos)",
            defaultCost = 6.49,
            defaultSplitSuggested = 1.62,
            maxMembersSuggested = 4,
            primaryColor = Color(0xFFF47521),
            accentColor = Color(0xFFFF9045),
            category = "Streaming",
            icon = Icons.Default.Movie
        ),
        PlatformPreset(
            name = "Netflix",
            defaultPlan = "Plan Premium 4K (4 pantallas)",
            defaultCost = 17.99,
            defaultSplitSuggested = 4.50,
            maxMembersSuggested = 4,
            primaryColor = Color(0xFFE50914),
            accentColor = Color(0xFFFF4B55),
            category = "Streaming",
            icon = Icons.Default.Movie
        ),
        PlatformPreset(
            name = "Spotify",
            defaultPlan = "Plan Familiar (6 cuentas)",
            defaultCost = 17.99,
            defaultSplitSuggested = 3.00,
            maxMembersSuggested = 5,
            primaryColor = Color(0xFF1DB954),
            accentColor = Color(0xFF1ED760),
            category = "Música",
            icon = Icons.Default.Headphones
        ),
        PlatformPreset(
            name = "Disney+",
            defaultPlan = "Plan Premium 4K",
            defaultCost = 11.99,
            defaultSplitSuggested = 3.00,
            maxMembersSuggested = 4,
            primaryColor = Color(0xFF113CCF),
            accentColor = Color(0xFF3861FB),
            category = "Streaming",
            icon = Icons.Default.Tv
        ),
        PlatformPreset(
            name = "Max (HBO)",
            defaultPlan = "Plan Platino 4K",
            defaultCost = 13.99,
            defaultSplitSuggested = 3.50,
            maxMembersSuggested = 4,
            primaryColor = Color(0xFF5822B4),
            accentColor = Color(0xFF7E42E6),
            category = "Streaming",
            icon = Icons.Default.Movie
        ),
        PlatformPreset(
            name = "YouTube Premium",
            defaultPlan = "Plan Familiar (5 miembros)",
            defaultCost = 17.99,
            defaultSplitSuggested = 3.60,
            maxMembersSuggested = 5,
            primaryColor = Color(0xFFFF0000),
            accentColor = Color(0xFFFF4D4D),
            category = "Streaming",
            icon = Icons.Default.Videocam
        ),
        PlatformPreset(
            name = "ChatGPT Plus / Team",
            defaultPlan = "Suscripción Team / Compartida",
            defaultCost = 25.00,
            defaultSplitSuggested = 12.50,
            maxMembersSuggested = 2,
            primaryColor = Color(0xFF10A37F),
            accentColor = Color(0xFF1FAF88),
            category = "Productividad",
            icon = Icons.Default.Chat
        ),
        PlatformPreset(
            name = "Prime Video / Amazon",
            defaultPlan = "Amazon Prime Anual / Mensual",
            defaultCost = 4.99,
            defaultSplitSuggested = 2.50,
            maxMembersSuggested = 2,
            primaryColor = Color(0xFF00A8E1),
            accentColor = Color(0xFF33C2F3),
            category = "Streaming",
            icon = Icons.Default.ShoppingBag
        ),
        PlatformPreset(
            name = "Apple One",
            defaultPlan = "Apple One Familiar",
            defaultCost = 25.95,
            defaultSplitSuggested = 5.20,
            maxMembersSuggested = 5,
            primaryColor = Color(0xFFFB233B),
            accentColor = Color(0xFFFF5266),
            category = "Música / Servicios",
            icon = Icons.Default.Widgets
        ),
        PlatformPreset(
            name = "Xbox Game Pass",
            defaultPlan = "Ultimate",
            defaultCost = 14.99,
            defaultSplitSuggested = 7.50,
            maxMembersSuggested = 2,
            primaryColor = Color(0xFF107C10),
            accentColor = Color(0xFF139813),
            category = "Gaming",
            icon = Icons.Default.Games
        ),
        PlatformPreset(
            name = "Nintendo Switch Online",
            defaultPlan = "Suscripción Familiar + Paquete",
            defaultCost = 5.83, // 69.99/12
            defaultSplitSuggested = 1.00,
            maxMembersSuggested = 7,
            primaryColor = Color(0xFFE60012),
            accentColor = Color(0xFFFF3344),
            category = "Gaming",
            icon = Icons.Default.Games
        ),
        PlatformPreset(
            name = "DAZN",
            defaultPlan = "DAZN Total / Esencial",
            defaultCost = 29.99,
            defaultSplitSuggested = 14.99,
            maxMembersSuggested = 2,
            primaryColor = Color(0xFF000000),
            accentColor = Color(0xFFF8FF00),
            category = "Salud",
            icon = Icons.Default.Movie
        ),
        PlatformPreset(
            name = "Movistar+",
            defaultPlan = "Movistar Plus+ Susp.",
            defaultCost = 14.00,
            defaultSplitSuggested = 7.00,
            maxMembersSuggested = 2,
            primaryColor = Color(0xFF00A9E0),
            accentColor = Color(0xFF005C8A),
            category = "Streaming",
            icon = Icons.Default.Tv
        ),
        PlatformPreset(
            name = "Filmin",
            defaultPlan = "Suscripción Anual / Mensual",
            defaultCost = 9.99,
            defaultSplitSuggested = 4.99,
            maxMembersSuggested = 2,
            primaryColor = Color(0xFF00E676),
            accentColor = Color(0xFF1DE9B6),
            category = "Streaming",
            icon = Icons.Default.Movie
        ),
        PlatformPreset(
            name = "SkyShowtime",
            defaultPlan = "Plan Estándar Plus",
            defaultCost = 7.99,
            defaultSplitSuggested = 3.99,
            maxMembersSuggested = 2,
            primaryColor = Color(0xFF002244),
            accentColor = Color(0xFF0055AA),
            category = "Streaming",
            icon = Icons.Default.Tv
        ),
        PlatformPreset(
            name = "Duolingo Super",
            defaultPlan = "Plan Familiar (6 cuentas)",
            defaultCost = 10.25, // 122.99/12
            defaultSplitSuggested = 2.00,
            maxMembersSuggested = 5,
            primaryColor = Color(0xFF58CC02),
            accentColor = Color(0xFF76E817),
            category = "Educación",
            icon = Icons.Default.School
        ),
        PlatformPreset(
            name = "Canva Pro",
            defaultPlan = "Canva Equipos",
            defaultCost = 14.00,
            defaultSplitSuggested = 3.50,
            maxMembersSuggested = 4,
            primaryColor = Color(0xFF00C4CC),
            accentColor = Color(0xFF2CE5ED),
            category = "Diseño",
            icon = Icons.Default.Palette
        ),
        PlatformPreset(
            name = "Gimnasio / Fitness",
            defaultPlan = "Pase Duo / Familiar",
            defaultCost = 45.00,
            defaultSplitSuggested = 22.50,
            maxMembersSuggested = 2,
            primaryColor = Color(0xFFF97316),
            accentColor = Color(0xFFFB923C),
            category = "Salud",
            icon = Icons.Default.FitnessCenter
        ),
        PlatformPreset(
            name = "Otra Plataforma",
            defaultPlan = "Suscripción Compartida",
            defaultCost = 10.00,
            defaultSplitSuggested = 5.00,
            maxMembersSuggested = 2,
            primaryColor = Color(0xFF6366F1),
            accentColor = Color(0xFF818CF8),
            category = "General",
            icon = Icons.Default.Subscriptions
        )
    )

    fun getPreset(name: String): PlatformPreset {
        val cleanName = name.trim().lowercase()
        if (cleanName.isBlank()) return list.last()

        // 1. Coincidencia exacta
        list.find { it.name.equals(name, ignoreCase = true) }?.let { return it }

        // 2. Coincidencia por palabras clave conocidas
        if (cleanName.contains("crunchyroll") || cleanName.contains("crunchy")) {
            list.find { it.name.equals("Crunchyroll", ignoreCase = true) }?.let { return it }
        }
        if (cleanName.contains("netflix")) {
            list.find { it.name.equals("Netflix", ignoreCase = true) }?.let { return it }
        }
        if (cleanName.contains("spotify")) {
            list.find { it.name.equals("Spotify", ignoreCase = true) }?.let { return it }
        }
        if (cleanName.contains("disney")) {
            list.find { it.name.equals("Disney+", ignoreCase = true) }?.let { return it }
        }
        if (cleanName.contains("hbo") || cleanName.contains("max")) {
            list.find { it.name.contains("Max", ignoreCase = true) || it.name.contains("HBO", ignoreCase = true) }?.let { return it }
        }
        if (cleanName.contains("youtube")) {
            list.find { it.name.equals("YouTube Premium", ignoreCase = true) }?.let { return it }
        }
        if (cleanName.contains("prime") || cleanName.contains("amazon")) {
            list.find { it.name.contains("Prime", ignoreCase = true) }?.let { return it }
        }
        if (cleanName.contains("apple")) {
            list.find { it.name.contains("Apple", ignoreCase = true) }?.let { return it }
        }
        if (cleanName.contains("xbox") || cleanName.contains("gamepass") || cleanName.contains("game pass")) {
            list.find { it.name.contains("Xbox", ignoreCase = true) }?.let { return it }
        }
        if (cleanName.contains("nintendo") || cleanName.contains("switch")) {
            list.find { it.name.contains("Nintendo", ignoreCase = true) }?.let { return it }
        }
        if (cleanName.contains("duolingo")) {
            list.find { it.name.contains("Duolingo", ignoreCase = true) }?.let { return it }
        }
        if (cleanName.contains("chatgpt") || cleanName.contains("openai") || cleanName.contains("gpt")) {
            list.find { it.name.contains("ChatGPT", ignoreCase = true) }?.let { return it }
        }
        if (cleanName.contains("dazn")) {
            list.find { it.name.contains("DAZN", ignoreCase = true) }?.let { return it }
        }
        if (cleanName.contains("movistar")) {
            list.find { it.name.contains("Movistar", ignoreCase = true) }?.let { return it }
        }
        if (cleanName.contains("filmin")) {
            list.find { it.name.contains("Filmin", ignoreCase = true) }?.let { return it }
        }
        if (cleanName.contains("skyshowtime") || cleanName.contains("showtime")) {
            list.find { it.name.contains("SkyShowtime", ignoreCase = true) }?.let { return it }
        }
        if (cleanName.contains("canva")) {
            list.find { it.name.contains("Canva", ignoreCase = true) }?.let { return it }
        }

        // 3. Coincidencia parcial con cualquier elemento de la lista (excepto el genérico final)
        for (preset in list.dropLast(1)) {
            val pName = preset.name.lowercase()
            if (cleanName.contains(pName) || pName.contains(cleanName)) {
                return preset
            }
        }

        return list.last()
    }
}
