package dev.onelenyk.pprominec.presentation.components.permissions

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PermissionsComponent {
    val state: StateFlow<PermissionsState>
    fun checkPermission(permission: String, context: Context)
    fun requestPermission(permission: String, context: Context)
}

sealed class PermissionsState {
    object Idle : PermissionsState()
    object Granted : PermissionsState()
    object Denied : PermissionsState()
    object Requesting : PermissionsState()
}

class DefaultPermissionsComponent(
    componentContext: ComponentContext
) : PermissionsComponent, ComponentContext by componentContext {
    private val _state = MutableStateFlow<PermissionsState>(PermissionsState.Idle)
    override val state: StateFlow<PermissionsState> = _state.asStateFlow()

    override fun checkPermission(permission: String, context: Context) {
        val granted = context.checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
        _state.value = if (granted) PermissionsState.Granted else PermissionsState.Denied
    }

    override fun requestPermission(permission: String, context: Context) {
        _state.value = PermissionsState.Requesting
        // Actual request should be handled in the UI layer (Composable) via launcher
    }
} 