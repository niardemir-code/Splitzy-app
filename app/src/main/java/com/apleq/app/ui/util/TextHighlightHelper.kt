package com.apleq.app.ui.util

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

object TextHighlightHelper {

    // Pastel yellow highlight colors
    val PastelYellowBg = Color(0xFFFEF08A) // Tailwind Yellow 200 (Warm pastel yellow)
    val HighlightDarkText = Color(0xFF713F12) // Warm dark text for strong contrast on yellow

    fun buildHighlightedText(
        text: String,
        query: String,
        highlightBgColor: Color = PastelYellowBg,
        highlightTextColor: Color = HighlightDarkText
    ): AnnotatedString {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty() || !text.contains(trimmedQuery, ignoreCase = true)) {
            return AnnotatedString(text)
        }

        val builder = AnnotatedString.Builder()
        val lowerText = text.lowercase()
        val lowerQuery = trimmedQuery.lowercase()

        var startIndex = 0
        while (startIndex < text.length) {
            val matchIndex = lowerText.indexOf(lowerQuery, startIndex)
            if (matchIndex == -1) {
                builder.append(text.substring(startIndex))
                break
            }

            if (matchIndex > startIndex) {
                builder.append(text.substring(startIndex, matchIndex))
            }

            val endIndex = matchIndex + lowerQuery.length
            builder.pushStyle(
                SpanStyle(
                    background = highlightBgColor,
                    color = highlightTextColor,
                    fontWeight = FontWeight.Bold
                )
            )
            builder.append(text.substring(matchIndex, endIndex))
            builder.pop()

            startIndex = endIndex
        }

        return builder.toAnnotatedString()
    }
}

@Composable
fun HighlightedText(
    text: String,
    query: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    highlightBgColor: Color = TextHighlightHelper.PastelYellowBg,
    highlightTextColor: Color = TextHighlightHelper.HighlightDarkText,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null
) {
    val annotatedString = TextHighlightHelper.buildHighlightedText(
        text = text,
        query = query,
        highlightBgColor = highlightBgColor,
        highlightTextColor = highlightTextColor
    )

    Text(
        text = annotatedString,
        modifier = modifier,
        style = style,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
        textAlign = textAlign
    )
}
