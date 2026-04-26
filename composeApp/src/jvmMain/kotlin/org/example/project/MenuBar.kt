package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import other.HomeOther

@Composable
fun MenuBar(onNavigate: (String) -> Unit){

    var showHomeOther by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().height(45.dp).background(color = Color(0xFF343434))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                modifier = Modifier.fillMaxHeight(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MenuButton(name = "主页", onClick = { onNavigate("主页") })
                MenuButton(name = "运行", onClick = { onNavigate("运行") })
                MenuButton(name = "设置", onClick = { onNavigate("设置") })
                MenuButton(name = "文档", onClick = { onNavigate("文档") })
            }

            Row(
                modifier = Modifier.fillMaxHeight(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = { showHomeOther = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Icon(
                        painter = painterResource("icon/packUP.svg"),
                        contentDescription = "收起"
                    )
                }
            }
        }
    }

    // ✅ 在 Composable 作用域内调用 HomeOther
    if (showHomeOther) {
        HomeOther(
            onDismiss = { showHomeOther = false },

        )
    }

}

@Composable
fun MenuButton(name: String, onClick: () -> Unit){
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
        ),
        shape = RoundedCornerShape(2.dp),
        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        ){
        Text(
            text = name,
            color = Color.White,
            fontWeight = FontWeight.W400,
        )
    }
}