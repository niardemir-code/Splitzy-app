package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.SubscriptionEntity
import com.example.data.model.IconLibrary
import com.example.data.model.PlatformPresets
import java.io.File

@Composable
fun PlatformIconBadge(
    platformName: String,
    modifier: Modifier = Modifier,
    iconType: String = "PRESET",
    iconKey: String = "Netflix",
    customImageUri: String = "",
    iconColorHex: String = "#6366F1",
    size: Dp = 48.dp,
    iconSize: Dp = 26.dp,
    cornerRadius: Dp = 14.dp
) {
    val context = LocalContext.current
    val parsedColor = remember(iconColorHex) {
        try {
            Color(android.graphics.Color.parseColor(iconColorHex))
        } catch (e: Exception) {
            Color(0xFF6366F1)
        }
    }

    val shape = RoundedCornerShape(cornerRadius)

    val preset = remember(platformName, iconKey) {
        val byKey = if (iconKey.isNotBlank() && !iconKey.equals("subscriptions", ignoreCase = true) && !iconKey.equals("Otra Plataforma", ignoreCase = true)) {
            PlatformPresets.getPreset(iconKey)
        } else null
        val byName = if (platformName.isNotBlank()) PlatformPresets.getPreset(platformName) else null

        when {
            byName != null && byName.name != "Otra Plataforma" -> byName
            byKey != null && byKey.name != "Otra Plataforma" -> byKey
            else -> byName ?: byKey ?: PlatformPresets.getPreset(platformName.ifBlank { "subscriptions" })
        }
    }

    val isFileValid = remember(customImageUri) {
        when {
            customImageUri.isBlank() -> false
            customImageUri.startsWith("data:image") -> true
            customImageUri.startsWith("http://") || customImageUri.startsWith("https://") -> true
            customImageUri.startsWith("content://") -> true
            customImageUri.startsWith("file://") -> {
                val path = customImageUri.removePrefix("file://")
                File(path).exists()
            }
            customImageUri.startsWith("/") -> File(customImageUri).exists()
            else -> customImageUri.length > 200 // Probable base64 string
        }
    }

    val isCustomImage = (iconType.equals("CUSTOM_IMAGE", ignoreCase = true) || customImageUri.isNotBlank()) && isFileValid
    val isExplicitVector = iconType.equals("VECTOR", ignoreCase = true) && !iconKey.equals("subscriptions", ignoreCase = true) && !iconKey.equals("Otra Plataforma", ignoreCase = true)

    when {
        // 1. Imagen personalizada válida (galería, URL remota, Archivo local o Base64 Data URI)
        isCustomImage -> {
            val isCustomColor = iconColorHex.isNotBlank() && !iconColorHex.equals("#6366F1", ignoreCase = true)
            val bgGradientColors = if (isCustomColor) {
                listOf(parsedColor.copy(alpha = 0.4f), parsedColor.copy(alpha = 0.15f))
            } else {
                listOf(preset.primaryColor.copy(alpha = 0.4f), preset.accentColor.copy(alpha = 0.15f))
            }

            Box(
                modifier = modifier
                    .size(size)
                    .clip(shape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = bgGradientColors
                        )
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), shape),
                contentAlignment = Alignment.Center
            ) {
                // Fallback brand icon under the image if image is loading or has transparency
                Icon(
                    imageVector = preset.icon,
                    contentDescription = null,
                    tint = preset.primaryColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(iconSize)
                )

                val imageModel = remember(customImageUri) {
                    when {
                        customImageUri.startsWith("content://") -> Uri.parse(customImageUri)
                        customImageUri.startsWith("file://") -> {
                            val path = customImageUri.removePrefix("file://")
                            File(path)
                        }
                        customImageUri.startsWith("/") -> File(customImageUri)
                        customImageUri.startsWith("data:image") -> customImageUri
                        customImageUri.startsWith("http://") || customImageUri.startsWith("https://") -> customImageUri
                        else -> {
                            // Base64 string pura
                            if (customImageUri.length > 200) {
                                "data:image/jpeg;base64,$customImageUri"
                            } else {
                                customImageUri
                            }
                        }
                    }
                }

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = platformName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 2. Icono vectorial explícito de la librería
        isExplicitVector -> {
            val vectorIcon = remember(iconKey) { IconLibrary.getIconByKey(iconKey) }
            Box(
                modifier = modifier
                    .size(size)
                    .clip(shape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                parsedColor,
                                parsedColor.copy(alpha = 0.75f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = vectorIcon,
                    contentDescription = platformName,
                    tint = Color.White,
                    modifier = Modifier.size(iconSize)
                )
            }
        }

        // 3. Preset de Plataforma (Crunchyroll, Netflix, Disney+, Spotify, etc.)
        else -> {
            val isCustomColor = iconColorHex.isNotBlank() && !iconColorHex.equals("#6366F1", ignoreCase = true)
            val bgGradientColors = if (isCustomColor) {
                listOf(parsedColor, parsedColor.copy(alpha = 0.75f))
            } else {
                listOf(preset.primaryColor, preset.accentColor)
            }

            Box(
                modifier = modifier
                    .size(size)
                    .clip(shape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = bgGradientColors
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = preset.icon,
                    contentDescription = platformName,
                    tint = Color.White,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

@Composable
fun PlatformIconBadge(
    subscription: SubscriptionEntity,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 26.dp,
    cornerRadius: Dp = 14.dp
) {
    PlatformIconBadge(
        platformName = subscription.platformName,
        iconType = subscription.iconType,
        iconKey = subscription.iconKey,
        customImageUri = subscription.customImageUri,
        iconColorHex = subscription.iconColorHex,
        modifier = modifier,
        size = size,
        iconSize = iconSize,
        cornerRadius = cornerRadius
    )
}
