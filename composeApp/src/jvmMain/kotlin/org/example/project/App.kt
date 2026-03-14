package org.example.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import content.Home


@Composable
@Preview
fun App() {
    Column(modifier = Modifier.fillMaxSize()){

        MenuBar()
        Home()

    }

}