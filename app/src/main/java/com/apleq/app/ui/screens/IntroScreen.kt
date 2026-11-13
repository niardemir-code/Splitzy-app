package com.apleq.app.ui.screens

import android.net.Uri
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.apleq.app.ui.components.SplitzyLogo
import com.apleq.app.ui.theme.Quicksand
import com.apleq.app.ui.util.I18n
import kotlinx.coroutines.launch

private data class IntroPage(
    /** Nombre del vídeo en res/raw (sin extensión). */
    val videoName: String,
    /** Icono que se muestra si el vídeo todavía no existe. */
    val icon: ImageVector,
    val title: String,
    val text: String
)

/** Vídeo en bucle y sin sonido. Solo se reproduce cuando su diapositiva es la visible. */
@Composable
private fun IntroVideo(
    videoRes: Int,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val frame = FrameLayout(ctx)
            val video = VideoView(ctx).apply {
                setVideoURI(Uri.parse("android.resource://${ctx.packageName}/$videoRes"))
                setOnPreparedListener { player ->
                    player.isLooping = true
                    player.setVolume(0f, 0f)
                }
            }
            // El VideoView mantiene la proporción del vídeo; el FrameLayout lo centra.
            frame.addView(
                video,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
                )
            )
            frame
        },
        update = { frame ->
            val video = frame.getChildAt(0) as VideoView
            if (isActive) {
                if (!video.isPlaying) video.start()
            } else {
                if (video.isPlaying) video.pause()
                video.seekTo(0)
            }
        },
        onRelease = { frame ->
            (frame.getChildAt(0) as? VideoView)?.stopPlayback()
        }
    )
}

/** Presentación de la app, antes de la pantalla de acceso. Se muestra una sola vez. */
@Composable
fun IntroScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pages = listOf(
        IntroPage("intro_1", Icons.Default.Savings, I18n.introTitle1, I18n.introText1),
        IntroPage("intro_2", Icons.Default.CalendarMonth, I18n.introTitle2, I18n.introText2),
        IntroPage("intro_3", Icons.Default.Notifications, I18n.introTitle3, I18n.introText3),
        IntroPage("intro_4", Icons.Default.GroupAdd, I18n.introTitle4, I18n.introText4)
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.size - 1
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SplitzyLogo(size = 32.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = I18n.appName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = Quicksand,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                if (!isLast) {
                    TextButton(onClick = onFinish) { Text(I18n.introSkip) }
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { index ->
                val page = pages[index]
                // 0 si el vídeo todavía no está en res/raw.
                val videoRes = remember(page.videoName) {
                    context.resources.getIdentifier(page.videoName, "raw", context.packageName)
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Zona del vídeo: ocupa el espacio sobrante.
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (videoRes != 0) {
                            IntroVideo(
                                videoRes = videoRes,
                                isActive = pagerState.currentPage == index,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(140.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = page.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(64.dp)
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = page.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { i ->
                    val selected = i == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (selected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }

            Button(
                onClick = {
                    if (isLast) {
                        onFinish()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(if (isLast) I18n.introStart else I18n.introNext)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
