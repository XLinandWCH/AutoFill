package SolutionFormat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontFamily

@Composable
fun FillBlankQuestions(questionIndex: Int, optionIndex: Int) {
    var text by remember(questionIndex, optionIndex) {
        mutableStateOf<String>(AnswerDictionary.getAnswer(questionIndex, optionIndex))
    }

    val lines = text.split("\n")
    val linesCount = lines.size.coerceAtLeast(1)
    
    val fontSize = 16.sp
    val lineHeight = 24.sp
    
    Column(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().height(250.dp)) {
            Box(modifier = Modifier.width(40.dp).fillMaxHeight().padding(vertical = 4.dp)) {
                val numbers = (1..linesCount).joinToString("\n")
                Text(
                    text = numbers,
                    color = Color.Gray,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color.Gray))

            BasicTextField(
                value = text,
                onValueChange = {
                    text = it
                    AnswerDictionary.updateAnswer(questionIndex, optionIndex, it)
                    AnswerDictionary.printDictionary()
                },
                modifier = Modifier.fillMaxSize().padding(start = 12.dp, top = 4.dp),
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    fontFamily = FontFamily.Monospace
                ),
                cursorBrush = SolidColor(Color.White)
            )
        }
    }
}
