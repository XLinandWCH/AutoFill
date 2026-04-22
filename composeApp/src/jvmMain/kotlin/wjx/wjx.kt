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

        // 2. 遍历所有题目容器提取选项文本
        val optionAll = questionContainers.map { question ->
            when {
                question.select(".ui-radio").isNotEmpty() ->
                    question.select(".ui-radio").map { it.text().trim() }

                question.select(".ui-checkbox").isNotEmpty() ->
                    question.select(".ui-checkbox").map { it.text().trim() }

                // 下拉框 (剔除值为 -2 的 "请选择")
                question.select("option").isNotEmpty() ->
                    question.select("option").filter { it.attr("value") != "-2" }.map { it.text().trim() }

                // 排序题
                question.select(".ui-li-static").isNotEmpty() ->
                    question.select(".ui-li-static").map { it.text().trim() }

                // 量表题 (小星星或数字)
                question.select(".rate-off").isNotEmpty() ->
                    question.select("li.td").map { it.text().trim() }

                // 矩阵题 (获取行标题作为子问题)
                question.select("tr.rowtitle").isNotEmpty() ->
                    question.select("tr.rowtitle .itemTitleSpan").map { it.text().trim() }

                // 滑动条题 (获取子问题)
                question.select("tr.rowtitletr").isNotEmpty() ->
                    question.select("tr.rowtitletr td.title span.itemTitleSpan").map { it.text().trim() }

                // 填空题
                question.select(".ui-input-text").isNotEmpty() || question.select("textarea").isNotEmpty() ->
                    emptyList()

                else -> emptyList() // 兜底
            }
        }

        // 3. 提取选项中是否包含填空题 (.ui-text 或者 .OtherText)
        val hasTextInputAll = questionContainers.map { question ->
            when {
                question.select(".ui-radio").isNotEmpty() ->
                    question.select(".ui-radio").map { it.select(".ui-text").isNotEmpty() || it.select("input[type=text]").isNotEmpty() }

                question.select(".ui-checkbox").isNotEmpty() ->
                    question.select(".ui-checkbox").map { it.select(".ui-text").isNotEmpty() || it.select("input[type=text]").isNotEmpty() }

                question.select(".ui-li-static").isNotEmpty() ->
                    question.select(".ui-li-static").map { it.select(".ui-text").isNotEmpty() || it.select("input[type=text]").isNotEmpty() }

                else -> emptyList()
            }
        }

        // 爬取所有题目的 type 属性
        val typeStrings = questionContainers.map { container ->
            container.attr("type")  // 获取 type 属性值
        }

        // 提取矩阵题的列标题
        val matrixColsAll = questionContainers.map { question ->
            if (question.select("table.matrix-rating").isNotEmpty()) {
                // 通常矩阵题列标题在 thead th 中，剔除第一个空 th
                question.select("thead th").map { it.text().trim() }.filter { it.isNotEmpty() }
            } else {
                emptyList()
            }
        }


        // 转换为 Int
        val typeInts = typeStrings.map { typeStr ->
            typeStr.toIntOrNull() ?: 0  // 安全转换
        }
        // 结果: [3, 3, 1, 2, ...]

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