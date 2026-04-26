package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.darkColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() {

    // 初始化 Playwright 环境
    other.initPlaywrightSystemProperties()
    
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "AutoFill",
            state = WindowState(height = 640.dp, width = 960.dp),
            icon = painterResource("drawables/AutoFill.png")


        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF222222))
            ) {

                App()
            }



        }
    }
}