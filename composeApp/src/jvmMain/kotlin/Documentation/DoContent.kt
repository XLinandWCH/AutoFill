package Documentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography

// 注意：确保导入的是 m3 或者是你项目对应的版本

@Composable
fun MarkdownContent(content: String) {

    val scrollState = rememberScrollState()

    // 切换内容时重置滚动位置
    androidx.compose.runtime.LaunchedEffect(content) {
        scrollState.scrollTo(0)
    }

    Markdown(
        content = content,
        // 显式提供默认配置
        colors = markdownColor(Color.White),
        typography = markdownTypography(),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    )
}