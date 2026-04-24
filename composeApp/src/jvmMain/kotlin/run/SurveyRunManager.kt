package run

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import java.util.concurrent.atomic.AtomicInteger

/**
 * 问卷自动填写运行管理器（全局单例）
 *
 * 管理多线程问卷填写任务的生命周期，包括启动、暂停、恢复、停止，
 * 并维护运行状态、进度统计和任务日志。
 */
object SurveyRunManager {

    // ─── 运行状态枚举 ───────────────────────────────────────────
    enum class RunState {
        IDLE,       // 空闲，可以启动
        RUNNING,    // 运行中
        PAUSED,     // 已暂停
        STOPPING    // 停止中（等待线程安全退出）
    }

    // ─── 任务日志条目 ───────────────────────────────────────────
    data class TaskLogEntry(
        val id: Int,              // 任务序号
        val threadName: String,   // 执行线程名称
        val status: String,       // 状态文字（成功、失败、运行中 等）
        val taskProgress: String, // 进度文字，如 "3/10"
        val detail: String = ""   // 详情/备注
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

    // ─── 操作方法 ──────────────────────────────────────────────

    /**
     * 启动问卷自动填写任务
     */
    fun start() {
        if (runState.value != RunState.IDLE) return

        // 重置统计
        successCount.set(0)
        failCount.set(0)
        completedCount.value = 0
        taskLogs.clear()

        runState.value = RunState.RUNNING

        // TODO: 在此处启动实际的多线程问卷填写逻辑
        // 例如使用 Playwright 打开浏览器、读取 AnswerDictionary 并自动填写
    }

    /**
     * 暂停所有运行中的任务
     */
    fun pauseAll() {
        if (runState.value == RunState.RUNNING) {
            runState.value = RunState.PAUSED
            // TODO: 通知各线程挂起
        }
    }

    /**
     * 恢复所有已暂停的任务
     */
    fun resumeAll() {
        if (runState.value == RunState.PAUSED) {
            runState.value = RunState.RUNNING
            // TODO: 通知各线程继续
        }
    }

    /**
     * 停止所有任务（安全退出）
     */
    fun stopAll() {
        if (runState.value == RunState.RUNNING || runState.value == RunState.PAUSED) {
            runState.value = RunState.STOPPING
            // TODO: 通知各线程停止，完成后切回 IDLE
            // 临时实现：直接切回 IDLE
            runState.value = RunState.IDLE
        }
    }

    // ─── 工具方法（供执行线程调用） ─────────────────────────────

    /**
     * 添加一条任务日志
     */
    fun addLog(entry: TaskLogEntry) {
        taskLogs.add(entry)
        completedCount.value = successCount.get() + failCount.get()
    }

    /**
     * 记录一次成功
     */
    fun recordSuccess(threadName: String, detail: String = "") {
        val count = successCount.incrementAndGet()
        val total = totalTarget.value
        addLog(
            TaskLogEntry(
                id = taskLogs.size + 1,
                threadName = threadName,
                status = "成功",
                taskProgress = "$count/$total",
                detail = detail
            )
        )
    }

    /**
     * 记录一次失败
     */
    fun recordFailure(threadName: String, detail: String = "") {
        val count = failCount.incrementAndGet()
        val total = totalTarget.value
        addLog(
            TaskLogEntry(
                id = taskLogs.size + 1,
                threadName = threadName,
                status = "失败",
                taskProgress = "${successCount.get()}/$total",
                detail = detail
            )
        )
    }
}
