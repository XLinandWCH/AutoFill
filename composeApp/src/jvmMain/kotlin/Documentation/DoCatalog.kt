package Documentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp

@Composable
fun DoCatalog() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF343434)
        ),
        shape = RoundedCornerShape(0.dp)
    ) {
        // 顶部分割线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF4B4A4A))
        )
        Box(
            modifier = Modifier.fillMaxWidth()
        ){
            Row(
                modifier = Modifier.fillMaxWidth(),
                // horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){

                DocTextButton(text = "使用说明")
                DocTextButton(text = "服务协议")
            }
        }
    }

}

@Composable
fun DocTextButton(
    text: String,
){

    TextButton(
        onClick = {   },
        shape = RoundedCornerShape(2.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
        ),
        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
    ){
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }

}