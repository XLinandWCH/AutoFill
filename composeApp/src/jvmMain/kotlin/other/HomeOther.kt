package other

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun HomeOther(
    onDismiss: () -> Unit,

) {

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false, // 防止误触关闭
            usePlatformInsets = true,
            scrimColor = Color(0x601A1A1A)

        ),

    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            elevation = 8.dp,
            modifier = Modifier.width(320.dp),
            color = Color(0xBF131313),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                // horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "控制面板",
                    style = MaterialTheme.typography.h6,
                    color = Color.White,
                )
                Column {

                    OtherButton("保存")

                    OtherButton("导入")

                    OtherButton("检查更新")

                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(0.3f),
                        shape = RoundedCornerShape(6.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xEED91E1E),
                        ),

                    ) {
                        Text("关闭", color = Color.White)
                    }
                }
            }

        }
    }
}

@Composable
fun OtherButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xF52BAC3B),
        )

    ) {
        Text(text = text, color = Color.White)
    }
}