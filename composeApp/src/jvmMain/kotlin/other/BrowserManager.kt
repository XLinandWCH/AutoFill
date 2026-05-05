package other

import com.microsoft.playwright.CLI
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.util.Scanner
import kotlin.concurrent.thread

/**
 * 浏览器内核管理器（统一版）
 *
 * 负责 Chromium 内核的检测、下载和生命周期管理。
 * 浏览器内核存放在用户目录下的 .autofill/browsers，
 * 卸载程序时可以一并删除此目录。
 */
object BrowserManager {

    /**
     * 浏览器内核存储路径
     * 使用用户主目录下的 .autofill/browsers，方便随程序一起清理
     */
    val BROWSERS_PATH: String by lazy {
        val path = File(System.getProperty("user.home"), ".autofill/browsers")
        if (!path.exists()) path.mkdirs()
        path.absolutePath
    }

    // ── 可观察状态 ──
    val downloadLogs = MutableStateFlow<List<String>>(emptyList())
    val isDownloading = MutableStateFlow(false)
    val isBrowserReady = MutableStateFlow(false)
    val showDialog = MutableStateFlow(false)

    /**
     * 启动时自动检查，如果缺失则弹出下载对话框
     */
    fun startBackgroundCheckAndDownload() {
        if (isDownloading.value) return

        thread {
            isDownloading.value = true
            addLog("系统启动：正在检查 Chromium 浏览器内核...")
            addLog("内核存储路径: $BROWSERS_PATH")

            if (checkBrowserExists()) {
                addLog("✅ Chromium 内核已就绪，可以正常使用。")
                isBrowserReady.value = true
                isDownloading.value = false
                return@thread
            }

            // 未找到内核，自动弹出对话框
            showDialog.value = true
            addLog("❌ 未找到 Chromium 内核，准备开始自动下载...")
            addLog("下载大小约 150~200MB，请确保网络畅通，耐心等待。")

            downloadChromium()
        }
    }

    /**
     * 手动触发重新下载（从设置页面或重试按钮调用）
     */
    fun retryDownload() {
        if (isDownloading.value) return
        thread {
            isDownloading.value = true
            isBrowserReady.value = false
            addLog("── 手动触发重新下载 ──")
            downloadChromium()
        }
    }

    /**
     * 检查 Chromium 内核是否已存在（文件系统检查）
     */
    fun checkBrowserExists(): Boolean {
        val root = File(BROWSERS_PATH)
        if (!root.exists()) return false
        return root.list()?.any { it.startsWith("chromium-") || it.startsWith("chromium_") } == true
    }

    /**
     * 下载 Chromium 内核
     *
     * 策略：
     * 1. 优先使用 ProcessBuilder 启动子进程（可以注入 PLAYWRIGHT_BROWSERS_PATH 环境变量）
     * 2. 如果失败，回退到直接调用 CLI.main()（下载到默认位置也可接受）
     */
    private fun downloadChromium() {
        try {
            val javaBin = File(System.getProperty("java.home"), "bin/java").absolutePath
            val classpath = System.getProperty("java.class.path") ?: ""

            if (classpath.isNotBlank() && classpath != ".") {
                addLog("正在通过子进程下载 Chromium...")
                downloadViaProcessBuilder(javaBin, classpath)
            } else {
                addLog("检测到打包环境，使用内置 CLI 方式下载...")
                downloadViaDirectCLI()
            }
        } catch (e: Exception) {
            addLog("❌ 下载过程发生异常: ${e.message}")
            e.printStackTrace()
        } finally {
            isDownloading.value = false
        }
    }

    /**
     * 方式一：ProcessBuilder 启动子进程下载
     * 优点：可以通过 pb.environment() 设置 PLAYWRIGHT_BROWSERS_PATH
     */
    private fun downloadViaProcessBuilder(javaBin: String, classpath: String) {
        try {
            val pb = ProcessBuilder(
                javaBin, "-cp", classpath,
                "com.microsoft.playwright.CLI", "install", "chromium"
            )
            pb.environment()["PLAYWRIGHT_BROWSERS_PATH"] = BROWSERS_PATH
            // 解决国内下载缓慢或 400 报错的问题，使用淘宝镜像
            pb.environment()["PLAYWRIGHT_DOWNLOAD_HOST"] = "https://npmmirror.com/mirrors/playwright/"
            pb.redirectErrorStream(true)

            val process = pb.start()
            val scanner = Scanner(process.inputStream)

            while (scanner.hasNextLine()) {
                val line = scanner.nextLine()
                if (line.isNotBlank()) {
                    addLog(line)
                }
            }

            val exitCode = process.waitFor()

            if (exitCode == 0 && checkBrowserExists()) {
                addLog("✅ Chromium 内核下载并验证成功！现在可以使用自动化功能了。")
                isBrowserReady.value = true
            } else {
                addLog("⚠️ 子进程退出码: $exitCode，尝试备用方式...")
                downloadViaDirectCLI()
            }
        } catch (e: Exception) {
            addLog("子进程方式失败: ${e.message}")
            addLog("尝试备用方式...")
            downloadViaDirectCLI()
        }
    }

