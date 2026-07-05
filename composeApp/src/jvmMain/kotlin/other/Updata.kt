package other

import java.io.File

/**
 * Called once at JVM startup (before any Playwright API is touched).
 *
 * Playwright reads PLAYWRIGHT_BROWSERS_PATH **only** from the real OS environment.
 * CreateOptions.setEnv() only affects the browser child-process, not the Node.js
 * driver subprocess. Since we can't launch the app with the env var pre-set (it's
 * a packaged MSI), we inject it directly into the live process environment here,
 * using the correct field name for JVM 17+ on Windows.
 *
 * We also set playwright.driver.tmpdir so the driver ZIP is extracted to a stable,
 * writable location under %APPDATA% rather than the system temp dir (which can have
 * permission or path-length issues on some Windows installations).
 */
fun initPlaywrightSystemProperties() {
    try {
        val appDataDir = System.getenv("APPDATA") ?: System.getProperty("user.home")

        // ── 1. Driver extraction directory ───────────────────────────────────
        // Playwright extracts its bundled Node.js driver here.
        // Using %APPDATA%/AutoFill/playwright-driver avoids temp-dir issues.
        val driverDir = File(appDataDir, "AutoFill/playwright-driver")
        if (!driverDir.exists()) driverDir.mkdirs()
        System.setProperty("playwright.driver.tmpdir", driverDir.absolutePath)

        // ── 2. Browsers path ─────────────────────────────────────────────────
        // Ensure the directory exists before the env var is read.
        val browsersPath = BrowserManager.BROWSERS_PATH
        File(browsersPath).mkdirs()

        // ── 3. Inject env vars into the live process ─────────────────────────
        // Playwright's Driver class reads PLAYWRIGHT_BROWSERS_PATH from the
        // actual OS environment (System.getenv), not from system properties.
        // We must inject it before the first Playwright.create() call.
        //
        // NOTE: Do NOT set PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD here.
        // BrowserManager handles the download via its own subprocess / CLI.main().
        // Setting SKIP=1 in the live process env would be inherited by the
        // download subprocess (Strategy 1) and block CLI.main() (Strategy 3).
        injectEnv("PLAYWRIGHT_BROWSERS_PATH", browsersPath)
        injectEnv("PLAYWRIGHT_DOWNLOAD_HOST", "https://npmmirror.com/mirrors/playwright")

        println("[AutoFill] Driver dir : ${driverDir.absolutePath}")
        println("[AutoFill] Browsers path: $browsersPath")
        println("[AutoFill] Env injected : PLAYWRIGHT_BROWSERS_PATH=$browsersPath")

    } catch (e: Exception) {
        println("[AutoFill] initPlaywrightSystemProperties failed: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * Injects [key]=[value] into the current process's environment map.
 *
 * On JVM 17+, ProcessEnvironment stores the mutable map under the field
 * "theEnvironment" (not "m" as in JDK 8). We try both field names so the
 * code works across JDK versions.
 *
 * This is the only way to set env vars in a running JVM process; it is an
 * intentional workaround for a well-known limitation of the Java platform.
 */
@Suppress("UNCHECKED_CAST")
private fun injectEnv(key: String, value: String) {
    // The unmodifiable view returned by System.getenv() delegates to the
    // internal ProcessEnvironment. We need the mutable backing map.
    val processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment")

    // JDK 17+: field is "theEnvironment"
    // JDK 8/11: field is "m" (inside the unmodifiable wrapper)
    val fieldNames = listOf("theEnvironment", "m")
    var injected = false

    for (fieldName in fieldNames) {
        try {
            val field = processEnvironmentClass.getDeclaredField(fieldName)
            field.isAccessible = true
            val map = field.get(null) as? MutableMap<String, String> ?: continue
            map[key] = value
            injected = true
            break
        } catch (_: NoSuchFieldException) {
            // try next field name
        }
    }

    if (!injected) {
        // Last resort: try the unmodifiable wrapper's delegate map
        try {
            val envMap = System.getenv()
            val field = envMap.javaClass.getDeclaredField("m")
            field.isAccessible = true
            val map = field.get(envMap) as MutableMap<String, String>
            map[key] = value
        } catch (e: Exception) {
            println("[AutoFill] Could not inject env var $key: ${e.message}")
        }
    }
}
