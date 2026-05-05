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
        val id: Int,              // 线程 ID (1, 2, 3...)
        val threadName: String,   // 线程名称
        var status: String,       // 状态文字
        var taskProgress: String, // 该线程进度 (已完成/总分配)
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
            addLog(TaskLogEntry(1, "主线程", "失败", "0/0", "未加载问卷链接"))
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
        
        // ─── 计算任务分配 ───
        val baseTasks = target / threads
        val remainder = target % threads

        executor = Executors.newFixedThreadPool(threads)

        // 启动工作线程
        repeat(threads) { threadIdx ->
            val myThreadId = threadIdx + 1
            val tasksForThisThread = baseTasks + (if (threadIdx < remainder) 1 else 0)
            
            // 立即为每个线程占位一条日志 (ID 固定为 1, 2, 3...)
            addLog(TaskLogEntry(myThreadId, "线程-$myThreadId", "准备中", "0/$tasksForThisThread", "等待浏览器启动..."))

            executor?.submit {
                val tName = "线程-$myThreadId"
                var myCompleted = 0
                
                try {
                    // 关键：告诉运行引擎去我们自定义的路径查找内核
                    val browsersPath = other.BrowserManager.getEffectiveBrowsersPath()
                    val createOptions = com.microsoft.playwright.Playwright.CreateOptions()
                        .setEnv(mapOf(
                            "PLAYWRIGHT_BROWSERS_PATH" to browsersPath,
                            "PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD" to "1", // 明确禁止在此处下载内核
                            "PLAYWRIGHT_DOWNLOAD_HOST" to "https://npmmirror.com/mirrors/playwright/"
                        ))

                    Playwright.create(createOptions).use { pw ->
                        val browser = pw.chromium().launch(
                            BrowserType.LaunchOptions()
                                .setHeadless(headless)
                                .setArgs(listOf(
                                    "--disable-extensions",
                                    "--disable-component-update",
                                    "--mute-audio",
                                    "--no-sandbox",
                                    "--disable-setuid-sandbox",
                                    "--disable-gpu",
                                    "--disable-dev-shm-usage"
                                ))
                        )

                        repeat(tasksForThisThread) { 
                            if (stopRequested.get()) return@repeat
                            
                            val currentStep = it + 1
                            val logIdx = threadIdx // 对应预先添加的行
                            val progress = "$myCompleted/$tasksForThisThread"

                            try {
                                updateLog(logIdx, "运行中", "正在打开页面...", progress)
                                
                                val context = browser.newContext()
                                val page = context.newPage()
                                page.navigate(url)
                                page.waitForLoadState(LoadState.NETWORKIDLE)

                                // 详细填表过程
                                fillSurvey(page, currentStep, tName, antiBot, logIdx, tasksForThisThread, myCompleted)

                                context.close()
                                myCompleted++
                                successCount.incrementAndGet()
                                completedCount.value = successCount.get() + failCount.get()
                                updateLog(logIdx, "运行中", "任务完成", "$myCompleted/$tasksForThisThread")
                            } catch (e: Exception) {
                                myCompleted++
                                failCount.incrementAndGet()
                                completedCount.value = successCount.get() + failCount.get()
                                updateLog(logIdx, "运行中", "单次任务失败: ${e.message?.take(30)}", "$myCompleted/$tasksForThisThread")
                            }

                            if (antiBot && !stopRequested.get() && currentStep < tasksForThisThread) {
                                Thread.sleep((2000L..4000L).random())
                            }
                        }
                        
                        // 线程最终状态
                        updateLog(threadIdx, "已结束", "所有分配任务已处理", "$myCompleted/$tasksForThisThread")
                        browser.close()
                    }
                } catch (e: Exception) {
                    val errorDetail = e.message ?: e.toString()
                    updateLog(threadIdx, "错误", "初始化失败: $errorDetail", "0/$tasksForThisThread")
                    e.printStackTrace()
                }

                // 检查是否全部完成
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
    fun updateLog(index: Int, status: String, detail: String, progress: String? = null) {
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
    private fun fillSurvey(page: com.microsoft.playwright.Page, taskId: Int, threadName: String, antiBot: Boolean, logIndex: Int, threadTotal: Int, threadCompleted: Int) {
        val answers = AnswerDictionary.toPlainList()
        val types = AnswerDictionary.typeMap
        val progress = "$threadCompleted/$threadTotal"

        for ((qIdx, answerList) in answers.withIndex()) {
            if (stopRequested.get()) break
            
            val type = types[qIdx] ?: 3
            val qNum = qIdx + 1
            val prefix = "【第${qNum}题】"

            try {
                // 每题作答前的基本延时
                if (antiBot) {
                    val scrollDetail = AntiBotUtils.randomScroll(page)
                    updateLog(logIndex, "运行中", scrollDetail, progress)
                    AntiBotUtils.breatheDelay()
                }

                // 确保题目在视野内
                val questionDiv = page.querySelector("#div$qNum")
                if (antiBot) AntiBotUtils.ensureInView(questionDiv)

                when (type) {
                    3 -> { // 单选题
                        val chosen = weightedRandomIndex(answerList)
                        val radios = page.querySelectorAll("#div$qNum .ui-radio")
                        if (chosen in radios.indices) {
                            val act = AntiBotUtils.humanClick(page, radios[chosen], antiBot)
                            updateLog(logIndex, "运行中", "$prefix $act", progress)
                            handleChoiceTextInput(page, qIdx, chosen, qNum, antiBot, logIndex)
                        }
                    }

                    4 -> { // 多选题
                        val checkboxes = page.querySelectorAll("#div$qNum .ui-checkbox")
                        for ((i, cb) in checkboxes.withIndex()) {
                            val prob = answerList.getOrNull(i)?.toIntOrNull() ?: 50
                            if ((1..100).random() <= prob) {
                                val act = AntiBotUtils.humanClick(page, cb, antiBot)
                                updateLog(logIndex, "运行中", "$prefix $act", progress)
                                handleChoiceTextInput(page, qIdx, i, qNum, antiBot, logIndex)
                            }
                        }
                    }

                    7 -> { // 下拉框
                        val chosen = weightedRandomIndex(answerList)
                        val selectEl = page.querySelector("#q$qNum")
                        if (selectEl != null) {
                            if (antiBot) AntiBotUtils.ensureInView(selectEl)
                            val options = page.querySelectorAll("#q$qNum option").filter {
                                it.getAttribute("value") != "-2"
                            }
                            if (chosen in options.indices) {
                                selectEl.selectOption(options[chosen].getAttribute("value"))
                                updateLog(logIndex, "运行中", "$prefix 选中下拉项", progress)
                            }
                        }
                    }

                    5 -> { // 量表题
                        val chosen = weightedRandomIndex(answerList)
                        val rateButtons = page.querySelectorAll("#div$qNum .rate-off")
                        if (chosen in rateButtons.indices) {
                            val act = AntiBotUtils.humanClick(page, rateButtons[chosen], antiBot)
                            updateLog(logIndex, "运行中", "$prefix $act", progress)
                        }
                    }

                    6 -> { // 矩阵题
                        for ((rowIdx, rowAnswer) in answerList.withIndex()) {
                            val probs = rowAnswer.split(",").map { it.trim().toIntOrNull() ?: 50 }
                            val chosen = weightedRandomIndex(probs.map { it.toString() })
                            val cells = page.querySelectorAll("#div$qNum tr[rowindex='$rowIdx'] .rate-off")
                            if (chosen in cells.indices) {
                                val act = AntiBotUtils.humanClick(page, cells[chosen], antiBot)
                                updateLog(logIndex, "运行中", "$prefix[行${rowIdx+1}] $act", progress)
                            }
                        }
                    }

                    1 -> { // 填空题
                        val rawText = answerList.firstOrNull() ?: ""
                        val act = AntiBotUtils.humanType(page, "#q$qNum", rawText, antiBot)
                        updateLog(logIndex, "运行中", "$prefix $act", progress)
                    }

                    9 -> { // 滑动条
                        for ((rowIdx, value) in answerList.withIndex()) {
                            val selector = "#q${qNum}_$rowIdx"
                            val act = AntiBotUtils.humanType(page, selector, value, antiBot)
                            updateLog(logIndex, "运行中", "$prefix[项${rowIdx+1}] $act", progress)
                            page.querySelector(selector)?.dispatchEvent("change")
                        }
                    }

                    11 -> { // 排序题
                        val indices = (answerList.indices).sortedByDescending {
                            answerList.getOrNull(it)?.toIntOrNull() ?: 50
                        }
                        for (i in indices) {
                            val items = page.querySelectorAll("#div$qNum .ui-li-static")
                            if (i in items.indices) {
                                val act = AntiBotUtils.humanClick(page, items[i], antiBot)
                                updateLog(logIndex, "运行中", "$prefix 排序点击", progress)
                            }
                        }
                    }
                }

                // 每题做完后随机滚动
                if (antiBot && (1..100).random() <= 25) {
                    val scrollDetail = AntiBotUtils.randomScroll(page)
                    updateLog(logIndex, "运行中", scrollDetail, progress)
                }

            } catch (e: Exception) {
                println("[$threadName] 题目 $qNum 填写异常: ${e.message}")
            }
        }

        // 提交
        if (antiBot) AntiBotUtils.breatheDelay()
        val submitBtn = page.querySelector("#ctlNext")
        AntiBotUtils.humanClick(page, submitBtn, antiBot)
        Thread.sleep(2000)
    }

    /**
     * 处理选项内的填空题（如：其他_______）
     */
    private fun handleChoiceTextInput(page: com.microsoft.playwright.Page, qIdx: Int, optIdx: Int, qNum: Int, antiBot: Boolean, logIndex: Int) {
        val text = AnswerDictionary.optionTexts.getOrNull(qIdx)?.getOrNull(optIdx) ?: ""
        if (text.isNotBlank()) {
            val prefix = "【第${qNum}题】选项补充-"
            // WJX 常见的选项内填空 ID 格式：t_qX_Y 或在 label 同级下的 input
            val selector = "#t_q${qNum}_${optIdx + 1}"
            if (page.querySelector(selector) != null) {
                val act = AntiBotUtils.humanType(page, selector, text, antiBot)
                updateLog(logIndex, "运行中", "$prefix$act")
            } else {
                // 兜底尝试查找该题目下的所有文本框
                val inputs = page.querySelectorAll("#div$qNum input[type=text]")
                for (input in inputs) {
                    if (input.isVisible) {
                        val id = input.getAttribute("id") ?: ""
                        val act = AntiBotUtils.humanType(page, "#$id", text, antiBot)
                        updateLog(logIndex, "运行中", "$prefix$act")
                        break
                    }
                }
            }
        }
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
