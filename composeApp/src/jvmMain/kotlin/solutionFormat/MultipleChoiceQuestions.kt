package SolutionFormat

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var text by remember(questionIndex, optionIndex) { 
        mutableStateOf<String>(AnswerDictionary.getAnswer(questionIndex, optionIndex))
    }
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
                value = text,
                onValueChange = {
                    text = it
                    AnswerDictionary.updateAnswer(questionIndex, optionIndex, it)
                    AnswerDictionary.printDictionary()
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
