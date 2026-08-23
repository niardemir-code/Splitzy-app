package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

@Composable
fun SplitzyLogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val cornerRadius = size * 0.23f

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        val painter = runCatching { painterResource(id = R.drawable.ic_launcher_foreground) }
            .getOrNull()
            ?: runCatching { painterResource(id = R.drawable.ic_splitzy_app_logo) }
                .getOrNull()

        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = "Splitzy Logo",
                modifier = Modifier
                    .size(size)
                    .clip(RoundedCornerShape(cornerRadius)),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Splitzy Logo",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.6f)
                )
            }
        }
    }
}


