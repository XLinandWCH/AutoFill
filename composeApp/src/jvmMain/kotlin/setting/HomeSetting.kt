package setting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import run.SurveyRunManager

@Composable
fun HomeSetting() {
    val customSelectionColors = TextSelectionColors(
        handleColor = Color.White,          // 手柄颜色（通常设为白色）
        backgroundColor = Color(0xC869EF79).copy(alpha = 0.15f) // 选中背景色（建议半透明）
    )

    var text_thread by remember { mutableStateOf(SurveyRunManager.threadCount.value.toString()) }
    var text_num by remember { mutableStateOf(SurveyRunManager.totalTarget.value.toString()) }

    CompositionLocalProvider(LocalTextSelectionColors provides customSelectionColors) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier.fillMaxSize().padding(6.dp),
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF3E3E3E)
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp)
                        ) {
                            Text(
                                text = "线程数:",
                                color = Color.White,
                                fontWeight = FontWeight.W300,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.padding(12.dp))

                            BasicTextField(
                                value = text_thread,
                                onValueChange = { newText ->
                                    val filtered = newText.filter { it.isDigit() }
                                    text_thread = filtered
                                    // 同步写入全局管理器
                                    val value = filtered.toIntOrNull() ?: 1
                                    SurveyRunManager.threadCount.value = value.coerceIn(1, 20)
                                },
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(30.dp)
                                    .border(
                                        width = 1.dp,
                                        color = Color.White,
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                                    )
                                    .background(
                                        color = Color.Transparent,
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                                    ),
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Normal,
                                    textAlign = TextAlign.Center // 1. 确保文字在行内水平居中
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(Color.White), // 2. 光标颜色设置为白色
                                // 3. 使用 decorationBox 实现容器内的完全居中
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center // 使内部文字在 Box 中垂直和水平居中
                                    ) {
                                        innerTextField()
                                    }
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "问卷数:",
                                color = Color.White,
                                fontWeight = FontWeight.W300,
                                fontSize = 20.sp
                            )

                            Spacer(modifier = Modifier.padding(12.dp))

                            BasicTextField(
                                value = text_num,
                                onValueChange = { newText ->
                                    val filtered = newText.filter { it.isDigit() }
                                    text_num = filtered
                                    // 同步写入全局管理器
                                    val value = filtered.toIntOrNull() ?: 1
                                    SurveyRunManager.totalTarget.value = value.coerceAtLeast(1)
                                },
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(30.dp)
                                    .border(
                                        width = 1.dp,
                                        color = Color.White,
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                                    )
                                    .background(
                                        color = Color.Transparent,
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                                    ),
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Normal,
                                    textAlign = TextAlign.Center // 1. 确保文字在行内水平居中
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(Color.White), // 2. 光标颜色设置为白色
                                // 3. 使用 decorationBox 实现容器内的完全居中
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center // 使内部文字在 Box 中垂直和水平居中
                                    ) {
                                        innerTextField()
                                    }
                                }
                            )
                        }

                        // 新增反爬虫控制开关
                        Row(
                            modifier = Modifier.padding(start = 2.dp, end = 12.dp, top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = SurveyRunManager.isAntiBotEnabled.value,
                                onCheckedChange = { SurveyRunManager.isAntiBotEnabled.value = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF69EF79),
                                    checkmarkColor = Color.Black,
                                    uncheckedColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "开启反爬虫机制",
                                color = Color.White,
                                fontWeight = FontWeight.W300,
                                fontSize = 18.sp
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            // 新增无头模式控制开关
                            Checkbox(
                                checked = SurveyRunManager.isHeadlessEnabled.value,
                                onCheckedChange = { SurveyRunManager.isHeadlessEnabled.value = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF69EF79),
                                    checkmarkColor = Color.Black,
                                    uncheckedColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "开启无头模式",
                                color = Color.White,
                                fontWeight = FontWeight.W300,
                                fontSize = 18.sp
                            )
                        }

                        // 新增浏览器内核状态按钮
                        Row(
                            modifier = Modifier.padding(start = 12.dp, top = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isBrowserReady = other.BrowserManager.isBrowserReady.collectAsState()
                            val statusText = if (isBrowserReady.value) "✅ 内核已就绪" else "❌ 内核缺失"
                            val statusColor = if (isBrowserReady.value) Color(0xFF2BAC3B) else Color(0xFFD91E1E)

                            Button(
                                onClick = { other.BrowserManager.showDialog.value = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2BAC3B))
                            ) {
                                Text("检查与下载浏览器内核", color = Color.White)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = statusText,
                                color = statusColor,
                                fontSize = 14.sp
                            )
                        }

                    }

                }
            }

            // 浏览器内核下载对话框已移至 App.kt 全局层
        }


    }
}
