package SolutionFormat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun FillBlankQuestions(questionIndex: Int, optionIndex: Int) {
    val rawText = AnswerDictionary.getAnswer(questionIndex, optionIndex)
    var textFieldValue by remember(questionIndex, optionIndex) {
        mutableStateOf(TextFieldValue(rawText))
    }

    // Capture density for DP/Pixel conversion
    val density = LocalDensity.current
    val editorHeightDp = 300.dp
    val editorHeightPx = with(density) { editorHeightDp.toPx() }

    LaunchedEffect(rawText) {
        if (rawText != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(text = rawText)
        }
    }

    val linesCount = textFieldValue.text.split("\n").size.coerceAtLeast(1)
    val fontSize = 16.sp
    val lineHeight = 24.sp

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier
        .fillMaxWidth()
        .height(editorHeightDp)
        .padding(8.dp)
        .background(Color(0xFF2B2B2B))
    ) {
        Row(modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
        ) {
            // Line numbers synchronized with text
            Column(modifier = Modifier
                .width(44.dp)
                .padding(vertical = 4.dp)
            ) {
                for (i in 1..linesCount) {
                    Text(
                        text = i.toString(),
                        color = Color(0xFF888888),
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Box(modifier = Modifier
                .width(1.dp)
                .background(Color(0xFF444444))
                .height(IntrinsicSize.Max) // Matches current Row height
            )

            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    AnswerDictionary.updateAnswer(questionIndex, optionIndex, newValue.text)
                    AnswerDictionary.printDictionary()
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = fontSize,
                    lineHeight = lineHeight,
                    fontFamily = FontFamily.Monospace
                ),
                cursorBrush = SolidColor(Color.White),
                onTextLayout = { layoutResult ->
                    // Auto-follow cursor logic
                    val cursorOffset = textFieldValue.selection.max
                    val lineIndex = layoutResult.getLineForOffset(cursorOffset)
                    val lineTop = layoutResult.getLineTop(lineIndex)
                    val lineBottom = layoutResult.getLineBottom(lineIndex)

                    val currentScroll = scrollState.value

                    if (lineTop < currentScroll) {
                        coroutineScope.launch {
                            scrollState.scrollTo(lineTop.toInt())
                        }
                    } else if (lineBottom > currentScroll + editorHeightPx) {
                        coroutineScope.launch {
                            scrollState.scrollTo((lineBottom - editorHeightPx + 40).toInt()) // +40 for buffer
                        }
                    }
                }
            )
        }
    }
}
