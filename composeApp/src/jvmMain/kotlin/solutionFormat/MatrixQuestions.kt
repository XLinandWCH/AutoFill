package SolutionFormat

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 矩阵题整体渲染组件
 *
 * 渲染效果示例：
 *           速度快   准确率高   信息量多   界面更美观
 *  百度      [50]     [50]      [50]      [50]
 *  Google    [50]     [50]      [50]      [50]
 *  搜狗      [50]     [50]      [50]      [50]
 *
 * @param questionIndex 题目索引
 * @param rowOptions    行标题列表（如 ["百度", "Google", "搜狗"]）
 */
@Composable
fun MatrixQuestionsTable(questionIndex: Int, rowOptions: List<String>) {
    val cols = AnswerDictionary.matrixColsMap[questionIndex] ?: emptyList()

    if (cols.isEmpty()) {
        // 列信息缺失时的 fallback：逐行渲染为普通选择题
        rowOptions.forEachIndexed { optionIndex, _ ->
            ChoiceQuestions(questionIndex, optionIndex)
        }
        return
    }

    // 如果行标题列表为空（抓取失败），为了保证功能可用，至少提供一个默认行
    val finalRowOptions = if (rowOptions.isEmpty()) listOf("请配置行标题") else rowOptions

    val rowLabelWidth = 120.dp
    val cellWidth = 72.dp
    val cellHeight = 36.dp

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // ── 表头行：空白角 + 列标题 ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左上角空白（与行标签等宽）
            Spacer(modifier = Modifier.width(rowLabelWidth))

            cols.forEach { colTitle ->
                Box(
                    modifier = Modifier
                        .width(cellWidth)
                        .padding(horizontal = 2.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = colTitle,
                        color = Color(0xFFB0B0B0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W400,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // ── 数据行 ──
        finalRowOptions.forEachIndexed { rowIndex, rowLabel ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 行标题
                Box(
                    modifier = Modifier.width(rowLabelWidth),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = rowLabel,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.W300,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }

                // 每列的输入框
                cols.forEachIndexed { colIndex, _ ->
                    MatrixCell(
                        questionIndex = questionIndex,
                        rowIndex = rowIndex,
                        colIndex = colIndex,
                        totalCols = cols.size,
                        cellWidth = cellWidth,
                        cellHeight = cellHeight
                    )
                }
            }
        }
    }
}

/**
 * 矩阵单元格：一个可编辑的概率输入框
 */
@Composable
private fun MatrixCell(
    questionIndex: Int,
    rowIndex: Int,
    colIndex: Int,
    totalCols: Int,
    cellWidth: androidx.compose.ui.unit.Dp,
    cellHeight: androidx.compose.ui.unit.Dp
) {
    // 从 AnswerDictionary 读取当前行的完整答案（格式："50,50,50,50"）
    val rawAnswer = AnswerDictionary.getAnswer(questionIndex, rowIndex)
    val probs = rawAnswer.split(",").toMutableList()
    // 确保 probs 长度足够
    while (probs.size < totalCols) probs.add("50")

    val cellValue = probs[colIndex]

    Card(
        modifier = Modifier
            .width(cellWidth)
            .height(cellHeight)
            .padding(horizontal = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xE4656363)
        ),
        shape = RoundedCornerShape(3.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = cellValue,
                onValueChange = { newValue ->
                    probs[colIndex] = newValue
                    AnswerDictionary.updateAnswer(
                        questionIndex,
                        rowIndex,
                        probs.joinToString(",")
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(Color.White)
            )
        }
    }
}

/**
 * 保留旧的单行签名以兼容 HomeSolution 路由（不再使用，但保留编译兼容）
 * 实际上矩阵题应通过 HomeContent 中 type==6 分支调用 MatrixQuestionsTable
 */
@Composable
fun MatrixQuestions(questionIndex: Int, optionIndex: Int) {
    // 如果仍被旧路径调用，则退化为普通输入框
    ChoiceQuestions(questionIndex, optionIndex)
}