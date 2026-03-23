package SolutionFormat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

object AnswerDictionary {
    val answers = mutableStateListOf<SnapshotStateList<String>>()
    val typeMap = mutableMapOf<Int, Int>() 

    fun initialize(typeInts: List<Int>, optionsList: List<List<String>>) {
        if (answers.isEmpty() || answers.size != typeInts.size) {
            answers.clear()
            typeMap.clear()
            typeInts.forEachIndexed { index, type ->
                typeMap[index] = type
                val options = optionsList.getOrNull(index) ?: emptyList()
                val list = mutableStateListOf<String>()
                if (type == 3 || type == 4) {
                    val count = if (options.isNotEmpty()) options.size else 1
                    repeat(count) { list.add("50") }
                } else if (type == 1) {
                    list.add("") 
                } else {
                    list.add("")
                }
                answers.add(list)
            }
        }
    }

    fun updateAnswer(questionIndex: Int, optionIndex: Int, value: String) {
        if (questionIndex in answers.indices) {
            val qList = answers[questionIndex]
            val type = typeMap[questionIndex] ?: 3
            
            if (type == 1) {
                // Split multi-line input into separate strings
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

    fun getAnswer(questionIndex: Int, optionIndex: Int): String {
        val qList = answers.getOrNull(questionIndex) ?: return ""
        val type = typeMap[questionIndex] ?: 3
        
        return if (type == 1) {
            qList.joinToString("\n")
        } else {
            qList.getOrNull(optionIndex) ?: ""
        }
    }

    fun printDictionary() {
        println("Current Answer Dictionary: $answers")
    }
}

@Composable
fun HomeSolution(questionIndex: Int, optionIndex: Int, type: Int){
    when(type){
        3 -> { ChoiceQuestions(questionIndex, optionIndex) }
        4 -> { MultipleChoiceQuestions(questionIndex, optionIndex) }
        1 -> { FillBlankQuestions(questionIndex, optionIndex) }
        else -> { println("Unknown type $type") }
    }
}