import com.microsoft.playwright.Playwright
import com.microsoft.playwright.CLI

fun main() {
    println("Start")
    try {
        CLI.main(arrayOf("install", "chromium"))
        println("Success")
    } catch(e: Exception) {
        e.printStackTrace()
    }
}
