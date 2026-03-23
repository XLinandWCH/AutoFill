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

@Composable
@Preview
fun App() {
    var currentScreen by remember { mutableStateOf("主页") }

    Column(modifier = Modifier.fillMaxSize()){
        MenuBar(onNavigate = { screen ->
            currentScreen = screen
        })

        when(currentScreen) {
            "主页" -> Home()
            "运行" -> HomeRun()
            "设置" -> HomeSetting()
            "文档" -> HomeDocumentation()
        }
    }
}