package content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import wjx.wjxCrawler

object GlobalData {
    // 存储抓取到的数据，放到全局防止切页丢失
    val surveyData = mutableStateOf<Map<String, Any>?>(null)
}

@Composable
fun Home(){
    val scope = rememberCoroutineScope()
    // 引用全局数据
    val surveyData = GlobalData.surveyData

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            HomeSearch(onSearch = { url ->
                scope.launch {
                    // 调用抓取函数，获取字典
                    val data = wjxCrawler(url)
                    // 更新状态
                    surveyData.value = data
                }
            })

            // 将获取到的字典传递给 HomeContent
            surveyData.value?.let { data ->
                HomeContent(
                    surveyData = data.toMutableMap()  // 转换为 MutableMap
                )
            }


        }
    }
}