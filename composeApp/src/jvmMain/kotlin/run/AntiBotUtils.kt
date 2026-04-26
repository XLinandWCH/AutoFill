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
    fun randomScroll(page: Page): String {
        return try {
            val isUp = (1..10).random() > 8 // 20% 概率向上
            val scrollAmount = (150..400).random() * if (isUp) -1 else 1
            page.evaluate("window.scrollBy({top: $scrollAmount, behavior: 'smooth'})")
            Thread.sleep((600..1200).random().toLong())
            if (isUp) "【浏览】随机向上翻页" else "【浏览】随机向下翻页"
        } catch (e: Exception) { "【浏览】页面滚动异常" }
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
     * 拟人化点击（返回点击描述，包括是否触发了纠错）
     */
    fun humanClick(page: Page, element: com.microsoft.playwright.ElementHandle?, isAntiBot: Boolean): String {
        if (element == null) return "未找到选项"
        var actionDetail = "点击选中"
        
        if (isAntiBot) {
            ensureInView(element)
            // 模拟“纠错”：有 8% 的概率先点一个错误的，再点正确的
            if ((1..100).random() <= 8) {
                try {
                    val siblings = page.querySelectorAll(".ui-radio, .ui-checkbox, .rate-off").filter { it != element }
                    if (siblings.isNotEmpty()) {
                        val mistakeTarget = siblings.random()
                        mistakeTarget.click()
                        actionDetail = "【纠错】先错选了其他项，正在重选..."
                        Thread.sleep((1000..2200).random().toLong()) 
                    }
                } catch (e: Exception) {}
            }
        }
        
        element.click()
        if (isAntiBot) breatheDelay()
        return actionDetail
    }

    /**
     * 拟人化输入
     */
    fun humanType(page: Page, selector: String, rawText: String, isAntiBot: Boolean): String {
        if (rawText.isBlank()) return "跳过空文本"
        
        val lines = rawText.split("\n").filter { it.isNotBlank() }
        if (lines.isEmpty()) return "无效文本池"
        val textToFill = lines.random()

        val element = page.querySelector(selector) ?: return "找不到输入框"
        ensureInView(element)
        element.focus()
        
        if (isAntiBot) {
            for (char in textToFill) {
                val delay = (100..300).random().toDouble()
                page.keyboard().type(char.toString(), Keyboard.TypeOptions().setDelay(delay))
            }
        } else {
            element.fill(textToFill)
        }
        return "填写内容: \"$textToFill\""
    }

    fun breatheDelay() {
        Thread.sleep((1000L..3000L).random())
    }
}
