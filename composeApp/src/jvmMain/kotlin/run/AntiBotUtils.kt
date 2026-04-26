package run

import com.microsoft.playwright.Keyboard
import com.microsoft.playwright.Page


/**
 * 拟人化行为工具类
 */
object AntiBotUtils {

    /**
     * 随机滚动页面，模拟人类阅读
     */
    fun randomScroll(page: Page) {
        try {
            val scrollAmount = (150..500).random() * if ((0..1).random() == 0) 1 else -1
            page.evaluate("window.scrollBy({top: $scrollAmount, behavior: 'smooth'})")
            Thread.sleep((800..2000).random().toLong())
        } catch (e: Exception) {}
    }

    /**
     * 拟人化点击（支持纠错逻辑）
     */
    fun humanClick(page: Page, element: com.microsoft.playwright.ElementHandle?, isAntiBot: Boolean) {
        if (element == null) return
        
        if (isAntiBot) {
            // 模拟“纠错”：有 10% 的概率先点一个错误的，再点正确的
            if ((1..100).random() <= 10) {
                try {
                    val container = element.evaluateHandle("el => el.closest('.ui-controlgroup, tr, .div_question')")
                    val siblings = page.querySelectorAll(".ui-radio, .ui-checkbox, .rate-off").filter { it != element }
                    if (siblings.isNotEmpty()) {
                        val mistakeTarget = siblings.random()
                        mistakeTarget.scrollIntoViewIfNeeded()
                        mistakeTarget.click()
                        Thread.sleep((1200..2500).random().toLong()) // 停留一下，发现点错了
                    }
                } catch (e: Exception) {}
            }
            element.scrollIntoViewIfNeeded()
        }
        
        element.click()
        if (isAntiBot) breatheDelay()
    }

    /**
     * 拟人化输入（采用 Playwright 官方推荐的 type 方法带延迟）
     */
    fun humanType(page: Page, selector: String, text: String, isAntiBot: Boolean) {
        if (text.isBlank()) return
        
        val element = page.querySelector(selector) ?: return
        element.scrollIntoViewIfNeeded()
        element.focus()

        if (isAntiBot) {
            for (char in text) {
                val delay = (1000..3000).random().toDouble()
                page.keyboard().type(
                    char.toString(),
                    Keyboard.TypeOptions().setDelay(delay)
                )
            }
        } else {
            element.fill(text)
        }
    }

    /**
     * 选项间的呼吸等待 (1.0s ~ 3.5s)
     */
    fun breatheDelay() {
        Thread.sleep((1000L..3500L).random())
    }
}
