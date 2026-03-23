package content

import SolutionFormat.HomeSolution
import SolutionFormat.AnswerDictionary
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.height

@Composable
fun HomeContent(surveyData: MutableMap<String, Any>) {

    val divx = surveyData["divx"] as? Int ?: 0
    val error = surveyData["error"] as? String
    val questions = surveyData["question"] as? List<String> ?:emptyList()
    val options = surveyData["option"] as? List<List<String>>?:emptyList()
    val typeInts = surveyData["typeInts"] as? List<Int> ?: emptyList()

    androidx.compose.runtime.LaunchedEffect(typeInts, options) {
        AnswerDictionary.initialize(typeInts, options)
    }


    val customSelectionColors = TextSelectionColors(
        handleColor = Color.White,          // 手柄颜色（通常设为白色）
        backgroundColor = Color(0xC869EF79).copy(alpha = 0.15f) // 选中背景色（建议半透明）
    )

    LazyColumn() {

        if (error != null){
            item {

                Box(
                    modifier = Modifier.fillMaxSize().padding(22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(LocalTextSelectionColors provides customSelectionColors){
                        SelectionContainer {
                            Card(
                                modifier = Modifier.fillMaxWidth(0.74f),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF3E3E3E),  // 卡片背景色
                                ),
                                shape = RoundedCornerShape(6.dp)

                            ) {

                                Text(
                                    text = "错误信息：$error",
                                    fontWeight = FontWeight.W300,
                                    fontSize = 24.sp,
                                    color = Color.White
                                )

                            }

                        }

                    }

                }

            }

        } else {
            items(divx) { index ->
                // 题目
                // 获取当前索引对应的题目
                val currentQuestion = questions.getOrNull(index) ?: "题目加载失败"
                val currentOption = options.getOrNull(index)?:emptyList()
                Box(
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                    contentAlignment = Alignment.Center
                ){
                    CompositionLocalProvider(LocalTextSelectionColors provides customSelectionColors){
                        SelectionContainer {
                            Card(
                                modifier = Modifier.fillMaxWidth(0.74f),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF3E3E3E),  // 卡片背景色
                                ),
                                shape = RoundedCornerShape(6.dp)

                            ) {

                                Text(
                                    text = currentQuestion,
                                    fontWeight = FontWeight.W300,
                                    fontSize = 24.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(vertical = 5.dp)
                                )

                                val currentType = typeInts.getOrNull(index) ?: 3

                                if (currentType == 1) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(6.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF505050),
                                        ),
                                        shape = RoundedCornerShape(2.dp)
                                    ) {
                                        HomeSolution(questionIndex = index, optionIndex = 0, type = currentType)
                                    }
                                } else {
                                    currentOption.forEachIndexed { optionIndex, option ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(6.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color(0xFF505050),  // 卡片背景色
                                            ),
                                            shape = RoundedCornerShape(2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = option,
                                                    fontWeight = FontWeight.W300,
                                                    fontSize = 22.sp,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp).weight(1f)
                                                )
                                                HomeSolution(questionIndex = index, optionIndex = optionIndex, type = currentType)
                                            }
                                        }
                                    }
                                }

                            }

                        }

                    }

                }

            }

        }

    }

}

