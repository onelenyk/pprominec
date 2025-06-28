package dev.onelenyk.pprominec.presentation.ui
/*

import dev.onelenyk.pprominec.R
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.backhandler.BackCallback
import dev.onelenyk.pprominec.presentation.ui.theme.Typography
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import java.util.Locale

// Define missing typography styles - these are just references to the Typography object
val H1 = Typography.H1
val H2 = Typography.H2
val T2 = Typography.T2
val H4 = Typography.H4

// Define missing composable
@Composable
fun AppScreen(content: @Composable () -> Unit) {
    content()
}

object SimplePermissionsManager {
    fun isIgnoringBatteryOptimizations(activity: ComponentActivity): Boolean {
        val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(activity.packageName)
    }

    fun checkPermission(
        activity: ComponentActivity,
        permission: Permission,
        isFirstRequest: Boolean = false,
    ) = when {
        activity.checkSelfPermission(permission.manifestPermissions.first()) == PackageManager.PERMISSION_GRANTED -> PermissionState.GRANTED
        activity.shouldShowRequestPermissionRationale(permission.manifestPermissions.first()) -> if (isFirstRequest) PermissionState.UNKNOWN else PermissionState.REQUIRE_RATIONALE
        else -> if (isFirstRequest) PermissionState.UNKNOWN else PermissionState.DENIED
    }

    fun checkPermission(context: Context, permission: Permission) = when {
        context.checkSelfPermission(permission.manifestPermissions.first()) == PackageManager.PERMISSION_GRANTED -> PermissionState.GRANTED
        else -> PermissionState.DENIED
    }
}


enum class Permission(val manifestPermissions: List<String>, val isMandatory: Boolean = true) {
    CAMERA(listOf(Manifest.permission.CAMERA)),

    GPS(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ),
    ),

    @RequiresApi(29)
    GPS_BACKGROUND(listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)),

    @RequiresApi(33)
    NOTIFICATIONS(listOf(Manifest.permission.POST_NOTIFICATIONS)),

    @RequiresApi(31)
    SCHEDULE_EXACT_ALARM(listOf(Manifest.permission.SCHEDULE_EXACT_ALARM)),

    */
