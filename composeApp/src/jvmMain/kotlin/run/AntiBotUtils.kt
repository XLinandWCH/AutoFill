package run

import com.microsoft.playwright.Keyboard
import com.microsoft.playwright.Page

/**
 * 拟人化行为工具类
 */
object AntiBotUtils {

    /**
     * 随机滚动页面，模拟人类阅读（建议在每题完成后调用）
     */
    fun randomScroll(page: Page) {
        try {
            // 随机选择滚动方向和力度
            val scrollAmount = (100..400).random() * if ((1..10).random() > 7) -1 else 1
            page.evaluate("window.scrollBy({top: $scrollAmount, behavior: 'smooth'})")
            Thread.sleep((500..1500).random().toLong())
        } catch (e: Exception) {}
    }

    /**
     * 确保元素在视口内（答题前必须调用）
     */
    fun ensureInView(element: com.microsoft.playwright.ElementHandle?) {
        try {
            element?.scrollIntoViewIfNeeded()
            Thread.sleep((300..600).random().toLong())
        } catch (e: Exception) {}
    }

    /**
     * 拟人化点击（支持纠错逻辑）
     */
    fun humanClick(page: Page, element: com.microsoft.playwright.ElementHandle?, isAntiBot: Boolean) {
        if (element == null) return
        
        if (isAntiBot) {
            ensureInView(element)
            // 模拟“纠错”：有 8% 的概率先点一个错误的，再点正确的
            if ((1..100).random() <= 8) {
                try {
                    val siblings = page.querySelectorAll(".ui-radio, .ui-checkbox, .rate-off").filter { it != element }
                    if (siblings.isNotEmpty()) {
                        val mistakeTarget = siblings.random()
                        mistakeTarget.click()
                        Thread.sleep((1000..2200).random().toLong()) 
                    }
                } catch (e: Exception) {}
            }
        }
        
        element.click()
        if (isAntiBot) breatheDelay()
    }

    /**
     * 拟人化输入（随机挑选一行填入）
     */
    fun humanType(page: Page, selector: String, rawText: String, isAntiBot: Boolean) {
        if (rawText.isBlank()) return
        
        // --- 核心修改：按行分割并随机选择一个答案 ---
        val lines = rawText.split("\n").filter { it.isNotBlank() }
        if (lines.isEmpty()) return
        val textToFill = lines.random()

        val element = page.querySelector(selector) ?: return
        ensureInView(element)
        element.focus()
        
        if (isAntiBot) {
            // 官方推荐的逐字输入
            for (char in textToFill) {
                val delay = (100..300).random().toDouble() // 适当微调打字速度，1-3秒太慢了，这里设为0.1-0.3秒，你可以根据需要调回
                page.keyboard().type(char.toString(), Keyboard.TypeOptions().setDelay(delay))
            }
        } else {
            element.fill(textToFill)
        }
    }

    /**
     * 选项间的呼吸等待 (1.0s ~ 3.5s)
     */
    fun breatheDelay() {
        Thread.sleep((1000L..3500L).random())
    }
}
