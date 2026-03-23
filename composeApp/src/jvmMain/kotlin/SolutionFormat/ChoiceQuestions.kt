package SolutionFormat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
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
fun ChocieQuestions(){
    var text by remember { mutableStateOf("") }

    BasicTextField(
        value = text,
        onValueChange = { text = it },
        modifier = Modifier
            .width(124.dp)  // 设置宽度为24dp
            .height(IntrinsicSize.Min)  // 高度根据内容自适应
            .fillMaxWidth(),  // 如果需要在特定布局中，可以替换为需要的修饰符
        singleLine = true,
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        ),
        cursorBrush = SolidColor(Color.White),
        decorationBox = { innerTextField ->
            // 通过decorationBox实现内容居中
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                innerTextField()
            }
        }
    )
}