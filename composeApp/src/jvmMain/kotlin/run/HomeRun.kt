package run

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeRun() {
    Box(modifier = Modifier.fillMaxSize()){
        Column {
            // 顶部设置
            RunSetting()
            // 主运行屏幕内容（表格等）
            RunScreen()
        }
    }
}
