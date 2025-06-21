package dev.onelenyk.pprominec.presentation.`interface`

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import dev.onelenyk.pprominec.presentation.components.RootComponent


@Composable
fun RootContainer(component: RootComponent) {
    Children(
        modifier = Modifier
            .fillMaxSize(),
        stack = component.menuNavigationState,
    ) {
        when (val currentScreen = it.instance) {
            is RootComponent.RootEntry.Main -> {
                MainScreen(currentScreen.component)
            }
        }
    }
    return
}
