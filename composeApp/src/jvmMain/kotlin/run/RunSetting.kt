package run

import SolutionFormat.AnswerDictionary
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RunSetting() {
    val scope = rememberCoroutineScope()
    val state = SurveyRunManager.runState.value
    val title = SurveyRunManager.surveyTitle.value.ifBlank { "未加载问卷" }
    val completed = SurveyRunManager.completedCount.value
    val total = SurveyRunManager.totalTarget.value
    val success = SurveyRunManager.successCount.get()
    val fail = SurveyRunManager.failCount.get()

    val customSelectionColors = TextSelectionColors(
        handleColor = Color.White,
        backgroundColor = Color(0xC869EF79).copy(alpha = 0.15f)
    )

    CompositionLocalProvider(LocalTextSelectionColors provides customSelectionColors) {
        SelectionContainer {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF343434)
                ),
                shape = RoundedCornerShape(0.dp)
            ) {
                // 顶部分割线
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFF4B4A4A))
                )

                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左侧：标题 + 进度
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "标题：",
                                color = Color.White,
                                fontWeight = FontWeight.W300,
                                fontSize = 18.sp,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = title,
                                color = Color(0xFF82CFFF),
                                fontWeight = FontWeight.W400,
                                fontSize = 18.sp,
                                textDecoration = TextDecoration.Underline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (state != SurveyRunManager.RunState.IDLE) {
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "进度: $completed/$total | 成功: $success | 失败: $fail",
                                    color = Color(0xFFAAFFAA),
                                    fontWeight = FontWeight.W300,
                                    fontSize = 15.sp,
                                )
                            }
                        }

                        // 右侧：操作按钮
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            when (state) {
                                SurveyRunManager.RunState.IDLE -> {
                                    ActionButton(
                                        text = "运行",
                                        color = Color(0xFF4CAF50),
                                        enabled = true,
                                        onClick = { SurveyRunManager.start(scope) }
                                    )
                                }

                                SurveyRunManager.RunState.RUNNING -> {
                                    ActionButton(
                                        text = "全部暂停",
                                        color = Color(0xFFFFA726),
                                        onClick = { SurveyRunManager.pauseAll() }
                                    )
                                    ActionButton(
                                        text = "停止",
                                        color = Color(0xFFEF5350),
                                        onClick = { SurveyRunManager.stopAll() }
                                    )
                                }

                                SurveyRunManager.RunState.PAUSED -> {
                                    ActionButton(
                                        text = "恢复运行",
                                        color = Color(0xFF4CAF50),
                                        onClick = { SurveyRunManager.resumeAll() }
                                    )
                                    ActionButton(
                                        text = "停止",
                                        color = Color(0xFFEF5350),
                                        onClick = { SurveyRunManager.stopAll() }
                                    )
                                }

                                SurveyRunManager.RunState.STOPPING -> {
                                    Text(
                                        text = "停止中...",
                                        color = Color(0xFFFF8A80),
                                        fontSize = 16.sp
                                    )
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
private fun ActionButton(
    text: String,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val bgColor = if (enabled) color else Color(0xFF666666)
    val textColor = if (enabled) Color.White else Color(0xFFAAAAAA)

    Card(
        modifier = Modifier
            .clickable(enabled = enabled) { onClick() },
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.W500,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}