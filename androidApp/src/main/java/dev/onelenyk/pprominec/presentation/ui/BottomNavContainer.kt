package dev.onelenyk.pprominec.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.onelenyk.pprominec.R
import dev.onelenyk.pprominec.presentation.components.bottomnav.BottomNavComponent

@Composable
fun BottomNavContainer(component: BottomNavComponent) {
    val childStack by component.stack.subscribeAsState()

    AppScreen(
        showInnerPadding = false,
    ) {
        Column {
            Box(
                modifier = Modifier.weight(1f),
            ) {
                Children(
                    stack = childStack,
                    modifier = Modifier,
                ) {
                    when (val child = it.instance) {
                        is BottomNavComponent.Child.Main -> MainScreen(child.component)
                        is BottomNavComponent.Child.Map -> MapScreen(child.component)
                        is BottomNavComponent.Child.Settings -> SettingsScreen(child.component)
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
            ) {
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    CustomNavigationBarItem(
                        selected = childStack.active.instance is BottomNavComponent.Child.Main,
                        onClick = component::onMainTabClicked,
                        icon = Icons.Default.Create,
                        label = stringResource(id = R.string.calc_tab),
                    )
                    CustomNavigationBarItem(
                        selected = childStack.active.instance is BottomNavComponent.Child.Map,
                        onClick = component::onMapTabClicked,
                        icon = Icons.Default.LocationOn,
                        label = "Карта",
                    )
                    CustomNavigationBarItem(
                        selected = childStack.active.instance is BottomNavComponent.Child.Settings,
                        onClick = component::onSettingsTabClicked,
                        icon = Icons.Default.Settings,
                        label = stringResource(id = R.string.settings_tab),
                    )
                }
            }
        }
    }
}
