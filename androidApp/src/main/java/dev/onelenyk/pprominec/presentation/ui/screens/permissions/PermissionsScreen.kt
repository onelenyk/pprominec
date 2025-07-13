package dev.onelenyk.pprominec.presentation.ui.screens.permissions

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.onelenyk.pprominec.presentation.components.permissions.PermissionsComponent
import dev.onelenyk.pprominec.presentation.ui.AppScreen
import dev.onelenyk.pprominec.presentation.components.permissions.PermissionsManager
import dev.onelenyk.pprominec.presentation.ui.components.AppToolbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(permissionsComponent: PermissionsComponent) {
    AppScreen(
        toolbar = {
            AppToolbar(title = "Permissions", showBack = true, onBackClick = {
                permissionsComponent.onBack()
            })
        },
        content = {
            PermissionsContent(
                modifier = Modifier,
                permissionsComponent = permissionsComponent,
            )
        },
    )
}

@Composable
fun PermissionsContent(
    modifier: Modifier = Modifier,
    permissionsComponent: PermissionsComponent,
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionsScreenState by permissionsComponent.permissionsScreenState.collectAsStateWithLifecycle()

    var showRationaleDialog by remember { mutableStateOf<PermissionsManager.Permission?>(null) }
    var showSettingsDialog by remember { mutableStateOf<PermissionsManager.Permission?>(null) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
        ) { result ->
            val permission =
                PermissionsManager.Permission.entries.first {
                    it.manifestPermissions.containsAll(result.keys)
                }
            val permissionState =
                PermissionsManager.SimplePermissionsManager.checkPermissionWithRationale(
                    activity,
                    permission,
                )

            permissionsComponent.onNewPermissionState(mapOf(permission to permissionState))
        }

    // Refresh permissions on every onResume
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    permissionsComponent.checkPermissionsState(activity)
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier =
        modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "Permission Manager",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Current permission states",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OverallStatusChip(state = permissionsScreenState)
            }
        }

        // Check Button
        Button(
            onClick = { permissionsComponent.checkPermissionsState(context) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Check Permissions")
        }

        // Revoke Permissions Button
        OutlinedButton(
            onClick = { permissionsComponent.openAppSettings(context) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Open App Settings")
        }

        // Permissions List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(permissionsScreenState.permissions) { index, permission ->
                PermissionItemCard(
                    permission = permission,
                    onRequestPermission = {
                        when (permission.state) {
                            PermissionsManager.PermissionState.PERMANENTLY_DENIED -> {
                                showSettingsDialog = permission.type
                            }

                            PermissionsManager.PermissionState.REQUIRE_RATIONALE -> {
                                showRationaleDialog = permission.type
                            }

                            else -> {
                                permissionsComponent.requestPermission(permission.type, context)
                                permissionLauncher.launch(permission.type.manifestPermissions.toTypedArray())
                            }
                        }
                    },
                )
            }
        }
    }

    // Rationale Dialog
    showRationaleDialog?.let { permission ->
        AlertDialog(
            onDismissRequest = { showRationaleDialog = null },
            title = { Text("Permission Required") },
            text = {
                Text(
                    "This permission is required for the app to function properly. " +
                        "Please grant the permission to continue.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRationaleDialog = null
                        permissionsComponent.requestPermission(permission, context)
                        permissionLauncher.launch(permission.manifestPermissions.toTypedArray())
                    },
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationaleDialog = null }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Settings Dialog
    showSettingsDialog?.let { permission ->
        AlertDialog(
            onDismissRequest = { showSettingsDialog = null },
            title = { Text("Permission Denied") },
            text = {
                Text(
                    "This permission has been permanently denied. " +
                        "You need to enable it manually in the app settings.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSettingsDialog = null
                        permissionsComponent.openAppSettings(context)
                    },
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
fun PermissionItemCard(
    permission: PermissionsManager.AppPermission,
    onRequestPermission: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
        CardDefaults.cardColors(
            containerColor =
            when (permission.state) {
                PermissionsManager.PermissionState.GRANTED -> MaterialTheme.colorScheme.surfaceVariant
                PermissionsManager.PermissionState.DENIED ->
                    MaterialTheme.colorScheme.errorContainer.copy(
                        alpha = 0.1f,
                    )

                PermissionsManager.PermissionState.REQUIRE_RATIONALE ->
                    MaterialTheme.colorScheme.primaryContainer.copy(
                        alpha = 0.3f,
                    )

                PermissionsManager.PermissionState.PERMANENTLY_DENIED ->
                    MaterialTheme.colorScheme.errorContainer.copy(
                        alpha = 0.2f,
                    )

                PermissionsManager.PermissionState.REQUESTING ->
                    MaterialTheme.colorScheme.primaryContainer.copy(
                        alpha = 0.5f,
                    )

                else -> MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = permission.type.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Permission: ${permission.type.manifestPermissions.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.height(4.dp))
                StatusChip(state = permission.state)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Request Button
            if (permission.state != PermissionsManager.PermissionState.GRANTED) {
                Button(
                    onClick = onRequestPermission,
                    enabled = permission.state != PermissionsManager.PermissionState.REQUESTING,
                ) {
                    if (permission.state == PermissionsManager.PermissionState.REQUESTING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(
                            when (permission.state) {
                                PermissionsManager.PermissionState.PERMANENTLY_DENIED -> "Settings"
                                else -> "Request"
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(state: PermissionsManager.PermissionState) {
    val (text, color) =
        when (state) {
            PermissionsManager.PermissionState.GRANTED -> "Granted" to Color(0xFF4CAF50)
            PermissionsManager.PermissionState.DENIED -> "Denied" to Color(0xFFFF5722)
            PermissionsManager.PermissionState.REQUIRE_RATIONALE ->
                "Requires Rationale" to
                    Color(
                        0xFFFF9800,
                    )

            PermissionsManager.PermissionState.PERMANENTLY_DENIED ->
                "Permanently Denied" to
                    Color(
                        0xFFE91E63,
                    )

            PermissionsManager.PermissionState.REQUESTING -> "Requesting..." to Color(0xFF2196F3)
            PermissionsManager.PermissionState.UNKNOWN -> "Unknown" to Color(0xFF9E9E9E)
        }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(vertical = 2.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

@Composable
fun OverallStatusChip(state: PermissionsManager.PermissionsScreenState) {
    val (text, color) =
        when {
            state.canContinue -> "All Mandatory Permissions Granted" to Color(0xFF4CAF50)
            state.permissions.any { it.state == PermissionsManager.PermissionState.PERMANENTLY_DENIED } ->
                "Some Permissions Permanently Denied" to
                    Color(
                        0xFFE91E63,
                    )

            state.permissions.any { it.state == PermissionsManager.PermissionState.DENIED } ->
                "Some Permissions Denied" to
                    Color(
                        0xFFFF5722,
                    )

            state.permissions.any { it.state == PermissionsManager.PermissionState.REQUIRE_RATIONALE } ->
                "Some Require Rationale" to
                    Color(
                        0xFFFF9800,
                    )

            state.permissions.any { it.state == PermissionsManager.PermissionState.REQUESTING } ->
                "Requesting Permissions..." to
                    Color(
                        0xFF2196F3,
                    )

            else -> "Mixed Status" to Color(0xFF9E9E9E)
        }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Medium,
        )
    }
}
