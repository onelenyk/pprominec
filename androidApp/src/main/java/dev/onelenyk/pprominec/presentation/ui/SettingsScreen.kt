package dev.onelenyk.pprominec.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.onelenyk.pprominec.presentation.components.settings.SettingsComponent
import dev.onelenyk.pprominec.presentation.ui.components.AppToolbar

@Composable
fun SettingsScreen(component: SettingsComponent) {
    val state by component.state.collectAsState()

    Scaffold(
        topBar = { AppToolbar(title = "Settings") }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            Row {
                Text("Dark Mode")
                Switch(
                    checked = state.isDarkMode,
                    onCheckedChange = component::onToggleDarkMode
                )
            }
            Row {
                Text("Enable Notifications")
                Switch(
                    checked = state.notificationsEnabled,
                    onCheckedChange = component::onToggleNotifications
                )
            }
        }
    }
} 