package other

import com.microsoft.playwright.Playwright
import java.io.File
import kotlin.concurrent.thread

/**
 * 处理 Playwright 运行环境的初始化与修复
 */
fun fixEnvironment(onStatusUpdate: (String) -> Unit) {
    thread {
        try {
            onStatusUpdate("正在检查驱动环境...")
            
            // 1. 尝试强制指定 Driver 提取目录，防止系统临时目录权限问题
            val appDataDir = System.getenv("APPDATA") ?: System.getProperty("user.home")
            val driverDir = File(appDataDir, "AutoFill/playwright-driver")
            if (!driverDir.exists()) driverDir.mkdirs()
            System.setProperty("playwright.driver.tmpdir", driverDir.absolutePath)

            onStatusUpdate("正在下载/校验浏览器核心 (约300MB)，请勿关闭程序...")
            
            // 2. 运行安装命令
            // CLI 用法: java -cp <classpath> com.microsoft.playwright.CLI install chromium
            // 在程序内部，我们可以直接调用 Playwright 对象的安装方法
            val process = ProcessBuilder(
                "cmd.exe", "/c", "set PLAYWRIGHT_DRIVER_TMPDIR=${driverDir.absolutePath} && mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args=\"install chromium\""
            ).inheritIO().start()
            
            // 或者更简单的做法，利用 Playwright 自带的静态方法触发安装
            // 注意：某些版本可能需要通过 ProcessBuilder 调用内置的 CLI
            Playwright.create().use { pw ->
                pw.chromium().launch() // 如果没有浏览器，这行会抛出包含安装说明的异常
            }

            onStatusUpdate("环境修复完成！现在可以开始运行了。")
        } catch (e: Exception) {
            val errorMsg = e.message ?: "未知错误"
            if (errorMsg.contains("playwright install")) {
                onStatusUpdate("缺少浏览器核心，请手动在终端运行: npx playwright install chromium")
            } else {
                onStatusUpdate("修复失败: $errorMsg")
            }
            e.printStackTrace()
        }
    }
}

/**
 * 启动时初始化系统属性
 */
fun initPlaywrightSystemProperties() {
    try {
        val appDataDir = System.getenv("APPDATA") ?: System.getProperty("user.home")
        val driverDir = File(appDataDir, "AutoFill/playwright-driver")
        if (!driverDir.exists()) driverDir.mkdirs()
        // 关键：强制 Playwright 将驱动释放到我们指定的、有权限的目录
        System.setProperty("playwright.driver.tmpdir", driverDir.absolutePath)
    } catch (e: Exception) {}
}