    /**
     * 方式二：直接调用 CLI.main()
     * 在打包环境下兜底使用，内核会下载到 Playwright 默认路径
     */
    private fun downloadViaDirectCLI() {
        try {
            // 尝试通过反射注入环境变量
            try {
                injectEnvVar("PLAYWRIGHT_BROWSERS_PATH", BROWSERS_PATH)
                injectEnvVar("PLAYWRIGHT_DOWNLOAD_HOST", "https://npmmirror.com/mirrors/playwright/")
                addLog("已设置自定义浏览器路径和下载镜像。")
            } catch (e: Exception) {
                addLog("提示：无法设置环境变量，将使用默认位置和官方源。")
            }

            addLog("正在通过内置 CLI 安装 Chromium，请耐心等待...")

            // 捕获 CLI 输出
            val originalOut = System.out
            val originalErr = System.err

            val outputStream = object : java.io.ByteArrayOutputStream() {
                override fun flush() {
                    super.flush()
                    val text = this.toString("UTF-8")
                    if (text.isNotBlank()) {
                        text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                            .forEach { addLog(it) }
                        this.reset()
                    }
                }
            }
            val printStream = java.io.PrintStream(outputStream, true, "UTF-8")

            System.setOut(printStream)
            System.setErr(printStream)

            CLI.main(arrayOf("install", "chromium"))

            System.setOut(originalOut)
            System.setErr(originalErr)

            addLog("下载命令执行完毕，正在验证...")

            if (checkBrowserExists()) {
                addLog("✅ Chromium 内核下载成功！")
                isBrowserReady.value = true
            } else {
                // 检查默认路径
                val defaultPath = getDefaultPlaywrightPath()
                if (defaultPath != null && File(defaultPath).list()?.any {
                        it.startsWith("chromium-") || it.startsWith("chromium_")
                    } == true) {
                    addLog("✅ 内核已下载到默认位置: $defaultPath")
                    isBrowserReady.value = true
                } else {
                    addLog("❌ 下载后仍未找到内核。请检查网络后重试。")
                }
            }
        } catch (e: Exception) {
            addLog("❌ CLI 下载失败: ${e.message}")
            addLog("请尝试手动在终端运行: npx playwright install chromium")
        }
    }

    /**
     * 获取运行时的浏览器路径（供 SurveyRunManager 使用）
     * 优先返回自定义路径，如果自定义路径没有内核则检查默认路径
     */
    fun getEffectiveBrowsersPath(): String {
        if (checkBrowserExists()) return BROWSERS_PATH

        val defaultPath = getDefaultPlaywrightPath()
        if (defaultPath != null && File(defaultPath).list()?.any {
                it.startsWith("chromium-") || it.startsWith("chromium_")
            } == true) {
            return defaultPath
        }

        return BROWSERS_PATH
    }

    /**
     * 获取 Playwright 默认浏览器缓存路径
     */
    private fun getDefaultPlaywrightPath(): String? {
        val os = System.getProperty("os.name", "").lowercase()
        return when {
            os.contains("win") -> {
                val localAppData = System.getenv("LOCALAPPDATA")
                if (localAppData != null) "$localAppData\\ms-playwright" else null
            }
            os.contains("mac") -> "${System.getProperty("user.home")}/Library/Caches/ms-playwright"
            else -> "${System.getProperty("user.home")}/.cache/ms-playwright"
        }
    }

    /**
     * 通过反射注入环境变量到当前进程
     */
    @Suppress("UNCHECKED_CAST")
    private fun injectEnvVar(key: String, value: String) {
        val env = System.getenv()
        val field = env.javaClass.getDeclaredField("m")
        field.isAccessible = true
        val writableEnv = field.get(env) as MutableMap<String, String>
        writableEnv[key] = value
    }

    fun addLog(message: String) {
        val current = downloadLogs.value.toMutableList()
        current.add(message)
        if (current.size > 500) current.removeAt(0)
        downloadLogs.value = current
    }
}
