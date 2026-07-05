package other

import com.microsoft.playwright.CLI
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.util.Scanner
import kotlin.concurrent.thread

/**
 * Browser kernel manager (unified version)
 *
 * Handles detection, download, and lifecycle management of the Chromium browser kernel.
 * The kernel is stored in the user's home directory under .autofill/browsers so it can
 * be cleaned up alongside the application.
 *
 * Download strategy (each falls through to the next on failure):
 *   1. ProcessBuilder subprocess — sets PLAYWRIGHT_BROWSERS_PATH via environment()
 *   2. npx playwright install — works if Node.js is on the system PATH
 *   3. CLI.main() in-process  — last resort; browser may land in the system default path
 */
object BrowserManager {

    /** Where the Chromium kernel is stored on disk. */
    val BROWSERS_PATH: String by lazy {
        val path = resolveBrowsersPath()
        if (!path.exists()) path.mkdirs()
        path.absolutePath
    }

    /**
     * Returns the directory where the Chromium browser binary is stored.
     *
     * On Windows we use C:\Users\Public\AutoFill\browsers instead of the
     * user's home directory because:
     *  1. user.home may contain non-ASCII characters (Chinese username) which
     *     cause Playwright to fail when pointing browsers at that path.
     *  2. C:\Users\Public is always ASCII, always writable by every user,
     *     and short enough to stay under the Windows MAX_PATH (260 chars) limit.
     *
     * If the old path (~/.autofill/browsers) already has a Chromium binary,
     * it is migrated (renamed) to the new location automatically.
     */
    private fun resolveBrowsersPath(): File {
        val os = System.getProperty("os.name", "").lowercase()
        val newPath = if (os.contains("win")) {
            val publicDir = System.getenv("PUBLIC") ?: "C:\\Users\\Public"
            File(publicDir, "AutoFill\\browsers")
        } else {
            File(System.getProperty("user.home"), ".autofill/browsers")
        }

        // Migrate from old location (~/.autofill/browsers) if the new location
        // does not yet exist but the old one does, so existing users don't have
        // to re-download Chromium after updating the app.
        if (!newPath.exists()) {
            val oldPath = File(System.getProperty("user.home"), ".autofill/browsers")
            if (oldPath.exists() && oldPath.absolutePath != newPath.absolutePath) {
                oldPath.renameTo(newPath)
            }
        }

        return newPath
    }

    // ── Observable state ──────────────────────────────────────────────────────
    val downloadLogs  = MutableStateFlow<List<String>>(emptyList())
    val isDownloading = MutableStateFlow(false)
    val isBrowserReady = MutableStateFlow(false)
    val showDialog    = MutableStateFlow(false)

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    /** Called at startup: check for an existing kernel and download if missing. */
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

