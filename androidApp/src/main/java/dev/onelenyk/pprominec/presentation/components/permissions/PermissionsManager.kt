package dev.onelenyk.pprominec.presentation.components.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PermissionsManager {

    private val _stateFlow = MutableStateFlow(initState())
    val stateFlow: StateFlow<PermissionsScreenState> = _stateFlow.asStateFlow()

    fun initState(): PermissionsScreenState {
        val permissions = listOf(
            AppPermission(
                type = Permission.GPS,
                icon = Icons.Default.LocationOn,
                title = android.R.string.unknownName, // Placeholder
                description = android.R.string.unknownName, // Placeholder
                state = PermissionState.UNKNOWN
            ),
            AppPermission(
                type = Permission.NOTIFICATIONS,
                icon = Icons.Default.Notifications, // Placeholder icon
                title = android.R.string.unknownName, // Placeholder
                description = android.R.string.unknownName, // Placeholder
                state = PermissionState.UNKNOWN
            )
        )

        return PermissionsScreenState(
            permissions = permissions,
            rationaleRequest = null
        )
    }

    fun checkPermissionsState(activity: ComponentActivity) {
        val currentState = _stateFlow.value
        val updatedPermissions = currentState.permissions.map { permission ->
            permission.copy(
                state = SimplePermissionsManager.checkPermissionWithRationale(activity, permission.type)
            )
        }

        _stateFlow.value = currentState.copy(
            permissions = updatedPermissions
        )
    }

    fun requestPermission(permission: Permission, context: Context) {
        val currentState = _stateFlow.value
        val updatedPermissions = currentState.permissions.map { perm ->
            if (perm.type == permission) {
                perm.copy(state = PermissionState.REQUESTING)
            } else {
                perm
            }
        }

        _stateFlow.value = currentState.copy(
            permissions = updatedPermissions
        )
    }

    fun onNewPermissionState(permissionStates: Map<Permission, PermissionState>) {
        val currentState = _stateFlow.value
        val updatedPermissions = currentState.permissions.map { permission ->
            val newState = permissionStates[permission.type]
            if (newState != null) {
                permission.copy(state = newState)
            } else {
                permission
            }
        }

        _stateFlow.value = currentState.copy(
            permissions = updatedPermissions
        )
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    object SimplePermissionsManager {
        fun isIgnoringBatteryOptimizations(activity: ComponentActivity): Boolean {
            val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
            return powerManager.isIgnoringBatteryOptimizations(activity.packageName)
        }

        fun checkPermission(
            activity: ComponentActivity,
            permission: Permission,
        ): PermissionState {
            val manifestPermission = permission.manifestPermissions.first()
            return when {
                activity.checkSelfPermission(manifestPermission) == PackageManager.PERMISSION_GRANTED ->
                    PermissionState.GRANTED
                activity.shouldShowRequestPermissionRationale(manifestPermission) ->
                    PermissionState.REQUIRE_RATIONALE
                activity.checkSelfPermission(manifestPermission) == PackageManager.PERMISSION_DENIED ->
                    PermissionState.PERMANENTLY_DENIED
                else -> PermissionState.DENIED
            }
        }

        fun checkPermission(context: Context, permission: Permission): PermissionState {
            val manifestPermission = permission.manifestPermissions.first()
            return when {
                context.checkSelfPermission(manifestPermission) == PackageManager.PERMISSION_GRANTED ->
                    PermissionState.GRANTED
                context.checkSelfPermission(manifestPermission) == PackageManager.PERMISSION_DENIED ->
                    PermissionState.DENIED
                else -> PermissionState.UNKNOWN
            }
        }

        fun checkPermissionWithRationale(
            activity: ComponentActivity,
            permission: Permission,
        ): PermissionState {
            val manifestPermission = permission.manifestPermissions.first()
            return when {
                activity.checkSelfPermission(manifestPermission) == PackageManager.PERMISSION_GRANTED ->
                    PermissionState.GRANTED
                activity.shouldShowRequestPermissionRationale(manifestPermission) ->
                    PermissionState.REQUIRE_RATIONALE
                activity.checkSelfPermission(manifestPermission) == PackageManager.PERMISSION_DENIED ->
                    PermissionState.PERMANENTLY_DENIED
                else -> PermissionState.DENIED
            }
        }
    }

    data class AppPermission(
        val type: Permission,
        val icon: ImageVector,
        @StringRes val title: Int,
        @StringRes val description: Int,
        @StringRes val rationaleTitle: Int = -1,
        @StringRes val rationaleDescription: Int = -1,
        val state: PermissionState,
    ) {
        val isMandatory: Boolean
            get() = type in listOf(
                Permission.GPS,
                Permission.NOTIFICATIONS,
            )
    }

    data class PermissionsScreenState(
        val permissions: List<AppPermission> = emptyList(),
        val rationaleRequest: AppPermission? = null,
    ) {
        val canContinue: Boolean
            get() = permissions.filter { it.type.isMandatory }
                .all { it.state == PermissionState.GRANTED }

        val isGeolocationGranted: Boolean
            get() = permissions.first { it.type == Permission.GPS }
                .state == PermissionState.GRANTED
    }

    enum class Permission(val manifestPermissions: List<String>, val isMandatory: Boolean = true) {
        GPS(
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        ),

        @RequiresApi(33)
        NOTIFICATIONS(listOf(Manifest.permission.POST_NOTIFICATIONS)),
    }

    enum class PermissionState {
        GRANTED, DENIED, REQUIRE_RATIONALE, PERMANENTLY_DENIED, UNKNOWN, REQUESTING,
    }

    fun PermissionState.granted(): Boolean = this == PermissionState.GRANTED
} 