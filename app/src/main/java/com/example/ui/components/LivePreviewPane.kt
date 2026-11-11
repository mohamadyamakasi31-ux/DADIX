package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.ProjectFileEntity
import com.example.ui.DeviceFrame
import com.example.ui.theme.DadixEmerald
import com.example.ui.theme.DadixPurple

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LivePreviewPane(
    files: List<ProjectFileEntity>,
    deviceFrame: DeviceFrame,
    reloadTrigger: Int,
    onDeviceFrameChange: (DeviceFrame) -> Unit,
    onReload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Build standalone HTML payload that bundles CSS and JS
    val compiledHtml = remember(files, reloadTrigger) {
        val indexFile = files.firstOrNull { it.path.equals("index.html", ignoreCase = true) }
        val cssFile = files.firstOrNull { it.path.equals("style.css", ignoreCase = true) }
        val jsFile = files.firstOrNull { it.path.equals("app.js", ignoreCase = true) || it.path.equals("script.js", ignoreCase = true) || it.path.equals("main.js", ignoreCase = true) }

        if (indexFile != null) {
            var raw = indexFile.content

            // Inline custom CSS if present
            if (cssFile != null && cssFile.content.isNotBlank()) {
                val inlineStyle = "<style>\n${cssFile.content}\n</style>"
                raw = if (raw.contains("</head>", ignoreCase = true)) {
                    raw.replace("</head>", "$inlineStyle\n</head>")
                } else {
                    "$inlineStyle\n$raw"
                }
            }

            // Inline custom JS if present
            if (jsFile != null && jsFile.content.isNotBlank()) {
                val inlineScript = "<script>\n${jsFile.content}\n</script>"
                raw = if (raw.contains("</body>", ignoreCase = true)) {
                    raw.replace("</body>", "$inlineScript\n</body>")
                } else {
                    "$raw\n$inlineScript"
                }
            }

            raw
        } else {
            "<html><body style='background:#09090E;color:#fff;font-family:sans-serif;padding:40px;text-align:center;'><h2>لم يتم العثور على index.html في هذا المشروع</h2></body></html>"
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Preview Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Device Frames Switcher
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    IconButton(
                        onClick = { onDeviceFrameChange(DeviceFrame.DESKTOP) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DesktopWindows,
                            contentDescription = "Desktop",
                            tint = if (deviceFrame == DeviceFrame.DESKTOP) DadixPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { onDeviceFrameChange(DeviceFrame.TABLET) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tablet,
                            contentDescription = "Tablet",
                            tint = if (deviceFrame == DeviceFrame.TABLET) DadixPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { onDeviceFrameChange(DeviceFrame.MOBILE) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Smartphone,
                            contentDescription = "Mobile",
                            tint = if (deviceFrame == DeviceFrame.MOBILE) DadixPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Status & Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(DadixEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مباشر (Live)",
                            style = MaterialTheme.typography.labelSmall,
                            color = DadixEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onReload,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("preview_reload_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"))
                            context.startActivity(browserIntent)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "Open Browser",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Webview framed area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF07070B)),
                contentAlignment = Alignment.TopCenter
            ) {
                val frameModifier = when (deviceFrame) {
                    DeviceFrame.DESKTOP -> Modifier.fillMaxSize()
                    DeviceFrame.TABLET -> Modifier
                        .width(420.dp)
                        .fillMaxHeight()
                        .padding(vertical = 12.dp)
                        .border(2.dp, Color(0xFF2A2742), RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                    DeviceFrame.MOBILE -> Modifier
                        .width(320.dp)
                        .fillMaxHeight()
                        .padding(vertical = 12.dp)
                        .border(2.dp, Color(0xFF2A2742), RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                }

                AndroidView(
                    modifier = frameModifier.testTag("live_webview_preview"),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                            webViewClient = WebViewClient()
                            webChromeClient = object : WebChromeClient() {
                                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                    return true
                                }
                            }
                            loadDataWithBaseURL("https://dadix.local/", compiledHtml, "text/html", "UTF-8", null)
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL("https://dadix.local/", compiledHtml, "text/html", "UTF-8", null)
                    }
                )
            }
        }
    }
}
