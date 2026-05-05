package org.example.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import content.Home
import run.HomeRun
import setting.HomeSetting
import Documentation.HomeDocumentation
import other.BrowserStatusBanner
import other.BrowserDownloadDialog

@Composable
@Preview
fun App() {
    var currentScreen by remember { mutableStateOf("主页") }

    Column(modifier = Modifier.fillMaxSize()){
        MenuBar(onNavigate = { screen ->
            currentScreen = screen
        })

        // 全局浏览器状态横幅 —— 在所有页面顶部显示
        BrowserStatusBanner()

        when(currentScreen) {
            "主页" -> Home()
            "运行" -> HomeRun()
            "设置" -> HomeSetting()
            "文档" -> HomeDocumentation()
        }
    }

    // 全局浏览器下载对话框（可从横幅或设置页触发）
    BrowserDownloadDialog()
}