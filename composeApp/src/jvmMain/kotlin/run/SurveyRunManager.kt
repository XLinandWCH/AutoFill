package run

import SolutionFormat.AnswerDictionary
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.options.LoadState
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * 问卷自动填写运行管理器（全局单例）
 *
 * 管理多线程问卷填写任务的生命周期，包括启动、停止，
 * 并维护运行状态、进度统计和任务日志。
 */
object SurveyRunManager {

    // ─── 运行状态枚举 ───────────────────────────────────────────
    enum class RunState {
        IDLE,       // 空闲，可以启动
        RUNNING,    // 运行中
        STOPPING    // 停止中（等待线程安全退出）
    }

    // ─── 任务日志条目 ───────────────────────────────────────────
    data class TaskLogEntry(
        val id: Int,              // 内部自增 ID，用于显示
        val taskId: Int,          // 业务任务编号
        val threadName: String,   // 执行线程名称
        var status: String,       // 状态文字
        var taskProgress: String, // 进度文字
        var detail: String = ""   // 详情
    )

    // ─── 状态 ──────────────────────────────────────────────────
    /** 当前运行状态 */
    val runState = mutableStateOf(RunState.IDLE)

    /** 问卷标题（由 HomeContent 设置） */
    val surveyTitle = mutableStateOf("")

    /** 问卷链接（由 HomeContent 设置） */
    val surveyUrl = mutableStateOf("")

    // ─── 设置项（由 HomeSetting 读写） ──────────────────────────
    /** 并发线程数 */
    val threadCount = mutableStateOf(3)

    /** 目标填写总数 */
    val totalTarget = mutableStateOf(10)

    /** 是否开启反爬虫机制 */
    val isAntiBotEnabled = mutableStateOf(true)

    /** 是否开启无头模式（不显示浏览器窗口） */
    val isHeadlessEnabled = mutableStateOf(true)

    // ─── 统计计数 ──────────────────────────────────────────────
    /** 已完成数量（成功 + 失败） */
    val completedCount = mutableStateOf(0)

    /** 成功数量（线程安全） */
    val successCount = AtomicInteger(0)

    /** 失败数量（线程安全） */
    val failCount = AtomicInteger(0)

    // ─── 任务日志 ──────────────────────────────────────────────
    /** 可观察的任务日志列表，用于 UI 实时刷新 */
    val taskLogs = mutableStateListOf<TaskLogEntry>()

    // ─── 内部控制 ──────────────────────────────────────────────
    private val stopRequested = AtomicBoolean(false)
    private var executor: java.util.concurrent.ExecutorService? = null

    // ─── 操作方法 ──────────────────────────────────────────────

    /**
     * 启动问卷自动填写任务
     */
    fun start() {
        if (runState.value != RunState.IDLE) return

        val url = surveyUrl.value
        if (url.isBlank()) {
            addLog(TaskLogEntry(1, 0, "主线程", "失败", "0/0", "未加载问卷链接"))
            return
        }

        // 重置统计
        successCount.set(0)
        failCount.set(0)
        completedCount.value = 0
        taskLogs.clear()
        stopRequested.set(false)

        runState.value = RunState.RUNNING

        val threads = threadCount.value.coerceIn(1, 10)
        val target = totalTarget.value.coerceAtLeast(1)
        val headless = isHeadlessEnabled.value
        val antiBot = isAntiBotEnabled.value
        val taskCounter = AtomicInteger(0)

        executor = Executors.newFixedThreadPool(threads)

        // 启动工作线程
        repeat(threads) { threadIdx ->
            executor?.submit {
                val tName = "线程-${threadIdx + 1}"
                try {
                    Playwright.create().use { pw ->
                        val browser = pw.chromium().launch(
                            BrowserType.LaunchOptions().setHeadless(headless)
                        )

                        while (!stopRequested.get()) {
                            val currentTaskNum = taskCounter.incrementAndGet()
                            if (currentTaskNum > target) break

                            // ─── 立即添加“运行中”日志 ───
                            val logIndex = addLog(
                                TaskLogEntry(
                                    id = 0, // addLog 会分配 id
                                    taskId = currentTaskNum,
                                    threadName = tName,
                                    status = "运行中",
                                    taskProgress = "$currentTaskNum/$target",
                                    detail = "正在打开问卷..."
                                )
                            )

                            try {
                                val context = browser.newContext()
                                val page = context.newPage()
                                page.navigate(url)
                                page.waitForLoadState(LoadState.NETWORKIDLE)

                                updateLog(logIndex, "运行中", "正在填写数据...")
                                
                                // 读取 AnswerDictionary 数据并填写
                                fillSurvey(page, currentTaskNum, tName, antiBot)

                                context.close()
                                val sCount = successCount.incrementAndGet()
                                updateLog(logIndex, "成功", "任务完成", "$sCount/$target")
                                completedCount.value = sCount + failCount.get()
                            } catch (e: Exception) {
                                val fCount = failCount.incrementAndGet()
                                updateLog(logIndex, "失败", "失败: ${e.message?.take(50)}", "${successCount.get()}/$target")
                                completedCount.value = successCount.get() + fCount
                            }

                            if (antiBot && !stopRequested.get()) {
                                Thread.sleep((2000L..4000L).random())
                            }
                        }
                        browser.close()
                    }
                } catch (e: Exception) {
                    addLog(TaskLogEntry(0, 0, tName, "错误", "0/0", "初始化失败: ${e.message?.take(50)}"))
                }

                if (successCount.get() + failCount.get() >= target || stopRequested.get()) {
                    runState.value = RunState.IDLE
                }
            }
        }
    }

