package run

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeRun() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶部设置（固定高度，不伸展）
            RunSetting()
            // 主运行屏幕内容（占满剩余空间）
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                RunScreen()
            }
        }
    }
}
