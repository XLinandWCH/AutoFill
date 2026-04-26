package other

import SolutionFormat.AnswerDictionary
import content.SearchTrigger
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

/**
 * 处理问卷配置的读取与导入
 */
fun importSurveyConfig() {
    // 1. 弹出加载对话框
    val dialog = FileDialog(null as Frame?, "导入问卷配置", FileDialog.LOAD)
    dialog.file = "*.json"
    dialog.isVisible = true
    
    val fileName = dialog.file ?: return
    val directory = dialog.directory ?: return
    val file = File(directory, fileName)
    
    try {
        val content = file.readText()
        
        // 2. 解析 JSON 数据
        val url = "\"url\":\\s*\"([^\"]+)\"".toRegex().find(content)?.groupValues?.get(1) ?: return
        val answersStr = findList(content, "answers") ?: return
        val textsStr = findList(content, "optionTexts") ?: return
        
        val answers = parseNestedList(answersStr)
        val texts = parseNestedList(textsStr)
        
        // 3. 应用数据并触发搜索
        AnswerDictionary.setPendingData(answers, texts)
        SearchTrigger.onSearch?.invoke(url)
        
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 内部工具：解析 JSON 中的数组
 */
private fun findList(content: String, key: String): String? {
    val match = "\"$key\":\\s*(\\[.*)".toRegex(RegexOption.DOT_MATCHES_ALL).find(content) ?: return null
    val arrayPart = match.groupValues[1]
    var balance = 0
    for (i in arrayPart.indices) {
        if (arrayPart[i] == '[') balance++
        if (arrayPart[i] == ']') balance--
        if (balance == 0) return arrayPart.substring(0, i + 1)
    }
    return null
}

private fun parseNestedList(json: String): List<List<String>> {
    val result = mutableListOf<List<String>>()
    val innerPart = json.trim().removePrefix("[").removeSuffix("]")
    var balance = 0
    var current = StringBuilder()
    val subStrings = mutableListOf<String>()
    
    for (char in innerPart) {
        if (char == '[') balance++
        if (char == ']') balance--
        current.append(char)
        if (balance == 0 && char == ']') {
            subStrings.add(current.toString())
            current = StringBuilder()
        }
    }
    
    for (sub in subStrings) {
        val list = mutableListOf<String>()
        val itemsPart = sub.trim().removePrefix("[").removeSuffix("]")
        "\"((?:\\\\\"|[^\"])*)\"".toRegex().findAll(itemsPart).forEach {
            list.add(it.groupValues[1].replace("\\\"", "\"").replace("\\n", "\n"))
        }
        result.add(list)
    }
    return result
}
