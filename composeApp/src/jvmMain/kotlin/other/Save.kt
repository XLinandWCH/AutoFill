package other

import SolutionFormat.AnswerDictionary
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

/**
 * 处理问卷配置的导出与保存
 */
fun saveSurveyConfig() {
    val url = AnswerDictionary.currentUrl
    if (url.isEmpty()) return

    // 1. 弹出保存对话框
    val dialog = FileDialog(null as Frame?, "保存问卷配置", FileDialog.SAVE)
    dialog.file = "survey_config.json"
    dialog.isVisible = true
    
    val fileName = dialog.file ?: return
    val directory = dialog.directory ?: return
    val file = File(directory, fileName)
    
    try {
        val answers = AnswerDictionary.toPlainList()
        val optionTexts = AnswerDictionary.optionTexts.map { it.toList() }

        // 2. 序列化数据
        val json = buildString {
            append("{\n")
            append("  \"url\": \"$url\",\n")
            append("  \"answers\": ${serializeNestedList(answers)},\n")
            append("  \"optionTexts\": ${serializeNestedList(optionTexts)}\n")
            append("}")
        }
        
        // 3. 写入文件
        val targetFile = if (file.name.endsWith(".json")) file else File(file.parent, file.name + ".json")
        targetFile.writeText(json)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 内部工具：序列化嵌套列表
 */
private fun serializeNestedList(list: List<List<String>>): String {
    return list.joinToString(prefix = "[", postfix = "]", separator = ",") { sub ->
        sub.joinToString(prefix = "[", postfix = "]", separator = ",") { 
            "\"${it.replace("\"", "\\\"").replace("\n", "\\n")}\"" 
        }
    }
}
