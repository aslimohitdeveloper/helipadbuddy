package com.mskdevelopers.helipadbuddy.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.R

private const val WHATSAPP_URI = "https://wa.me/918930253964"

@Composable
fun DeveloperInfoCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val developerName = stringResource(R.string.developer_name)
    val whatsappLabel = stringResource(R.string.developer_whatsapp_label)
    val whatsappNumber = stringResource(R.string.developer_whatsapp_number)

    AviationInstrumentCard(
        style = InstrumentStyle.NEUTRAL,
        modifier = modifier,
        contentPadding = 12.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Developer",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
            Text(
                developerName,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
            Text(
                whatsappLabel,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
            Text(
                whatsappNumber,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(WHATSAPP_URI))
                        context.startActivity(intent)
                    }
            )
        }
    }
}
