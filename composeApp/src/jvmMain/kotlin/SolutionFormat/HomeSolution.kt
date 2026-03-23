package SolutionFormat

import androidx.compose.runtime.Composable

@Composable
fun HomeSolution(surveyData : Map<String, Any>?){

    surveyData?.let { data ->
        val option = data["option"] as? List<List<String>> ?: emptyList()
        val typeInt = data["typeInts"] as? List<Int> ?: emptyList()

        println(typeInt)

        typeInt.forEach { type ->
            when(type){
                
                3 -> {ChocieQuestions()}
                4 -> {println("这是第四题")}
                else -> {println(type)}
            }
        }
    }



}