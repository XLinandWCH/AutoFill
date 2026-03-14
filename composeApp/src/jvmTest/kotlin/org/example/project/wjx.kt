package org.example.project

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

suspend fun wjxCrawler(url: String){
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

// 2. 遍历所有题目容器
        val optionAll = questionContainers.map { question ->
            when {
                // 使用 select 判断是否存在对应的选项标签
                question.select(".ui-radio").isNotEmpty() ->
                    question.select(".ui-radio").map { it.text().trim() }

                question.select(".ui-checkbox").isNotEmpty() ->
                    question.select(".ui-checkbox").map { it.text().trim() }

                // 显式匹配填空题的输入框
                question.select(".ui-input-text").isNotEmpty() ->
                    emptyList()

                question.select("option").isNotEmpty() ->
                    question.select("option").map { it.text().trim() }

                else -> emptyList() // 兜底返回空列表，确保 index 对齐
            }
        }
        surveyData["title"] = title
        surveyData["divx"] = divx
        surveyData["question"] = questions
        surveyData["option"] = optionAll

        println("字典已成功写入")
        for (survey in surveyData){
            println(survey)
        }

    } catch (e: Exception) {
        // 针对 url 输入错误或网络请求失败的各种可能情况进行详细匹配
        error = when (e) {
            is IllegalArgumentException, is MalformedURLException ->
                "URL格式不正确：请检查是否缺少协议头(如 http:// 或 https://)或包含了非法字符。"
            is UnknownHostException, is UnresolvedAddressException ->
                "无法解析域名：请检查URL中的域名是否输入有误，或检查当前网络是否连通。"
            is ConnectException ->
                "连接被拒绝：目标服务器可能已宕机，或者URL中的端口不正确。"
            is SocketTimeoutException ->
                "请求超时：目标服务器响应时间过长，可能是网络拥堵或遇到了反爬限制。"
            is SSLHandshakeException ->
                "安全连接失败：HTTPS证书校验未通过，可能是目标网站证书已过期。"
            is io.ktor.client.plugins.ResponseException ->
                "HTTP请求错误：服务器返回了错误状态码（${e.response.status.value}），可能是页面已删除(404)或无权限访问(403)。"
            else ->
                "发生未知错误：${e.message}"
        }

        // 可选：打印出错误信息方便调试确认
        println("抓取中断，错误信息: $error")

    } finally {
        client.close()
    }
}