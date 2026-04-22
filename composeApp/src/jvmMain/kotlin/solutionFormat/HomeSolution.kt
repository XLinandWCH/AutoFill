package SolutionFormat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlin.collections.*

object AnswerDictionary {
    val answers = mutableStateListOf<SnapshotStateList<String>>()
    val optionTexts = mutableStateListOf<SnapshotStateList<String>>() // For storing text input for choices
    val typeMap = mutableMapOf<Int, Int>() 
    val hasTextInputMap = mutableMapOf<Int, List<Boolean>>()
    val matrixColsMap = mutableMapOf<Int, List<String>>()
    var currentUrl: String = ""

    fun initialize(typeInts: List<Int>, optionsList: List<List<String>>, url: String = "", hasTextInputList: List<List<Boolean>> = emptyList(), matrixColsList: List<List<String>> = emptyList()) {
        if (answers.isEmpty() || currentUrl != url || answers.size != typeInts.size) {
            currentUrl = url
            answers.clear()
            optionTexts.clear()
            typeMap.clear()
            hasTextInputMap.clear()
            matrixColsMap.clear()
            typeInts.forEachIndexed { index, type ->
                typeMap[index] = type
                hasTextInputMap[index] = hasTextInputList.getOrNull(index) ?: emptyList()
                matrixColsMap[index] = matrixColsList.getOrNull(index) ?: emptyList()
                val options = optionsList.getOrNull(index) ?: emptyList()
                val list = mutableStateListOf<String>()
                val textList = mutableStateListOf<String>()
                if (type == 1) {
                    list.add("") 
                    textList.add("")
                } else if (type == 6 && matrixColsMap[index]!!.isNotEmpty()) {
                    val rowCount = if (options.isNotEmpty()) options.size else 1
                    val colCount = matrixColsMap[index]!!.size
                    val defaultProbs = MutableList(colCount) { "50" }.joinToString(",")
                    repeat(rowCount) { 
                        list.add(defaultProbs) 
                        textList.add("")
                    }
                } else {
                    val count = if (options.isNotEmpty()) options.size else 1
                    repeat(count) { 
                        list.add("50") 
                        textList.add("")
                    }
                }
                answers.add(list)
                optionTexts.add(textList)
            }
        }
    }

    fun updateAnswer(questionIndex: Int, optionIndex: Int, value: String, isTextInput: Boolean = false) {
        if (questionIndex in answers.indices) {
            val qList = if (isTextInput) optionTexts[questionIndex] else answers[questionIndex]
            val type = typeMap[questionIndex] ?: 3
            
            if (type == 1 && !isTextInput) {
                val lines = value.split("\n")
                qList.clear()
                qList.addAll(lines)
            } else {
                if (optionIndex >= 0 && optionIndex < qList.size) {
                    qList[optionIndex] = value
                } else if (optionIndex >= qList.size) {
                    while(qList.size <= optionIndex) {
                        qList.add("")
                    }
                    qList[optionIndex] = value
                }
            }
        }
    }

    fun getAnswer(questionIndex: Int, optionIndex: Int, isTextInput: Boolean = false): String {
        val qList = if (isTextInput) optionTexts.getOrNull(questionIndex) else answers.getOrNull(questionIndex)
        if (qList == null) return ""
        val type = typeMap[questionIndex] ?: 3
        
        return if (type == 1 && !isTextInput) {
            qList.joinToString("\n")
        } else {
            qList.getOrNull(optionIndex) ?: ""
        }
    }

    fun hasTextInput(questionIndex: Int, optionIndex: Int): Boolean {
        return hasTextInputMap[questionIndex]?.getOrNull(optionIndex) == true
    }

    fun toPlainList(): List<List<String>> {
        return answers.map { it.toList() }
    }

    fun printDictionary() {
        println("Current Answer Dictionary: ${toPlainList()}")
    }
}

@Composable
fun HomeSolution(questionIndex: Int, optionIndex: Int, type: Int){
    when(type){
        3 -> { ChoiceQuestions(questionIndex, optionIndex) }
        4 -> { MultipleChoiceQuestions(questionIndex, optionIndex) }
        1 -> { FillBlankQuestions(questionIndex, optionIndex) }
        5 -> { ScaleQuestions(questionIndex, optionIndex) }
        6 -> { MatrixQuestions(questionIndex, optionIndex) }
        7 -> { DropDownOption(questionIndex, optionIndex) }
        9 -> { SliderQuestions(questionIndex, optionIndex) }
        11 -> { SequencingQuestion(questionIndex, optionIndex) }
        else -> { println("Unknown type $type") }
    }
}