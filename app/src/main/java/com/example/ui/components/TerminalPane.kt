package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LogLevel
import com.example.data.model.TerminalLog
import com.example.ui.theme.DadixCyan
import com.example.ui.theme.DadixEmerald
import com.example.ui.theme.DadixPurple
import com.example.ui.theme.DadixRose

@Composable
fun TerminalPane(
    logs: List<TerminalLog>,
    onExecuteCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var commandInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Surface(
        color = Color(0xFF09080F),
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Terminal Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF110F1C))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = DadixCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dadix Web Terminal (bash / node v22)",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE2E8F0)
                        )
                    )
                }

                // Quick command buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { onExecuteCommand("npm run dev") },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DadixPurple.copy(alpha = 0.2f)),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("npm run dev", fontSize = 10.sp, color = DadixPurple, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = { onExecuteCommand("npm run build") },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DadixEmerald.copy(alpha = 0.2f)),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("build", fontSize = 10.sp, color = DadixEmerald, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { onExecuteCommand("clear") },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF221F38))

            // Terminal Logs Area
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(logs) { log ->
                    val color = when (log.level) {
                        LogLevel.COMMAND -> DadixPurple
                        LogLevel.SUCCESS -> DadixEmerald
                        LogLevel.ERROR -> DadixRose
                        LogLevel.INFO -> DadixCyan
                        LogLevel.OUTPUT -> Color(0xFFE2E8F0)
                    }

                    Text(
                        text = log.message,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = color,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            // Command Prompt Line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F0E1A))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "dadix@project:~$ ",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DadixCyan
                    )
                )

                BasicTextField(
                    value = commandInput,
                    onValueChange = { commandInput = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color.White
                    ),
                    cursorBrush = SolidColor(DadixCyan),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        val cmd = commandInput.trim()
                        if (cmd.isNotEmpty()) {
                            commandInput = ""
                            onExecuteCommand(cmd)
                        }
                    }),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("terminal_input_line")
                )

                IconButton(
                    onClick = {
                        val cmd = commandInput.trim()
                        if (cmd.isNotEmpty()) {
                            commandInput = ""
                            onExecuteCommand(cmd)
                        }
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run",
                        tint = DadixCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
