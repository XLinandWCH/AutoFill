package Documentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// 定义文档类型枚举
enum class DocType {
    USAGE_GUIDE,      // 使用说明
    SERVICE_AGREEMENT // 服务协议
}

@Composable
fun HomeDocumentation() {
    var selectedDoc by remember { mutableStateOf(DocType.USAGE_GUIDE) }
    var markdownContent by remember { mutableStateOf("") }

    LaunchedEffect(selectedDoc) {
        val fileName = when (selectedDoc) {
            DocType.USAGE_GUIDE -> "file/usage.md"
            DocType.SERVICE_AGREEMENT -> "file/agreement.md"
        }
        // JVM 资源加载方式
        markdownContent = try {
            Thread.currentThread().contextClassLoader.getResourceAsStream(fileName)
                ?.bufferedReader()
                ?.use { it.readText() } ?: "未找到文件: $fileName"
        } catch (e: Exception) {
            "加载错误: ${e.message}"
        }
    }

    val customSelectionColors = TextSelectionColors(
        handleColor = Color.White,          // 手柄颜色（通常设为白色）
        backgroundColor = Color(0xC869EF79).copy(alpha = 0.15f) // 选中背景色（建议半透明）
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            DoCatalog(
                selectedDoc = selectedDoc,
                onDocChange = { selectedDoc = it }
            )
            // Spacer(modifier = Modifier.height(8.dp))

            CompositionLocalProvider(LocalTextSelectionColors provides customSelectionColors){

                SelectionContainer {

                    Card(
                        modifier = Modifier.fillMaxSize().padding(6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF3E3E3E)
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ){
                        MarkdownContent(markdownContent)
                    }
                }
            }
        }
    }
}
