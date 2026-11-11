package com.example.ui.components

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApiKeySource
import com.example.data.model.AppLanguage
import com.example.data.model.ProviderStatus

@Composable
fun ApiKeyDialog(
    providerStatus: ProviderStatus,
    currentCustomKey: String,
    language: AppLanguage,
    onSaveKey: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var keyInput by remember { mutableStateOf(currentCustomKey) }
    var passwordVisible by remember { mutableStateOf(false) }

    val title = when (language) {
        AppLanguage.AR -> "إعداد مزود الذكاء الاصطناعي (Gemini)"
        AppLanguage.FR -> "Fournisseur d'IA (Gemini)"
        AppLanguage.EN -> "AI Provider Setup (Gemini)"
    }

    val hint = when (language) {
        AppLanguage.AR -> "أدخل مفتاح Google Gemini API هنا"
        AppLanguage.FR -> "Entrez la clé API Google Gemini"
        AppLanguage.EN -> "Enter Google Gemini API key"
    }

    val cancelText = when (language) {
        AppLanguage.AR -> "إلغاء"
        AppLanguage.FR -> "Annuler"
        AppLanguage.EN -> "Cancel"
    }

    val saveText = when (language) {
        AppLanguage.AR -> "حفظ المفتاح"
        AppLanguage.FR -> "Enregistrer"
        AppLanguage.EN -> "Save Key"
    }

    val clearText = when (language) {
        AppLanguage.AR -> "مسح المفتاح"
        AppLanguage.FR -> "Supprimer la clé"
        AppLanguage.EN -> "Clear Key"
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Status banner
                val statusBg = if (providerStatus.isLinked) {
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                }
                val statusColor = if (providerStatus.isLinked) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.error
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Icon(
                            imageVector = if (providerStatus.isLinked) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val statusText = if (providerStatus.isLinked) {
                            when (language) {
                                AppLanguage.AR -> "مزود الذكاء الاصطناعي متصل وجاهز"
                                AppLanguage.FR -> "Fournisseur d'IA connecté et prêt"
                                AppLanguage.EN -> "AI Provider connected and ready"
                            }
                        } else {
                            when (language) {
                                AppLanguage.AR -> "المزود غير مرتبط حالياً (لن يتم توليد ردود وهمية)"
                                AppLanguage.FR -> "Fournisseur non associé (aucune réponse simulée)"
                                AppLanguage.EN -> "Provider unlinked (no fake responses)"
                            }
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val explanationText = when (language) {
                    AppLanguage.AR -> "يدعم التطبيق مفاتيح Google AI Studio الرسمية. يمكنك إدخال مفتاحك هنا، أو ضبطه عبر لوحة أسرار AI Studio (GEMINI_API_KEY)."
                    AppLanguage.FR -> "Dadix AI utilise l'API officielle Google AI Studio. Vous pouvez saisir votre clé ci-dessous ou la configurer via le panneau des secrets."
                    AppLanguage.EN -> "Dadix AI calls the official Google Gemini API. Enter your key below, or set it via the AI Studio Secrets panel (GEMINI_API_KEY)."
                }

                Text(
                    text = explanationText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    label = { Text(hint) },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide key" else "Show key"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input_field"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (providerStatus.source == ApiKeySource.AI_STUDIO_SECRETS) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val injectedText = when (language) {
                            AppLanguage.AR -> "تم اكتشاف مفتاح محقون من لوحة أسرار AI Studio: ${providerStatus.maskedKey}"
                            AppLanguage.FR -> "Clé injectée depuis les secrets: ${providerStatus.maskedKey}"
                            AppLanguage.EN -> "Injected secret key detected: ${providerStatus.maskedKey}"
                        }
                        Text(
                            text = injectedText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveKey(keyInput)
                    onDismissRequest()
                },
                modifier = Modifier.testTag("save_api_key_button")
            ) {
                Text(saveText)
            }
        },
        dismissButton = {
            Row {
                if (currentCustomKey.isNotBlank()) {
                    TextButton(
                        onClick = {
                            keyInput = ""
                            onSaveKey("")
                            onDismissRequest()
                        }
                    ) {
                        Text(clearText, color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismissRequest) {
                    Text(cancelText)
                }
            }
        }
    )
}
