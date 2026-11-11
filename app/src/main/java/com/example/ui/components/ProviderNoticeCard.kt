package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.ProviderStatus

@Composable
fun ProviderNoticeCard(
    providerStatus: ProviderStatus,
    language: AppLanguage,
    onConfigureClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLinked = providerStatus.isLinked

    val containerBg = if (isLinked) {
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
    } else {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
    }

    val borderColor = if (isLinked) {
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
    }

    val iconColor = if (isLinked) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.error
    }

    val title = if (isLinked) {
        when (language) {
            AppLanguage.AR -> "مزود Gemini متصل (${providerStatus.activeModel.displayName})"
            AppLanguage.FR -> "Gemini connecté (${providerStatus.activeModel.displayName})"
            AppLanguage.EN -> "Gemini Connected (${providerStatus.activeModel.displayName})"
        }
    } else {
        when (language) {
            AppLanguage.AR -> "مزود الذكاء الاصطناعي غير متصل"
            AppLanguage.FR -> "Fournisseur d'IA non associé"
            AppLanguage.EN -> "AI Provider Not Linked"
        }
    }

    val description = if (isLinked) {
        when (language) {
            AppLanguage.AR -> "الردود مباشرة وحقيقية من Google Gemini."
            AppLanguage.FR -> "Réponses authentiques en direct depuis Google Gemini."
            AppLanguage.EN -> "Real direct responses powered by Google Gemini."
        }
    } else {
        when (language) {
            AppLanguage.AR -> "تطبيق Dadix AI حقيقي ولا يولد ردوداً وهمية. يرجى ربط مفتاح Gemini API للبدء بالدردشة."
            AppLanguage.FR -> "Dadix AI n'invente pas de fausses réponses. Veuillez lier votre clé Gemini API pour discuter."
            AppLanguage.EN -> "Dadix AI is authentic and does not use fake data. Please link a Gemini API key to begin chatting."
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(containerBg)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onConfigureClicked() }
            .padding(14.dp)
            .testTag("provider_notice_card")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (isLinked) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Configure provider",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 17.sp
        )
    }
}
