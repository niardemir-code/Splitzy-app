package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CustomIconOption(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val category: String
)

data class ColorOption(
    val hex: String,
    val color: Color,
    val label: String
)

object IconLibrary {
    val availableColors = listOf(
        ColorOption("#6366F1", Color(0xFF6366F1), "Índigo"),
        ColorOption("#4F46E5", Color(0xFF4F46E5), "Violeta"),
        ColorOption("#E50914", Color(0xFFE50914), "Rojo"),
        ColorOption("#1DB954", Color(0xFF1DB954), "Verde"),
        ColorOption("#113CCF", Color(0xFF113CCF), "Azul"),
        ColorOption("#00A8E1", Color(0xFF00A8E1), "Cian"),
        ColorOption("#10A37F", Color(0xFF10A37F), "Esmeralda"),
        ColorOption("#F59E0B", Color(0xFFF59E0B), "Ámbar"),
        ColorOption("#EC4899", Color(0xFFEC4899), "Rosa"),
        ColorOption("#8B5CF6", Color(0xFF8B5CF6), "Púrpura"),
        ColorOption("#06B6D4", Color(0xFF06B6D4), "Turquesa"),
        ColorOption("#F97316", Color(0xFFF97316), "Naranja"),
        ColorOption("#64748B", Color(0xFF64748B), "Pizarra"),
        ColorOption("#1E293B", Color(0xFF1E293B), "Oscuro")
    )

    val availableIcons = listOf(
        CustomIconOption("movie", "Películas", Icons.Default.Movie, "Streaming"),
        CustomIconOption("tv", "Series / TV", Icons.Default.Tv, "Streaming"),
        CustomIconOption("videocam", "Video", Icons.Default.Videocam, "Streaming"),
        CustomIconOption("headphones", "Música", Icons.Default.Headphones, "Música"),
        CustomIconOption("mic", "Podcast / Audio", Icons.Default.Mic, "Música"),
        CustomIconOption("radio", "Radio", Icons.Default.Radio, "Música"),
        CustomIconOption("chat", "Chat / IA", Icons.Default.Chat, "Productividad"),
        CustomIconOption("auto_awesome", "IA / Gemini", Icons.Default.AutoAwesome, "Productividad"),
        CustomIconOption("code", "Desarrollo", Icons.Default.Code, "Productividad"),
        CustomIconOption("cloud", "Nube / Almacenamiento", Icons.Default.Cloud, "Productividad"),
        CustomIconOption("work", "Trabajo", Icons.Default.Work, "Productividad"),
        CustomIconOption("laptop", "Software", Icons.Default.Laptop, "Productividad"),
        CustomIconOption("games", "Gaming", Icons.Default.Games, "Gaming"),
        CustomIconOption("sports_esports", "Consola", Icons.Default.SportsEsports, "Gaming"),
        CustomIconOption("school", "Educación", Icons.Default.School, "Educación"),
        CustomIconOption("book", "Libros / Lectura", Icons.Default.Book, "Educación"),
        CustomIconOption("fitness", "Gimnasio / Deporte", Icons.Default.FitnessCenter, "Salud"),
        CustomIconOption("soccer", "Deportes", Icons.Default.SportsSoccer, "Salud"),
        CustomIconOption("shopping", "Compras / Tienda", Icons.Default.ShoppingBag, "Estilo de vida"),
        CustomIconOption("palette", "Diseño", Icons.Default.Palette, "Estilo de vida"),
        CustomIconOption("newspaper", "Noticias / Prensa", Icons.Default.Newspaper, "Estilo de vida"),
        CustomIconOption("lock", "Seguridad / VPN", Icons.Default.Lock, "Seguridad"),
        CustomIconOption("key", "Contraseñas / Vault", Icons.Default.VpnKey, "Seguridad"),
        CustomIconOption("euro", "Finanzas / Banco", Icons.Default.Euro, "Finanzas"),
        CustomIconOption("star", "Favorito / Premium", Icons.Default.Star, "General"),
        CustomIconOption("heart", "Salud / Bienestar", Icons.Default.Favorite, "General"),
        CustomIconOption("subscriptions", "Suscripción", Icons.Default.Subscriptions, "General"),
        CustomIconOption("widgets", "Servicios", Icons.Default.Widgets, "General")
    )

    fun getIconByKey(key: String): ImageVector {
        return availableIcons.find { it.key.equals(key, ignoreCase = true) }?.icon
            ?: PlatformPresets.list.find { it.name.equals(key, ignoreCase = true) }?.icon
            ?: Icons.Default.Subscriptions
    }
}
