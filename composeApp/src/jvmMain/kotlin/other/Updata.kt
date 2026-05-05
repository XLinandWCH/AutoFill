package other

import java.io.File

/**
 * 启动时初始化 Playwright 系统属性
 *
 * 设置驱动提取目录和浏览器路径，确保打包后的程序
 * 也能正确提取和定位 Playwright 驱动及浏览器内核。
 */
fun initPlaywrightSystemProperties() {
    try {
        // 1. 设置驱动提取目录（防止系统临时目录权限问题）
        val appDataDir = System.getenv("APPDATA") ?: System.getProperty("user.home")
        val driverDir = File(appDataDir, "AutoFill/playwright-driver")
        if (!driverDir.exists()) driverDir.mkdirs()
        System.setProperty("playwright.driver.tmpdir", driverDir.absolutePath)

        // 2. 确保浏览器存储目录存在
        val browsersDir = File(BrowserManager.BROWSERS_PATH)
        if (!browsersDir.exists()) browsersDir.mkdirs()

        println("[AutoFill] Playwright 驱动目录: ${driverDir.absolutePath}")
        println("[AutoFill] 浏览器内核目录: ${BrowserManager.BROWSERS_PATH}")
    } catch (e: Exception) {
        println("[AutoFill] 初始化 Playwright 属性失败: ${e.message}")
    }
}
