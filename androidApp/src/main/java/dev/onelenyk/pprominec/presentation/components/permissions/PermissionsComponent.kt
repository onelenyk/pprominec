package dev.onelenyk.pprominec.presentation.components.permissions

import android.content.Context
import androidx.activity.ComponentActivity
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.StateFlow

// Enhanced Interface
interface PermissionsComponent {
    val permissionsScreenState: StateFlow<PermissionsManager.PermissionsScreenState>

    fun checkPermissionsState(activity: ComponentActivity)
    fun requestPermission(permission: PermissionsManager.Permission, context: Context)
    fun onNewPermissionState(permissionStates: Map<PermissionsManager.Permission, PermissionsManager.PermissionState>)
    fun openAppSettings(context: Context)
    val onBack: () -> Unit
}

class DefaultPermissionsComponent(
    componentContext: ComponentContext,
    private val permissionsManager: PermissionsManager,
    override val onBack: () -> Unit = { }
) : PermissionsComponent, ComponentContext by componentContext {

    override val permissionsScreenState: StateFlow<PermissionsManager.PermissionsScreenState> =
        permissionsManager.stateFlow

    override fun checkPermissionsState(activity: ComponentActivity) {
        permissionsManager.checkPermissionsState(activity)
    }

    override fun requestPermission(permission: PermissionsManager.Permission, context: Context) {
        permissionsManager.requestPermission(permission, context)
    }

    override fun onNewPermissionState(permissionStates: Map<PermissionsManager.Permission, PermissionsManager.PermissionState>) {
        permissionsManager.onNewPermissionState(permissionStates)
    }

    override fun openAppSettings(context: Context) {
        permissionsManager.openAppSettings(context)
    }
}