    /**
     * 添加日志并返回其在列表中的索引（用于后续更新）
     */
    private fun addLog(entry: TaskLogEntry): Int {
        val newEntry = entry.copy(id = taskLogs.size + 1)
        taskLogs.add(newEntry)
        return taskLogs.size - 1
    }

    /**
     * 更新指定索引的日志条目
     */
    private fun updateLog(index: Int, status: String, detail: String, progress: String? = null) {
        if (index in taskLogs.indices) {
            val old = taskLogs[index]
            // 通过重新赋值触发 Compose 重绘
            taskLogs[index] = old.copy(
                status = status,
                detail = detail,
                taskProgress = progress ?: old.taskProgress
            )
        }
    }

    /**
     * 使用 Playwright Page 自动填写问卷
     */
    private fun fillSurvey(page: com.microsoft.playwright.Page, taskId: Int, threadName: String, antiBot: Boolean) {
        val answers = AnswerDictionary.toPlainList()
        val types = AnswerDictionary.typeMap

        for ((qIdx, answerList) in answers.withIndex()) {
            if (stopRequested.get()) break
            
            val type = types[qIdx] ?: 3
            val qNum = qIdx + 1

            try {
                // 每题作答前的随机思考延时（反爬核心）
                if (antiBot) {
                    Thread.sleep((800L..2000L).random())
                }

                when (type) {
                    3 -> { // 单选题
                        val chosen = weightedRandomIndex(answerList)
                        val radios = page.querySelectorAll("#div$qNum .ui-radio")
                        if (chosen in radios.indices) {
                            radios[chosen].click()
                        }
                    }

                    4 -> { // 多选题
                        val checkboxes = page.querySelectorAll("#div$qNum .ui-checkbox")
                        for ((i, cb) in checkboxes.withIndex()) {
                            val prob = answerList.getOrNull(i)?.toIntOrNull() ?: 50
                            if ((1..100).random() <= prob) {
                                cb.click()
                                if (antiBot) Thread.sleep((100..300).random().toLong())
                            }
                        }
                        // 确保选中
                        val anyChecked = page.querySelectorAll("#div$qNum .jqcheck.checkon").isNotEmpty()
                        if (!anyChecked && checkboxes.isNotEmpty()) {
                            checkboxes[0].click()
                        }
                    }

                    7 -> { // 下拉框
                        val chosen = weightedRandomIndex(answerList)
                        val selectEl = page.querySelector("#q$qNum")
                        if (selectEl != null) {
                            val options = page.querySelectorAll("#q$qNum option").filter {
                                it.getAttribute("value") != "-2"
                            }
                            if (chosen in options.indices) {
                                selectEl.selectOption(options[chosen].getAttribute("value"))
                            }
                        }
                    }

                    5 -> { // 量表题
                        val chosen = weightedRandomIndex(answerList)
                        val rateButtons = page.querySelectorAll("#div$qNum .rate-off")
                        if (chosen in rateButtons.indices) {
                            rateButtons[chosen].click()
                        }
                    }

                    6 -> { // 矩阵题
                        for ((rowIdx, rowAnswer) in answerList.withIndex()) {
                            val probs = rowAnswer.split(",").map { it.trim().toIntOrNull() ?: 50 }
                            val chosen = weightedRandomIndex(probs.map { it.toString() })
                            val cells = page.querySelectorAll("#div$qNum tr[rowindex='$rowIdx'] .rate-off")
                            if (chosen in cells.indices) {
                                cells[chosen].click()
                                if (antiBot) Thread.sleep((200..500).random().toLong())
                            }
                        }
                    }

                    1 -> { // 填空题
                        val text = answerList.joinToString(" ")
                        page.fill("#q${qNum}", text)
                    }

                    9 -> { // 滑动条
                        for ((rowIdx, value) in answerList.withIndex()) {
                            val selector = "#q${qNum}_$rowIdx"
                            page.fill(selector, value)
                            page.querySelector(selector)?.dispatchEvent("change")
                            if (antiBot) Thread.sleep((200..400).random().toLong())
                        }
                    }

                    11 -> { // 排序题
                        val indices = (answerList.indices).sortedByDescending {
                            answerList.getOrNull(it)?.toIntOrNull() ?: 50
                        }
                        for (i in indices) {
                            val items = page.querySelectorAll("#div$qNum .ui-li-static")
                            if (i in items.indices) {
                                items[i].click()
                                if (antiBot) Thread.sleep((300..600).random().toLong())
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("[$threadName] 题目 $qNum 填写异常: ${e.message}")
            }
        }

        // 提交
        if (antiBot) Thread.sleep((1000..2000).random().toLong())
        val submitBtn = page.querySelector("#ctlNext")
        submitBtn?.click()
        Thread.sleep(2000)
    }

    /**
     * 根据概率权重列表随机选一个索引
     */
    private fun weightedRandomIndex(probs: List<String>): Int {
        val weights = probs.map { it.toIntOrNull()?.coerceAtLeast(0) ?: 50 }
        val totalWeight = weights.sum()
        if (totalWeight == 0) return (probs.indices).random()

        val rand = (1..totalWeight).random()
        var cumulative = 0
        for ((i, w) in weights.withIndex()) {
            cumulative += w
            if (rand <= cumulative) return i
        }
        return probs.lastIndex
    }

    /**
     * 停止所有任务（安全退出）
     */
    fun stopAll() {
        if (runState.value == RunState.RUNNING) {
            runState.value = RunState.STOPPING
            stopRequested.set(true)
            executor?.shutdown()
            // 异步等待关闭
            Thread {
                executor?.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS)
                executor?.shutdownNow()
                runState.value = RunState.IDLE
            }.start()
        }
    }

    // 保留兼容性
    fun pauseAll() {}
    fun resumeAll() {}
}
