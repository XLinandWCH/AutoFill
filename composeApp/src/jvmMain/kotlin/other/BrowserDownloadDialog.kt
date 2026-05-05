package other

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * 全局浏览器状态横幅
 *
 * 放在 App 顶层，当浏览器内核缺失或正在下载时，
 * 在所有页面顶部显示一条状态提示栏。
 * 用户可以点击横幅打开详细的下载对话框。
 */
@Composable
fun BrowserStatusBanner() {
    val isBrowserReady by BrowserManager.isBrowserReady.collectAsState()
    val isDownloading by BrowserManager.isDownloading.collectAsState()

    // 只在内核未就绪时显示
    AnimatedVisibility(
        visible = !isBrowserReady,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        val bgColor = if (isDownloading) Color(0xFF1A3A5C) else Color(0xFF5C1A1A)
        val statusText = if (isDownloading) {
            "⏳ 正在下载 Chromium 浏览器内核，请耐心等待..."
        } else {
            "⚠️ 浏览器内核未就绪，自动化功能无法使用。"
        }
        val actionText = if (isDownloading) "查看详情" else "点击下载"

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .clickable { BrowserManager.showDialog.value = true }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = statusText,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.W400,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = actionText,
                color = Color(0xFF82CFFF),
                fontSize = 13.sp,
                fontWeight = FontWeight.W500,
                modifier = Modifier.clickable {
                    BrowserManager.showDialog.value = true
                    if (!isDownloading && !isBrowserReady) {
                        BrowserManager.retryDownload()
                    }
                }
            )
        }
    }
}

/**
 * 浏览器内核下载对话框
 *
 * 显示下载日志、状态和操作按钮。
 * 现在作为全局组件放在 App 层，可以从任何页面触发。
 */
@Composable
fun BrowserDownloadDialog() {
    val showDialog by BrowserManager.showDialog.collectAsState()
    val logs by BrowserManager.downloadLogs.collectAsState()
    val isDownloading by BrowserManager.isDownloading.collectAsState()
    val isBrowserReady by BrowserManager.isBrowserReady.collectAsState()

    if (showDialog) {
        Dialog(
            onDismissRequest = {
                BrowserManager.showDialog.value = false
            },
            properties = DialogProperties(
                dismissOnClickOutside = false,
                usePlatformInsets = true,
                scrimColor = Color(0x601A1A1A)
            )
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                elevation = 8.dp,
                modifier = Modifier.width(560.dp).height(420.dp),
                color = Color(0xFF1E1E1E),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Chromium 浏览器内核管理",
                        style = MaterialTheme.typography.h6,
                        color = Color.White,
                    )

                    // 状态行
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "状态: ",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = when {
                                isDownloading -> "正在下载..."
                                isBrowserReady -> "✅ 内核就绪"
                                else -> "❌ 内核缺失"
                            },
                            color = when {
                                isDownloading -> Color(0xFFE2C843)
                                isBrowserReady -> Color(0xFF2BAC3B)
                                else -> Color(0xFFD91E1E)
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W500
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "路径: ${BrowserManager.BROWSERS_PATH}",
                            color = Color(0xFF888888),
                            fontSize = 11.sp,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 日志输出框
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFF2E2E2E), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        val listState = rememberLazyListState()

                        // 自动滚动到最底部
                        LaunchedEffect(logs.size) {
                            if (logs.isNotEmpty()) {
                                listState.animateScrollToItem(logs.size - 1)
                            }
                        }

                        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                            items(logs) { log ->
                                val logColor = when {
                                    log.startsWith("✅") -> Color(0xFF81C784)
                                    log.startsWith("❌") -> Color(0xFFE57373)
                                    log.startsWith("⚠️") -> Color(0xFFFFB74D)
                                    log.startsWith("──") -> Color(0xFF64B5F6)
                                    else -> Color.LightGray
                                }
                                Text(
                                    text = log,
                                    color = logColor,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // 底部操作按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    ) {
                        // 重试按钮（内核未就绪且不在下载中时显示）
                        if (!isBrowserReady && !isDownloading) {
                            TextButton(
                                onClick = { BrowserManager.retryDownload() },
                                modifier = Modifier.width(120.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2BAC3B),
                                )
                            ) {
                                Text("重新下载", color = Color.White)
                            }
                        }

                        TextButton(
                            onClick = { BrowserManager.showDialog.value = false },
                            modifier = Modifier.width(100.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD91E1E),
                            )
                        ) {
                            Text("关闭", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
