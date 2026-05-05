package run

/**
 * 此文件已废弃，浏览器管理功能已统一到 other.BrowserManager。
 * 保留此文件仅为避免编译错误，所有功能请使用 other.BrowserManager。
 */
@Deprecated("请使用 other.BrowserManager", replaceWith = ReplaceWith("other.BrowserManager"))
object BrowserManager {
    val BROWSERS_PATH: String get() = other.BrowserManager.BROWSERS_PATH
}
