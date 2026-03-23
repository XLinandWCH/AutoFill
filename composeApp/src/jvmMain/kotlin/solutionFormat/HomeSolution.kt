package SolutionFormat

import androidx.compose.runtime.Composable

object AnswerDictionary {
    val answers = mutableListOf<MutableList<String>>()

    fun initialize(typeInts: List<Int>, optionsList: List<List<String>>) {
        if (answers.isEmpty() || answers.size != typeInts.size) {
            answers.clear()
            typeInts.forEachIndexed { index, type ->
                val options = optionsList.getOrNull(index) ?: emptyList()
                val list = mutableListOf<String>()
                if (type == 3 || type == 4) {
                    val count = if (options.isNotEmpty()) options.size else 1
                    repeat(count) { list.add("50") }
                } else if (type == 1) {
                    val count = if (options.isNotEmpty()) options.size else 1
                    repeat(count) { list.add("") }
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

    fun getAnswer(questionIndex: Int, optionIndex: Int): String {
        return answers.getOrNull(questionIndex)?.getOrNull(optionIndex) ?: ""
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