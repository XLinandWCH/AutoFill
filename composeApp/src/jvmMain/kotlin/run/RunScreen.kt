package run

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun RunScreen() {
    val customSelectionColors = TextSelectionColors(
        handleColor = Color.White,
        backgroundColor = Color(0xC869EF79).copy(alpha = 0.15f)
    )

    Box(
        modifier = Modifier.fillMaxSize().padding(6.dp),
        contentAlignment = Alignment.Center
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
                    SimpleTable()
                }
            }
        }
    }
}

@Composable
fun SimpleTable() {
    val data = listOf(
        listOf("ID", "数量", "状态", "提交", "运行"), // 表头
        listOf("001", "10", "运行", "10:00", "5ms"), // 数据行
        listOf("002", "5", "停止", "10:05", "0ms")
    )

    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        data.forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { cell ->
                    Text(
                        text = cell,
                        modifier = Modifier
                            .weight(1f) // 自动平分宽度
                            .border(1.dp, Color.Gray) // 画边框
                            .padding(8.dp),
                        color = Color.White
                    )
                }
            }
        }
    }
}