/**
     * Not a real permission, please, don't use in requestPermissions. It's here just for a useful interface of
     * getRationale(), when() with enums etc.
     *//*

    IGNORE_BATTERY_OPTIMIZATIONS(emptyList(), isMandatory = false),
}

enum class PermissionState {
    GRANTED,
    DENIED,
    REQUIRE_RATIONALE,
    UNKNOWN,
}

fun PermissionState.granted(): Boolean = this == PermissionState.GRANTED

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
        get() = type in listOf(Permission.GPS, Permission.NOTIFICATIONS, Permission.GPS_BACKGROUND)
}

data class PermissionsScreenState(
    val permissions: List<AppPermission> = emptyList(),
    val rationaleRequest: AppPermission? = null,
    val isFirstLaunch: Boolean = true,
) {
    val canContinue: Boolean
        get() = permissions.filter { it.type.isMandatory }
            .all { it.state == PermissionState.GRANTED }

    val isGeolocationGranted: Boolean
        get() = permissions.first { it.type == Permission.GPS }
            .state == PermissionState.GRANTED
}


interface PermissionsComponent2 {
    val state: StateFlow<PermissionsScreenState>

    fun onNewPermissionState(state: Map<Permission, PermissionState>)
    fun showRationaleDialogFor(permission: AppPermission)
    fun dismissRationaleDialog()
    fun onContinueClicked()
}

class DefaultPermissions2Component(
    private val componentContext: ComponentContext,
    private val goBack: () -> Unit,
) : PermissionsComponent2, ComponentContext by componentContext {

    private val _state = MutableStateFlow(PermissionsScreenState())
    override val state = _state.asStateFlow()

    private val backCallback = BackCallback {
        // doing nothing
    }

    init {
        backHandler.register(backCallback)

        val permissions = mutableListOf<AppPermission>()
        permissions.add(
            AppPermission(
                type = Permission.GPS,
                icon = VanongoIcons.MapOutline,
                title = R.string.dialog_telemetry_item_permission_location,
                description = R.string.dialog_telemetry_location_description,
                rationaleTitle = R.string.permissions_location_alert_title,
                rationaleDescription = R.string.permissions_location_alert_message,
                state = PermissionState.UNKNOWN,
            ),
        )
        if (Build.VERSION.SDK_INT >= 33) {
            permissions.add(
                AppPermission(
                    type = Permission.NOTIFICATIONS,
                    icon = VanongoIcons.Notification,
                    title = R.string.dialog_telemetry_item_permission_notifications,
                    description = R.string.dialog_telemetry_notifications_description,
                    rationaleTitle = R.string.permissions_notifications_alert_title,
                    rationaleDescription = R.string.permissions_notifications_alert_message,
                    state = PermissionState.UNKNOWN,
                ),
            )
        }
        _state.update { it.copy(permissions = permissions) }
    }

    override fun showRationaleDialogFor(permission: AppPermission) {
        _state.update { it.copy(rationaleRequest = permission) }
    }

    override fun dismissRationaleDialog() {
        _state.update { it.copy(rationaleRequest = null) }
    }

    override fun onNewPermissionState(state: Map<Permission, PermissionState>) {
        val permissions = _state.value.permissions
        val newList = mutableListOf<AppPermission>()
        newList.addAll(
            permissions.map {
                if (it.type in state.keys) {
                    val permissionState = state[it.type]
                    val newPermissionState =
                        if (permissionState == PermissionState.UNKNOWN) it.state else permissionState
                    it.copy(state = newPermissionState ?: PermissionState.UNKNOWN)
                } else {
                    it
                }
            },
        )
        _state.update { it.copy(permissions = newList) }
    }

    override fun onContinueClicked() {
        goBack()
    }

    init {
    }
}


@Composable
fun PermissionsScreen2(component: PermissionsComponent2) {
    val context = LocalContext.current
    val state by component.state.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val activity = context as ComponentActivity
        val permission =
            Permission.entries.first { it.manifestPermissions.containsAll(result.keys) }
        val permissionState = SimplePermissionsManager.checkPermission(activity, permission)

        component.onNewPermissionState(mapOf(permission to permissionState))
    }

    AppScreen {
        RootContent(
            component = component,
            permissionLauncher = permissionLauncher,
        )

        if (state.rationaleRequest != null) {
            RationaleDialog(
                permission = state.rationaleRequest!!,
                permissionLauncher = permissionLauncher,
                onDismiss = { component.dismissRationaleDialog() },
            )
        }
    }
}

@Composable
fun RationaleDialog(
    permission: AppPermission,
    permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val buttonTitle = if (permission.state == PermissionState.DENIED) {
        R.string.permissions_alert_go_to_settings
    } else {
        R.string.allow
    }
    AppAlertDialog(
        title = stringResource(id = permission.rationaleTitle),
        message = stringResource(id = permission.rationaleDescription),
        actions = listOf(
            {
                DialogAction(
                    title = stringResource(R.string.permissions_dont_allow),
                    onClick = onDismiss,
                )
            },
            {
                DialogAction(
                    title = stringResource(buttonTitle),
                    onClick = {
                        onDismiss()
                        if (permission.state == PermissionState.REQUIRE_RATIONALE) {
                            permissionLauncher.launch(permission.type.manifestPermissions.toTypedArray())
                        } else {
                            val intent =
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                            context.startActivity(intent)
                        }
                    },
                )
            },
        ),
        onDismissRequest = onDismiss,
    )
}

@Composable
fun loadDrawableFromResource(@DrawableRes resId: Int): ImageBitmap? =
    ContextCompat.getDrawable(
        LocalContext.current,
        resId,
    )?.toBitmap()?.asImageBitmap()


@Composable
fun RootContent(
    component: PermissionsComponent2,
    permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    val state by component.state.collectAsState()
    val scrollState = rememberScrollState()

    val context = LocalContext.current

    val painterRes = dev.onelenyk.pprominec.R.drawable.ic_launcher_foreground
    val painterResBitmap = loadDrawableFromResource(painterRes)

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(scrollState)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))
        Column(
            Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (painterResBitmap != null) {
                Image(
                    modifier = Modifier.size(64.dp),
                    bitmap = painterResBitmap,
                    contentDescription = "",
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(id = R.string.permissions_screen_title),
                textAlign = TextAlign.Center,
                style = H1,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(id = R.string.permissions_screen_description),
                textAlign = TextAlign.Center,
                style = T2,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(BorderRadiusBig)
                .background(color = MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = BorderRadiusBig,
                ),
        ) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                state.permissions.forEachIndexed { index, permission ->
                    val isEnabled = if (permission.type != Permission.GPS_BACKGROUND) {
                        true
                    } else {
                        state.isGeolocationGranted
                    }
                    val alpha = if (isEnabled) 1f else 0.6f
                    Column(
                        modifier = Modifier
                            .alpha(alpha)
                            .clickable(
                                enabled = isEnabled,
                            ) {
                                val permissions = permission.type.manifestPermissions
                                if (permission.type == Permission.SCHEDULE_EXACT_ALARM) {
                                    val success = tryOpenAlarmSettings(context)

                                    if (success) {
                                        return@clickable
                                    }
                                }

                                if (permission.type == Permission.IGNORE_BATTERY_OPTIMIZATIONS) {
                                    val success = tryToOpenOurAppOptimizationSettings(context)

                                    if (success) {
                                        return@clickable
                                    }

                                    // no success opening the shortcut settings - open the complex default settings
                                    val intent = Intent()
                                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                    intent.setData(Uri.parse("package:${context.packageName}"))
                                    context.startActivity(intent)
                                } else {
                                    if (permission.state in listOf(
                                            PermissionState.REQUIRE_RATIONALE,
                                            PermissionState.DENIED,
                                        )
                                    ) {
                                        component.showRationaleDialogFor(permission)
                                    } else {
                                        permissionLauncher.launch(permissions.toTypedArray())
                                    }
                                }
                            },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = rememberVectorPainter(permission.icon),
                                contentDescription = "",
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = stringResource(permission.title),
                                )
                                Text(
                                    text = stringResource(permission.description),
                                )
                            }

                        }
                        if (index != state.permissions.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))

    }

    LaunchedEffect(lifecycleState) {
        val isFirstLaunch = state.isFirstLaunch
        val activity = context as ComponentActivity

        val locationPermissionState =
            SimplePermissionsManager.checkPermission(
                activity,
                Permission.GPS,
                isFirstLaunch,
            )
        val notificationsPermissionState =
            SimplePermissionsManager.checkPermission(
                activity,
                Permission.NOTIFICATIONS,
                isFirstLaunch,
            )
        val backgroundWorkPermissionState =
            SimplePermissionsManager.checkPermission(
                activity,
                Permission.GPS_BACKGROUND,
                isFirstLaunch,
            )

        val scheduleAlarmPermissionState =
            SimplePermissionsManager.checkPermission(
                activity,
                Permission.SCHEDULE_EXACT_ALARM,
                isFirstLaunch,
            )

        val ignoringBatteryOptimizationsPermissionState =
            if (SimplePermissionsManager.isIgnoringBatteryOptimizations(activity)) {
                PermissionState.GRANTED
            } else {
                PermissionState.UNKNOWN
            }

        val resultsMap = mapOf(
            Permission.GPS to locationPermissionState,
            Permission.NOTIFICATIONS to notificationsPermissionState,
            Permission.GPS_BACKGROUND to backgroundWorkPermissionState,
            Permission.IGNORE_BATTERY_OPTIMIZATIONS to ignoringBatteryOptimizationsPermissionState,
            Permission.SCHEDULE_EXACT_ALARM to scheduleAlarmPermissionState,
        )

        component.onNewPermissionState(resultsMap)
    }
}

private fun tryOpenAlarmSettings(context: Context): Boolean {
    val intent = if (Build.VERSION.SDK_INT >= 31) {
        Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
    } else {
        return false
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        return false
    }
    return true
}

private fun tryToOpenOurAppOptimizationSettings(context: Context): Boolean {
    val intent = Intent()
    val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
    when (manufacturer) {
        "xiaomi" -> {
            intent.setClassName(
                "com.miui.powerkeeper",
                "com.miui.powerkeeper.ui.HiddenAppsConfigActivity",
            ).putExtra("package_name", context.packageName)
                .putExtra("package_label", context.getString(dev.onelenyk.pprominec.R.string.app_name))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        else -> return false
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Timber.e(e, "Can't open MIUI settings.")
        return false
    }
    return true
}

// Add regular icons for permissions
object VanongoIcons {
    val MapOutline = Icons.Default.Home
    val Notification = Icons.Default.Notifications
}

@Composable
fun AppAlertDialog(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    image: ImageVector? = null,
    actions: List<@Composable () -> Unit> = listOf(),
    actionsOrientation: DialogActionsOrientation = DialogActionsOrientation.HORIZONTAL,
    dialogProperties: DialogProperties = DialogProperties(
        usePlatformDefaultWidth = true,
    ),
    onDismissRequest: () -> Unit,
) {
    Dialog(
        properties = dialogProperties,
        onDismissRequest = onDismissRequest,
    ) {
        Box(modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(alignment = Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = BorderRadiusBig,
                    )
                    .padding(all = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = title,
                    style = H2,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (image != null) {
                    Image(
                        modifier = Modifier
                            .padding(vertical = 14.dp)
                            .size(150.dp),
                        painter = rememberVectorPainter(image),
                        contentDescription = "",
                    )
                }
                if (message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = message,
                        style = T2,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                when (actionsOrientation) {
                    DialogActionsOrientation.HORIZONTAL -> Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        actions.forEachIndexed { index, action ->
                            action()
                            if (index != actions.lastIndex) {
                                Spacer(Modifier.width(24.dp))
                            }
                        }
                    }

                    DialogActionsOrientation.VERTICAL -> Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalAlignment = Alignment.End,
                    ) {
                        actions.forEach {
                            it()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DialogAction(
    title: String,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
) {
    Text(
        modifier = Modifier.clickable { onClick() },
        text = title,
        style = H4,
        color = color,
    )
}

enum class DialogActionsOrientation {
    HORIZONTAL,
    VERTICAL,
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AppAlertDialogPreview(
    @PreviewParameter(AppAlertDialogProvider::class) configuration: DialogConfiguration,
) {
    AppAlertDialog(
        title = configuration.title,
        message = configuration.message,
        actions = configuration.actions,
        image = configuration.image,
        actionsOrientation = configuration.orientation,
    ) { }
}

private class AppAlertDialogProvider : PreviewParameterProvider<DialogConfiguration> {
    override val values: Sequence<DialogConfiguration> = sequenceOf(
        DialogConfiguration(
            orientation = DialogActionsOrientation.HORIZONTAL,
            actions = listOf(),
        ),
        DialogConfiguration(
            orientation = DialogActionsOrientation.HORIZONTAL,
            actions = listOf(
                { DialogAction(title = "Done", onClick = {}) },
            ),
        ),
        DialogConfiguration(
            orientation = DialogActionsOrientation.HORIZONTAL,
            actions = listOf(
                { DialogAction(title = "Done", onClick = {}) },
                { DialogAction(title = "Cancel", onClick = {}) },
            ),
        ),
        DialogConfiguration(
            message = "Message",
            title = "title",
            orientation = DialogActionsOrientation.HORIZONTAL,
            actions = listOf(
                { DialogAction(title = "Reject", onClick = {}) },
                { DialogAction(title = "Cancel", onClick = {}) },
            ),
        ),
        DialogConfiguration(
            message = "",
            orientation = DialogActionsOrientation.HORIZONTAL,
            actions = listOf(
                { DialogAction(title = "Done", onClick = {}, color = Color.Red) },
                { DialogAction(title = "Cancel", onClick = {}) },
            ),
        ),
        DialogConfiguration(
            orientation = DialogActionsOrientation.VERTICAL,
            actions = listOf(
                { DialogAction(title = "Done", onClick = {}) },
            ),
        ),
        DialogConfiguration(
            orientation = DialogActionsOrientation.VERTICAL,
            actions = listOf(
                { DialogAction(title = "Done", onClick = {}) },
                { DialogAction(title = "Cancel", onClick = {}) },
            ),
        ),
        DialogConfiguration(
            message = "",
            orientation = DialogActionsOrientation.VERTICAL,
            actions = listOf(
                { DialogAction(title = "Done", onClick = {}) },
                { DialogAction(title = "Cancel", onClick = {}) },
            ),
        ),
    )
}

data class DialogConfiguration(
    val title: String = "A Short Title Is Best",
    val message: String = "A message should be a short,complete sentence.",
    val image: ImageVector? = null,
    val orientation: DialogActionsOrientation,
    val actions: List<@Composable () -> Unit>,
)
*/
