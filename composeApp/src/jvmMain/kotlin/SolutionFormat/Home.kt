package SolutionFormat

import androidx.compose.runtime.Composable


@Composable
fun Home(surveyData : Map<String, Any>?){

    surveyData?.let { data ->
        val option = data["option"] as? List<List<String>> ?:emptyList()
        val typeInt = data["typeInts"] as? List<Int> ?: emptyList()


    }



}