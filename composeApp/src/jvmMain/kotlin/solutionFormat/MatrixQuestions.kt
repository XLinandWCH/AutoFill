package SolutionFormat

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MatrixQuestions(questionIndex: Int, optionIndex: Int) {
    val currentText = AnswerDictionary.getAnswer(questionIndex, optionIndex)
    val cols = AnswerDictionary.matrixColsMap[questionIndex] ?: emptyList()
    
    // Fallback if no columns are parsed (e.g. not a standard matrix layout)
    if (cols.isEmpty() || !currentText.contains(",")) {
        ChoiceQuestions(questionIndex, optionIndex)
        return
    }

    val probs = currentText.split(",").toMutableList()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        cols.forEachIndexed { colIndex, colTitle ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Text(
                    text = colTitle,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp),
                    maxLines = 1
                )
                Card(
                    modifier = Modifier
                        .width(42.dp)
                        .height(36.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xE4656363)
                    ),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicTextField(
                            value = probs.getOrElse(colIndex) { "50" },
                            onValueChange = { newValue ->
                                if (colIndex < probs.size) {
                                    probs[colIndex] = newValue
                                } else {
                                    while (probs.size <= colIndex) probs.add("50")
                                    probs[colIndex] = newValue
                                }
                                AnswerDictionary.updateAnswer(questionIndex, optionIndex, probs.joinToString(","))
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
        }
    }
}