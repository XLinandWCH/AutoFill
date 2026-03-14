import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
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
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")


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
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.example.project"
            packageVersion = "1.0.0"
        }
    }
}
