package dev.onelenyk.pprominec.presentation.components.settings

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsState(
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
)

interface SettingsComponent {
    val state: StateFlow<SettingsState>

    fun onToggleDarkMode(isDarkMode: Boolean)

    fun onToggleNotifications(enabled: Boolean)

    fun onPermissionsClicked()
}

class DefaultSettingsComponent(
    componentContext: ComponentContext,
    private val openPermissions: () -> Unit,
) : SettingsComponent, ComponentContext by componentContext {
    private val _state = MutableStateFlow(SettingsState())
    override val state: StateFlow<SettingsState> = _state.asStateFlow()

    override fun onToggleDarkMode(isDarkMode: Boolean) {
        _state.value = _state.value.copy(isDarkMode = isDarkMode)
    }

    override fun onToggleNotifications(enabled: Boolean) {
        _state.value = _state.value.copy(notificationsEnabled = enabled)
    }

    override fun onPermissionsClicked() {
        openPermissions()
    }
}
