package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun ResponsiveText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontWeight: FontWeight = FontWeight.Normal,
    digitCountOverride: Int? = null
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val digits = digitCountOverride ?: text.count { it.isDigit() }.coerceAtLeast(1)
        val baseSize = responsiveFontSize(digits)
        val fontSize = if (maxWidth.value < 120f) {
            (baseSize.value * 0.85f).sp
        } else {
            baseSize
        }
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun responsiveFontSize(digitCount: Int): TextUnit = when {
    digitCount <= 2 -> 70.sp
    digitCount == 3 -> 62.sp
    digitCount == 4 -> 54.sp
    else -> 46.sp
}
