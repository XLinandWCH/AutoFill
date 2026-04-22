package SolutionFormat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MultipleChoiceQuestions(questionIndex: Int, optionIndex: Int) {
    val currentText = AnswerDictionary.getAnswer(questionIndex, optionIndex)

    Card(
        modifier = Modifier
            .width(84.dp)
            .fillMaxHeight(),
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
                value = currentText,
                onValueChange = {
                    AnswerDictionary.updateAnswer(questionIndex, optionIndex, it)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(Color.White)
            )
        }
    }
}
