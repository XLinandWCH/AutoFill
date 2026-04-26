import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

repositories {
    // 阿里云镜像仓库
    maven { url = uri("https://maven.aliyun.com/repository/public/") }
    maven { url = uri("https://maven.aliyun.com/repository/central/") }
    maven { url = uri("https://maven.aliyun.com/repository/google/") }

    // 保留官方仓库作为后备
    mavenCentral()
    google()
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(projects.shared)

            // Jsoup：用于解析 HTML 网页内容
            implementation("org.jsoup:jsoup:1.15.4")

            // Ktor Client：Kotlin 官方出品，用于发送网络请求（类似 HttpClient）
            implementation("io.ktor:ktor-client-core:2.3.0")
            implementation("io.ktor:ktor-client-cio:2.3.0") // 使用 CIO 引擎
            implementation("org.slf4j:slf4j-simple:2.0.7") // 添加 ktor 依赖

            // 协程支持
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

            // Playwright 核心库
            implementation("com.microsoft.playwright:playwright:1.45.0")

            // Markdown库核心渲染库
            implementation("com.mikepenz:multiplatform-markdown-renderer:0.40.2")

            //  Markdown 里有图片，建议配合 Coil3 使用（KMP 官方推荐的图片库）
            implementation("com.mikepenz:multiplatform-markdown-renderer-coil3:0.40.2")

            // Material 3 适配库 (必须添加这个，否则找不到 markdownColor() 等方法)
            implementation("com.mikepenz:multiplatform-markdown-renderer-m3:0.40.2")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)


        }
    }
}


compose.desktop {
    application {
        mainClass = "org.example.project.MainKt"

        nativeDistributions {
            // 在这里加上 TargetFormat.Zip
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "AutoFill"
            packageVersion = "1.0.4"

            // --- 在这里配置图标 ---

            linux {
                iconFile.set(project.file("../icons/AutoFill.png"))
                shortcut = true // 添加快捷方式
            }
            windows {
                iconFile.set(project.file("../icons/AutoFill.ico"))
                // 如果你想在 Windows 安装菜单里显示特定的图标，也可以在这里设置
                menuGroup = "MyKMPApp"
                shortcut = true // 添加快捷方式
            }
            macOS {
                iconFile.set(project.file("../icons/AutoFill.icns"))
                bundleID = "com.example.project"
            }
        }
    }
}
