package content

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeSearch(onSearch : (String) -> Unit){
    // 0xFF343434
    // 创建搜索文本状态
    val searchText = remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current



    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF343434)
        ),
        shape = RoundedCornerShape(0.dp)
    ) {
        // 添加顶部边框线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF4B4A4A))  // 深灰色线
        ) {
            // 这里只需要一个空的 Box 来画线
        }

        val interactionSource = remember { MutableInteractionSource() }

        BasicTextField(
            value = searchText.value,
            onValueChange = { searchText.value = it },
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .padding(16.dp)
                .align(androidx.compose.ui.Alignment.CenterHorizontally),
            singleLine = true,
            interactionSource = interactionSource,
            textStyle = LocalTextStyle.current.copy(color = Color.White),
            cursorBrush = SolidColor(Color.White),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onSearch(searchText.value)
                    focusManager.clearFocus()
                }
            ),

            decorationBox = @Composable { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = searchText.value,
                    visualTransformation = VisualTransformation.None,
                    innerTextField = innerTextField,
                    placeholder = {
                        Text(
                            text = "输入关键词搜索",
                            color = Color.White,
                            fontWeight = FontWeight.W300
                        )
                    },
                    label = { Text("搜索...") },
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                onSearch(searchText.value)
                            },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xF52BAC3B),
                            ),
                            modifier = Modifier.height(44.dp).width(78.dp).padding(6.dp)
                        ){
                            Text(
                                text = "确认",
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.W400
                            )
                        }
                    },
                    singleLine = true,
                    enabled = true,
                    isError = false,
                    interactionSource = interactionSource,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xF52BAC3B),
                        cursorColor = Color.White,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                        focusedLabelColor = Color(0xF52BAC3B),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                    ),
                    // 在这里自由控制你想要的内边距
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    container = {
                        OutlinedTextFieldDefaults.Container( // 注意：如果你使用的 Compose 版本较新，这里可能需要改为 Container
                            enabled = true,
                            isError = false,
                            interactionSource = interactionSource,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xF52BAC3B),
                                cursorColor = Color.White,
                                focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                                unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                                focusedLabelColor = Color(0xF52BAC3B),
                                unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                )
            }
        )
    }

}


