package dev.onelenyk.pprominec.presentation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import dev.onelenyk.pprominec.presentation.components.root.RootComponent

@Composable
fun RootContainer(component: RootComponent) {
    Children(
        modifier =
        Modifier
            .fillMaxSize(),
        stack = component.childStack,
    ) {
        when (val child = it.instance) {
            is RootComponent.Child.BottomNav -> BottomNavContainer(child.component)
            is RootComponent.Child.Permissions -> PermissionsScreen(child.component)
            is RootComponent.Child.MapSettings -> MapSettingsScreen(child.component)
        }
    }
    return
}
