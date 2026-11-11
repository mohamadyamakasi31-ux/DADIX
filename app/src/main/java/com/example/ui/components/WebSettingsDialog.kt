package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiModel
import com.example.data.model.AppLanguage
import com.example.data.model.AppThemeMode
import com.example.data.model.ProviderStatus
import com.example.ui.theme.DadixCyan
import com.example.ui.theme.DadixEmerald
import com.example.ui.theme.DadixPurple

@Composable
fun WebSettingsDialog(
    selectedModel: AiModel,
    providerStatus: ProviderStatus,
    themeMode: AppThemeMode,
    language: AppLanguage,
    currentApiKey: String,
    currentCfAccount: String,
    currentCfToken: String,
    onSelectModel: (AiModel) -> Unit,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onSetLanguage: (AppLanguage) -> Unit,
    onSaveApiKey: (String) -> Unit,
    onSaveCloudflare: (String, String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var apiKeyInput by remember { mutableStateOf(currentApiKey) }
    var apiKeyVisible by remember { mutableStateOf(false) }

    var cfAccountInput by remember { mutableStateOf(currentCfAccount) }
    var cfTokenInput by remember { mutableStateOf(currentCfToken) }
    var cfTokenVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                onClick = {
                    onSaveApiKey(apiKeyInput.trim())
                    onSaveCloudflare(cfAccountInput.trim(), cfTokenInput.trim())
                    onDismissRequest()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DadixPurple),
                modifier = Modifier.testTag("save_settings_btn")
            ) {
                Text("حفظ الإعدادات", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("إغلاق")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = DadixPurple,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("إعدادات المنصة ومزودي الخدمة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Gemini AI Provider Section
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (providerStatus.isLinked) DadixEmerald.copy(alpha = 0.12f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (providerStatus.isLinked) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (providerStatus.isLinked) DadixEmerald else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (providerStatus.isLinked) "مزود الذكاء الاصطناعي متصل وجاهز" else "مزود الذكاء الاصطناعي غير متصل",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (providerStatus.isLinked) DadixEmerald else MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (providerStatus.isLinked) "يتم إرسال الطلبات إلى Google Gemini API وتوليد أكواد المواقع الحقيقية."
                            else "تطبيق Dadix Web AI لا يولد مشاريع وهمية. يرجى توفير مفتاح Gemini API لتفعيل التوليد الذكي.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }

                // API Key input
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("مفتاح Google Gemini API Key") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                            Icon(if (apiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. AI Model Selection
                Text("نموذج الذكاء الاصطناعي النشط:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AiModel.ALL.forEach { model ->
                        val isSelected = model == selectedModel
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) DadixPurple.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .border(1.dp, if (isSelected) DadixPurple else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { onSelectModel(model) }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(model.displayName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f), fontSize = 13.sp)
                            Text(model.speedTag, fontSize = 11.sp, color = DadixPurple, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // 3. Cloudflare Pages Deployment Section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = DadixCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إعدادات نشر Cloudflare Pages", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = cfAccountInput,
                    onValueChange = { cfAccountInput = it },
                    label = { Text("Cloudflare Account ID") },
                    placeholder = { Text("مثال: e3b0c44298fc1c149afbf4c8996fb924") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cfTokenInput,
                    onValueChange = { cfTokenInput = it },
                    label = { Text("Cloudflare API Token") },
                    placeholder = { Text("Bearer Token مع صلاحيات Pages") },
                    singleLine = true,
                    visualTransformation = if (cfTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { cfTokenVisible = !cfTokenVisible }) {
                            Icon(if (cfTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // 4. Language & Theme
                Text("لغة المنصة:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguage.entries.forEach { lang ->
                        val isSelected = lang == language
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) DadixPurple else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSetLanguage(lang) }
                        ) {
                            Text(
                                text = "${lang.flagEmoji} ${lang.nativeName}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    )
}
