package dev.onelenyk.pprominec.presentation.ui.screens.mapsettings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.onelenyk.pprominec.presentation.components.main.MapSettingsComponent
import dev.onelenyk.pprominec.presentation.ui.MapMode
import dev.onelenyk.pprominec.presentation.ui.AppScreen
import dev.onelenyk.pprominec.presentation.ui.components.AppToolbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSettingsScreen(component: MapSettingsComponent) {
    val state by component.state.collectAsState()

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        result.data?.data?.let { uri ->
            component.onFileSelected(uri)
        }
    }

    AppScreen(
        toolbar = {
            AppToolbar(
                title = "Map Settings",
                showBack = true,
                onBackClick = { component.onBack() },
            )
        },
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                ) {
                    // Map Mode Selection
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(8.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Map Mode",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                Button(
                                    onClick = { component.setMapMode(MapMode.ONLINE) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (state.mapMode == MapMode.ONLINE) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        },
                                    ),
                                ) {
                                    Text("Online")
                                }
                                Button(
                                    onClick = { component.setMapMode(MapMode.OFFLINE) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (state.mapMode == MapMode.OFFLINE) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        },
                                    ),
                                ) {
                                    Text("Offline")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Map Files List
                    if (state.mapMode == MapMode.OFFLINE) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(8.dp),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Offline Map Files",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Action buttons in a responsive layout
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    // Primary actions row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Button(
                                            onClick = {
                                                val intent =
                                                    Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                                        addCategory(Intent.CATEGORY_OPENABLE)
                                                        type = "*/*"
                                                        putExtra(
                                                            Intent.EXTRA_MIME_TYPES,
                                                            arrayOf(
                                                                "application/x-sqlite3",
                                                                "application/octet-stream",
                                                                "*/*",
                                                            ),
                                                        )
                                                    }
                                                documentPickerLauncher.launch(intent)
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                            ),
                                        ) {
                                            Icon(
                                                imageVector = androidx.compose.material.icons.Icons.Default.Add,
                                                contentDescription = "Add file",
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Add File")
                                        }

                                        Button(
                                            onClick = { component.refreshFiles() },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary,
                                            ),
                                        ) {
                                            Icon(
                                                imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                                                contentDescription = "Refresh files",
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Refresh")
                                        }
                                    }

                                    // Clear all button (only when files exist)
                                    if (state.mapFiles.isNotEmpty()) {
                                        Button(
                                            onClick = { component.clearFolder() },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error,
                                            ),
                                        ) {
                                            Icon(
                                                imageVector = androidx.compose.material.icons.Icons.Default.Clear,
                                                contentDescription = "Clear all files",
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Clear All Files")
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (state.mapFiles.isEmpty()) {
                                    Text(
                                        text = "No offline map files available",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        state.mapFiles.forEach { fileInfo ->
                                            val isSelected =
                                                state.selectedMapFile?.uid == fileInfo.uid
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clickable { component.selectFile(fileInfo) },
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) {
                                                        MaterialTheme.colorScheme.primaryContainer
                                                    } else {
                                                        MaterialTheme.colorScheme.surface
                                                    },
                                                ),
                                                border = if (isSelected) {
                                                    BorderStroke(
                                                        2.dp,
                                                        MaterialTheme.colorScheme.primary,
                                                    )
                                                } else {
                                                    null
                                                },
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = fileInfo.name,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = if (isSelected) {
                                                                MaterialTheme.colorScheme.onPrimaryContainer
                                                            } else {
                                                                MaterialTheme.colorScheme.onSurface
                                                            },
                                                        )
                                                        Text(
                                                            text = "UID: ${fileInfo.uid}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = if (isSelected) {
                                                                MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                                    alpha = 0.7f,
                                                                )
                                                            } else {
                                                                MaterialTheme.colorScheme.onSurfaceVariant
                                                            },
                                                        )
                                                        Text(
                                                            text = "Type: ${fileInfo.type.extension}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = if (isSelected) {
                                                                MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                                    alpha = 0.7f,
                                                                )
                                                            } else {
                                                                MaterialTheme.colorScheme.onSurfaceVariant
                                                            },
                                                        )
                                                        Text(
                                                            text = "Size: ${fileInfo.size} bytes",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = if (isSelected) {
                                                                MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                                    alpha = 0.7f,
                                                                )
                                                            } else {
                                                                MaterialTheme.colorScheme.onSurfaceVariant
                                                            },
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            component.deleteFile(fileInfo.uid)
                                                        },
                                                    ) {
                                                        Icon(
                                                            imageVector = androidx.compose.material.icons.Icons.Default.Clear,
                                                            contentDescription = "Remove file",
                                                            tint = if (isSelected) {
                                                                MaterialTheme.colorScheme.onPrimaryContainer
                                                            } else {
                                                                MaterialTheme.colorScheme.onSurface
                                                            },
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (state.loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        },
    )
}
