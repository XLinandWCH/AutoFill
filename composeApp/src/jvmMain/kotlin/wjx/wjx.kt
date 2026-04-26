package wjx

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.jsoup.Jsoup
import java.net.ConnectException
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import javax.net.ssl.SSLHandshakeException

// 定义全局字典，供所有函数直接调用

suspend fun wjxCrawler(url: String): Map<String, Any>?{
    var error: String? = null // 定义 error 变量存储详细错误信息
    val client = HttpClient(CIO)
    val surveyData = mutableMapOf<String, Any>()

    try {
        val response : HttpResponse = client.get(url) {
            headers {
                append("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:148.0) Gecko/20100101 Firefox/148.0")
            }
        }
        val html = response.bodyAsText()

        val doc = Jsoup.parse(html)
        val title = doc.title()
        val divx = doc.select(".field.ui-field-contain").size
        val questions = doc.select(".topichtml").map { it.text() }
        // 1. 获取所有题目的公共父容器（确保包含填空题的容器）
        val questionContainers = doc.select(".field.ui-field-contain")

        // ═══ 首先提取每个题目容器的 type 属性（这是最可靠的题型判断依据） ═══
        val typeStrings = questionContainers.map { container ->
            container.attr("type")
        }
        val typeInts = typeStrings.map { typeStr ->
            typeStr.toIntOrNull() ?: 0
        }

        // ═══ 基于 type 属性提取选项（type-first 策略，彻底避免 CSS 选择器冲突） ═══
        val optionAll = questionContainers.mapIndexed { idx, question ->
            val type = typeInts.getOrElse(idx) { 0 }
            when (type) {
                3 -> // 单选题
                    question.select(".ui-radio").map { it.text().trim() }

                4 -> // 多选题
                    question.select(".ui-checkbox").map { it.text().trim() }

                7 -> // 下拉框
                    question.select("option").filter { it.attr("value") != "-2" }.map { it.text().trim() }

                11 -> // 排序题
                    question.select(".ui-li-static").map { it.text().trim() }

                5 -> // 量表题
                    question.select("li.td").map { it.text().trim() }

                6 -> {
                    // 矩阵题：提取行标题
                    // WJX 矩阵结构：<tr class="rowtitle"><td><span class="itemTitleSpan">行标题</span></td></tr>
                    // 同时存在数据行 <tr tp="d"><td class="scalerowtitletd"><span class="itemTitleSpan">行标题</span></td></tr>
                    // 使用数据行（tr[tp=d]）的 itemTitleSpan 来获取去重的行标题
                    val dataRows = question.select("tr[tp=d] .itemTitleSpan")
                    if (dataRows.isNotEmpty()) {
                        dataRows.map { it.text().trim() }.filter { it.isNotEmpty() }
                    } else {
                        // 备选：从 tr.rowtitle 获取
                        val rowTitles = question.select("tr.rowtitle .itemTitleSpan")
                        if (rowTitles.isNotEmpty()) {
                            rowTitles.map { it.text().trim() }.filter { it.isNotEmpty() }
                        } else {
                            // 兜底：表格中除表头外每行的第一列
                            val table = question.select("table").firstOrNull()
                            table?.select("tbody tr, tr")?.drop(1)
                                ?.mapNotNull { it.select("td, th").firstOrNull()?.text()?.trim() }
                                ?.filter { it.isNotEmpty() }
                                ?: emptyList()
                        }
                    }
                }

                9 -> // 滑动条题
                    question.select("tr.rowtitletr td.title span.itemTitleSpan").map { it.text().trim() }

                1 -> // 填空题
                    emptyList()

                else -> {
                    // 兜底：尝试通用 CSS 选择器
                    when {
                        question.select(".ui-radio").isNotEmpty() ->
                            question.select(".ui-radio").map { it.text().trim() }
                        question.select(".ui-checkbox").isNotEmpty() ->
                            question.select(".ui-checkbox").map { it.text().trim() }
                        else -> emptyList()
                    }
                }
            }
        }

        // ═══ 提取选项中是否包含填空题 (.ui-text 或 .OtherText) ═══
        val hasTextInputAll = questionContainers.mapIndexed { idx, question ->
            val type = typeInts.getOrElse(idx) { 0 }
            when (type) {
                3 -> question.select(".ui-radio").map {
                    it.select(".ui-text").isNotEmpty() || it.select("input[type=text]").isNotEmpty()
                }
                4 -> question.select(".ui-checkbox").map {
                    it.select(".ui-text").isNotEmpty() || it.select("input[type=text]").isNotEmpty()
                }
                11 -> question.select(".ui-li-static").map {
                    it.select(".ui-text").isNotEmpty() || it.select("input[type=text]").isNotEmpty()
                }
                else -> emptyList()
            }
        }

        // ═══ 提取矩阵题的列标题 ═══
        val matrixColsAll = questionContainers.mapIndexed { idx, question ->
            val type = typeInts.getOrElse(idx) { 0 }
            if (type == 6) {
                val matrixTable = question.select("table.matrix-rating, table.matrixtable, table").firstOrNull()
                if (matrixTable != null) {
                    // 优先从 tr.trlabel th 获取（WJX 标准结构）
                    var headers = matrixTable.select("tr.trlabel th").map { it.text().trim() }.filter { it.isNotEmpty() }

                    // 备选：thead th
                    if (headers.isEmpty()) {
                        headers = matrixTable.select("thead th").map { it.text().trim() }.filter { it.isNotEmpty() }
                    }

                    // 兜底：第一行的 th/td，跳过第一个空单元格
                    if (headers.isEmpty()) {
                        headers = matrixTable.select("tr").firstOrNull()
                            ?.select("td, th")?.drop(1)
                            ?.map { it.text().trim() }?.filter { it.isNotEmpty() }
                            ?: emptyList()
                    }
                    headers
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }



        surveyData["title"] = title
        surveyData["divx"] = divx
        surveyData["question"] = questions
        surveyData["option"] = optionAll
        surveyData["typeInts"] = typeInts
        surveyData["hasTextInput"] = hasTextInputAll
        surveyData["matrixCols"] = matrixColsAll
        surveyData["url"] = url


        println("字典已成功写入")
        for (survey in surveyData){
            println(survey)
        }
        return surveyData

    } catch (e: Exception) {
        // 针对 url 输入错误或网络请求失败的各种可能情况进行详细匹配
        error = when (e) {
            is IllegalArgumentException, is MalformedURLException ->
                "【URL格式不正确】\n请检查是否缺少协议头(如 http:// 或 https://)\n或包含了非法字符。"
            is UnknownHostException, is UnresolvedAddressException ->
                "【无法解析域名】\n请检查URL中的域名是否输入有误\n或检查当前网络是否连通。"
            is ConnectException ->
                "【连接被拒绝】\n目标服务器可能已宕机\n或者输入的问卷地址不正确。"
            is SocketTimeoutException ->
                "【请求超时】\n目标服务器响应时间过长，可能是网络拥堵或遇到了反爬限制。"
            is SSLHandshakeException ->
                "【安全连接失败】\nHTTPS证书校验未通过，可能是目标网站证书已过期。"
            is io.ktor.client.plugins.ResponseException ->
                "【HTTP请求错误】\n服务器返回了错误状态码（${e.response.status.value}）\n可能是页面已删除(404)或无权限访问(403)。"
            else ->
                "【发生未知错误】\n${e.message}"
        }

        // 可选：打印出错误信息方便调试确认
        println("抓取中断，错误信息: $error")

        // 将错误信息存入字典
        surveyData["error"] = error
        surveyData["title"] = ""
        surveyData["divx"] = 0
        surveyData["question"] = emptyList<String>()
        surveyData["option"] = emptyList<List<String>>()
        surveyData["matrixCols"] = emptyList<List<String>>()

        return surveyData

    } finally {
        client.close()
    }
}