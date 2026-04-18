package run

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun RunScreen() {
    val customSelectionColors = TextSelectionColors(
        handleColor = Color.White,
        backgroundColor = Color(0xC869EF79).copy(alpha = 0.15f)
    )

    val taskLogs = SurveyRunManager.taskLogs
    val listState = rememberLazyListState()

    // 自动滚动到底部（新任务出现时）
    LaunchedEffect(taskLogs.size) {
        if (taskLogs.isNotEmpty()) {
            listState.animateScrollToItem(taskLogs.size - 1)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(6.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        CompositionLocalProvider(LocalTextSelectionColors provides customSelectionColors) {
            SelectionContainer {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF3E3E3E)
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        // ── 表头 ──
                        TableHeader()

                        Spacer(modifier = Modifier.height(2.dp))

                        // ── 数据行 ──
                        if (taskLogs.isEmpty()) {
                            // 空状态
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "暂无运行记录\n点击上方 \"运行\" 按钮开始自动填写问卷",
                                    color = Color(0xFF888888),
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 28.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(taskLogs) { entry ->
                                    TaskRow(entry)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeader() {
    val headerColor = Color(0xFF555555)
    val textColor = Color(0xFFCCCCCC)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(headerColor, RoundedCornerShape(4.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        HeaderCell("ID", Modifier.width(60.dp), textColor)
        HeaderCell("线程", Modifier.width(80.dp), textColor)
        HeaderCell("状态", Modifier.width(100.dp), textColor)
        HeaderCell("耗时", Modifier.width(90.dp), textColor)
        HeaderCell("详情", Modifier.weight(1f), textColor)
    }
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier, color: Color) {
    Text(
        text = text,
        modifier = modifier.padding(horizontal = 4.dp),
        color = color,
        fontWeight = FontWeight.W600,
        fontSize = 14.sp,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun TaskRow(entry: SurveyRunManager.TaskLogEntry) {
    val statusColor = when {
        entry.status.contains("成功") -> Color(0xFF81C784)
        entry.status.contains("失败") -> Color(0xFFE57373)
        entry.status.contains("运行") -> Color(0xFF64B5F6)
        else -> Color(0xFF999999)
    }

    val rowBg = if (entry.id % 2 == 0) Color(0xFF404040) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ID
        Text(
            text = "${entry.id}",
            modifier = Modifier.width(60.dp),
            color = Color.White,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        // 线程
        Text(
            text = entry.threadName,
            modifier = Modifier.width(80.dp),
            color = Color(0xFFBBBBBB),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        // 状态
        Text(
            text = entry.status,
            modifier = Modifier.width(100.dp),
            color = statusColor,
            fontWeight = FontWeight.W500,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        // 耗时
        Text(
            text = if (entry.elapsedMs > 0) "${entry.elapsedMs}ms" else "-",
            modifier = Modifier.width(90.dp),
            color = Color(0xFFBBBBBB),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        // 详情
        Text(
            text = entry.detail.ifBlank { "-" },
            modifier = Modifier.weight(1f),
            color = Color(0xFF999999),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start
        )
    }
}