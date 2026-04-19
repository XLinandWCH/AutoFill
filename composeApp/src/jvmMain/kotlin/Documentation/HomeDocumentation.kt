package Documentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeDocumentation() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            DoCatalog()
        }
    }
}
