package dev.onelenyk.pprominec.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.onelenyk.pprominec.presentation.components.settings.SettingsComponent
import dev.onelenyk.pprominec.presentation.ui.components.AppToolbar

@Composable
fun SettingsScreen(component: SettingsComponent) {
    val state by component.state.collectAsState()

    AppScreen(
        toolbar = { AppToolbar(title = "Settings") },
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp),
        ) {
            // Dark Mode Setting
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Dark Mode",
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = state.isDarkMode,
                    onCheckedChange = component::onToggleDarkMode,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notifications Setting
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Enable Notifications",
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = state.notificationsEnabled,
                    onCheckedChange = component::onToggleNotifications,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Permissions Button
            Button(
                onClick = component::onPermissionsClicked,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Manage Permissions")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Map Files Button
            Button(
                onClick = component::onMapSettingsClicked,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Map Settings")
            }
        }
    }
}
