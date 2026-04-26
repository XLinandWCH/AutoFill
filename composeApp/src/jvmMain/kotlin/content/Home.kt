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

/**
 * 搜索触发器，用于跨组件调用搜索逻辑
 */
object SearchTrigger {
    var onSearch: ((String) -> Unit)? = null
}

@Composable
fun Home(){
    val scope = rememberCoroutineScope()
    // 引用全局数据
    val surveyData = GlobalData.surveyData

    // 将搜索方法注册到触发器
    SearchTrigger.onSearch = { url ->
        scope.launch {
            val data = wjxCrawler(url)
            surveyData.value = data
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            HomeSearch(onSearch = { url ->
                SearchTrigger.onSearch?.invoke(url)
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