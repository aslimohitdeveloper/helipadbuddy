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
fun ResponsiveValueText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontWeight: FontWeight = FontWeight.Bold,
    digitCountOverride: Int? = null
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val digits = digitCountOverride ?: text.count { it.isDigit() }.coerceAtLeast(1)
        val fontSize = valueFontSize(digits)
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

private fun valueFontSize(digitCount: Int): TextUnit = when {
    digitCount <= 2 -> 64.sp
    digitCount == 3 -> 58.sp
    digitCount == 4 -> 50.sp
    else -> 44.sp
}