            showDialog.value = true
            addLog("❌ 未找到 Chromium 内核，准备开始自动下载...")
            addLog("下载大小约 150~200MB，请确保网络畅通，耐心等待。")
            downloadChromium()
        }
    }

    /** Called from the retry button or settings page. */
    fun retryDownload() {
        if (isDownloading.value) return
        thread {
            isDownloading.value = true
            isBrowserReady.value = false
            addLog("── 手动触发重新下载 ──")
            downloadChromium()
        }
    }

    /** File-system check: does a chromium-* directory exist under BROWSERS_PATH? */
    fun checkBrowserExists(): Boolean {
        val root = File(BROWSERS_PATH)
        if (!root.exists()) return false
        return root.list()?.any { it.startsWith("chromium-") || it.startsWith("chromium_") } == true
    }

    /**
     * Returns the browsers path that actually contains a kernel.
     * Prefers the custom BROWSERS_PATH; falls back to the Playwright system default.
     */
    fun getEffectiveBrowsersPath(): String {
        if (checkBrowserExists()) return BROWSERS_PATH
        val defaultPath = getDefaultPlaywrightPath()
        if (defaultPath != null && File(defaultPath).list()?.any {
                it.startsWith("chromium-") || it.startsWith("chromium_")
            } == true) return defaultPath
        return BROWSERS_PATH
    }

    fun addLog(message: String) {
        val current = downloadLogs.value.toMutableList()
        current.add(message)
        if (current.size > 500) current.removeAt(0)
        downloadLogs.value = current
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Download orchestration
    // ─────────────────────────────────────────────────────────────────────────

    private fun downloadChromium() {
        try {
            // Build an effective classpath (explicit JARs, not a shell wildcard)
            val sysCp = System.getProperty("java.class.path") ?: ""
            val effectiveCp = if (sysCp.isNotBlank() && sysCp != ".") sysCp
                              else getPackagedClasspath() ?: sysCp

            // Strategy 1 — subprocess with bundled java
            val javaBin = findJavaExecutable()
            if (javaBin != null) {
                addLog("正在通过子进程下载 Chromium (java: $javaBin)...")
                if (downloadViaProcessBuilder(javaBin, effectiveCp)) return
                addLog("⚠️ 子进程方式失败，尝试备用方式...")
            } else {
                addLog("⚠️ 未找到可用的 java 可执行文件，跳过子进程方式。")
            }

            // Strategy 2 — npx (requires Node.js on PATH)
            if (downloadViaNpx()) return

            // Strategy 3 — in-process CLI.main() (last resort)
            downloadViaInProcessCLI()

        } catch (e: Exception) {
            addLog("❌ 下载过程发生异常: ${e.message}")
        } finally {
            isDownloading.value = false
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Strategy 1: ProcessBuilder subprocess
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Launches a child JVM process to run `com.microsoft.playwright.CLI install chromium`.
     * Environment variables are set via ProcessBuilder.environment() — fully reliable.
     * Returns true if the kernel was successfully downloaded and verified.
     */
    private fun downloadViaProcessBuilder(javaBin: String, classpath: String): Boolean {
        return try {
            val pb = ProcessBuilder(
                javaBin, "-cp", classpath,
                "com.microsoft.playwright.CLI", "install", "chromium"
            )
            pb.environment()["PLAYWRIGHT_BROWSERS_PATH"] = BROWSERS_PATH
            pb.environment()["PLAYWRIGHT_DOWNLOAD_HOST"] = "https://npmmirror.com/mirrors/playwright"
            pb.environment().remove("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD") // must not be set during download
            pb.redirectErrorStream(true)

            val process = pb.start()
            Scanner(process.inputStream).use { sc ->
                while (sc.hasNextLine()) {
                    val line = sc.nextLine()
                    if (line.isNotBlank()) addLog(line)
                }
            }

            val exitCode = process.waitFor()
            addLog("子进程退出码: $exitCode，正在验证...")

            verifyAndFinalize("子进程")
        } catch (e: Exception) {
            addLog("子进程方式失败: ${e.message}")
            false
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Strategy 2: npx playwright install
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Runs `npx playwright install chromium` via the system shell.
     * Requires Node.js / npm to be installed on the user's machine.
     * Returns true if the kernel was successfully downloaded and verified.
     */
    private fun downloadViaNpx(): Boolean {
        return try {
            addLog("正在尝试通过 npx 安装 Chromium...")
            val os = System.getProperty("os.name", "").lowercase()
            val cmd = if (os.contains("win"))
                listOf("cmd", "/c", "npx playwright install chromium")
            else
                listOf("sh", "-c", "npx playwright install chromium")

            val pb = ProcessBuilder(cmd)
            pb.environment()["PLAYWRIGHT_BROWSERS_PATH"] = BROWSERS_PATH
            pb.environment()["PLAYWRIGHT_DOWNLOAD_HOST"] = "https://npmmirror.com/mirrors/playwright"
            pb.environment().remove("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD") // must not be set during download
            pb.redirectErrorStream(true)

            val process = pb.start()
            Scanner(process.inputStream).use { sc ->
                while (sc.hasNextLine()) {
                    val line = sc.nextLine()
                    if (line.isNotBlank()) addLog(line)
                }
            }

            val exitCode = process.waitFor()
            addLog("npx 退出码: $exitCode，正在验证...")

            verifyAndFinalize("npx")
        } catch (e: Exception) {
            addLog("npx 方式失败: ${e.message}（可能未安装 Node.js）")
            false
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Strategy 3: in-process CLI.main()
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Calls CLI.main() directly inside the running JVM.
     * Cannot set PLAYWRIGHT_BROWSERS_PATH reliably — Chromium may land in the system default.
     *
     * Before calling CLI.main(), verifies that the embedded driver-bundle.zip is accessible.
     * If it is not found (e.g. stripped by ProGuard), a clear diagnostic message is shown.
     */
    private fun downloadViaInProcessCLI() {
        addLog("正在以进程内方式安装 Chromium（最后手段）...")
        try {
            // Guard: check the embedded Playwright driver bundle is present
            val bundle = CLI::class.java.getResourceAsStream("/driver-bundle.zip")
            if (bundle == null) {
                addLog("❌ 致命错误：安装包中找不到 Playwright driver bundle (/driver-bundle.zip)。")
                addLog("这通常是 ProGuard 裁剪了 Playwright 内部资源导致的。")
                addLog("解决方案：在 proguard-rules.pro 中确保 `-keep class com.microsoft.playwright.impl.** { *; }` 规则存在，重新打包后重试。")
                addLog("或手动运行: npx playwright install chromium")
                return
            }
            bundle.close()

            // Redirect stdout/stderr so we can capture CLI output into the log
            val originalOut = System.out
            val originalErr = System.err
            val capture = object : java.io.ByteArrayOutputStream() {
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
            val ps = java.io.PrintStream(capture, true, "UTF-8")
            System.setOut(ps)
            System.setErr(ps)

            try {
                CLI.main(arrayOf("install", "chromium"))
            } finally {
                System.setOut(originalOut)
                System.setErr(originalErr)
            }

            addLog("下载命令执行完毕，正在验证...")
            verifyAndFinalize("进程内 CLI")

        } catch (e: Exception) {
            addLog("❌ 进程内 CLI 下载失败: ${e.message}")
            addLog("请手动在终端运行: npx playwright install chromium")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Checks whether a kernel is now present (either in BROWSERS_PATH or the system default).
     * Sets [isBrowserReady] and logs the result. Returns true on success.
     */
    private fun verifyAndFinalize(source: String): Boolean {
        if (checkBrowserExists()) {
            addLog("✅ Chromium 内核下载成功！（来源: $source，路径: $BROWSERS_PATH）")
            isBrowserReady.value = true
            return true
        }
        // Also accept the Playwright system default location
        val defaultPath = getDefaultPlaywrightPath()
        if (defaultPath != null && File(defaultPath).list()?.any {
                it.startsWith("chromium-") || it.startsWith("chromium_")
            } == true) {
            addLog("✅ 内核已下载到默认位置: $defaultPath（来源: $source）")
            isBrowserReady.value = true
            return true
        }
        addLog("❌ $source 方式完成，但未找到 Chromium 内核，继续尝试其他方式...")
        return false
    }

    /**
     * Finds the `java` / `java.exe` binary using multiple strategies:
     *
     * 1. Standard `java.home/bin/java[.exe]`
     * 2. Walk the `java.home` directory tree up to 3 levels (covers unusual JRE layouts)
     * 3. `where java` (Windows) / `which java` (Unix) — searches the system PATH
     */
    private fun findJavaExecutable(): String? {
        val os = System.getProperty("os.name", "").lowercase()
        val exeName = if (os.contains("win")) "java.exe" else "java"
        val javaHome = File(System.getProperty("java.home", ""))

        // 1. Standard location
        File(javaHome, "bin/$exeName").takeIf { it.exists() }?.let {
            addLog("java 路径: ${it.absolutePath}")
            return it.absolutePath
        }

        // 2. Walk the java.home tree (handles custom jlink layouts)
        addLog("标准路径未找到 $exeName，正在搜索 ${javaHome.absolutePath}...")
        javaHome.walkTopDown().maxDepth(3)
            .firstOrNull { it.isFile && it.name.equals(exeName, ignoreCase = true) }
            ?.let {
                addLog("java 路径（搜索发现）: ${it.absolutePath}")
                return it.absolutePath
            }

        // 3. System PATH
        try {
            val cmd = if (os.contains("win")) listOf("where", "java") else listOf("which", "java")
            val proc = ProcessBuilder(cmd).start()
            val result = proc.inputStream.bufferedReader().readLine()?.trim()
            proc.waitFor()
            if (!result.isNullOrBlank() && File(result).exists()) {
                addLog("java 路径（PATH）: $result")
                return result
            }
        } catch (_: Exception) {}

        addLog("⚠️ 未找到 java 可执行文件（java.home=${javaHome.absolutePath}）")
        return null
    }

    /**
     * Builds an explicit classpath string from all JARs in the packaged `app` directory.
     *
     * ProcessBuilder does NOT expand shell wildcards (like `app\*`), so we enumerate
     * every .jar file and join them with the platform path separator.
     */
    private fun getPackagedClasspath(): String? {
        return try {
            val jarUri = BrowserManager::class.java.protectionDomain.codeSource?.location?.toURI()
            val jarFile = jarUri?.let { File(it) } ?: return null
            if (!jarFile.exists()) return null

            if (jarFile.isFile) {
                val appDir = jarFile.parentFile ?: return null
                val jars = appDir.listFiles { f -> f.isFile && f.name.endsWith(".jar") }
                if (!jars.isNullOrEmpty()) jars.joinToString(File.pathSeparator) { it.absolutePath }
                else jarFile.absolutePath          // fallback: single JAR
            } else {
                jarFile.absolutePath               // development: directory on classpath
            }
        } catch (e: Exception) {
            addLog("获取打包 Classpath 失败: ${e.message}")
            null
        }
    }

    /** Returns the Playwright default browser cache path for the current OS. */
    private fun getDefaultPlaywrightPath(): String? {
        val os = System.getProperty("os.name", "").lowercase()
        return when {
            os.contains("win") -> System.getenv("LOCALAPPDATA")?.let { "$it\\ms-playwright" }
            os.contains("mac") -> "${System.getProperty("user.home")}/Library/Caches/ms-playwright"
            else               -> "${System.getProperty("user.home")}/.cache/ms-playwright"
        }
    }
}
