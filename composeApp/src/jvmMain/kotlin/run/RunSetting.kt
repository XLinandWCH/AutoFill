package run

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RunSetting(){

    val customSelectionColors = TextSelectionColors(
        handleColor = Color.White,
        backgroundColor = Color(0xC869EF79).copy(alpha = 0.15f)
    )

    CompositionLocalProvider(LocalTextSelectionColors provides customSelectionColors) {
        SelectionContainer{

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
                Box(
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    contentAlignment = Alignment.Center
                ){
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row {
                            Text(
                                text = "标题：",
                                color = Color.White,
                                fontWeight = FontWeight.W300,
                                fontSize = 20.sp,
                            )
                            Spacer(modifier = Modifier.padding(6.dp))

                            Text(
                                text = "百度一下，你就知道",
                                color = Color.White,
                                fontWeight = FontWeight.W300,
                                fontSize = 20.sp,
                                textDecoration = TextDecoration.Underline // 添加下划线
                            )
                        }

                        Row {
                            Text(
                                text = "暂停",
                                color = Color.White

                            )
                        }

                    }

                }
            }

        }
    }

}