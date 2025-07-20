package dev.onelenyk.pprominec.presentation.ui.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import dev.onelenyk.pprominec.presentation.components.main.Mode
import dev.onelenyk.pprominec.presentation.components.main.UsersMarkersComponent
import dev.onelenyk.pprominec.presentation.components.main.UsersMarkersEffect
import dev.onelenyk.pprominec.presentation.components.main.UsersMarkersIntent
import dev.onelenyk.pprominec.presentation.mvi.MviScreen
import dev.onelenyk.pprominec.presentation.ui.AppDialog
import dev.onelenyk.pprominec.presentation.ui.MapMarker

@Composable
private fun MarkerItem(
    index: Int,
    marker: MapMarker,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var title by remember { mutableStateOf(marker.title) }
    var position by remember { mutableStateOf("${marker.latitude}, ${marker.longitude}") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$index.",
            modifier = Modifier.width(32.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        ) {
            if (isEditing) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = position,
                    onValueChange = { position = it },
                    label = { Text("Position") },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Text(text = title)
                Text(
                    text = position,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Row {
            IconButton(
                onClick = if (isEditing) onSaveClick else onEditClick,
            ) {
                Icon(
                    if (isEditing) Icons.Default.AddCircle else Icons.Default.Edit,
                    contentDescription = if (isEditing) "Save" else "Edit",
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
fun UsersMarkersContent(
    markers: List<MapMarker>,
    editingMarkerId: String?,
    onEditMarker: (String) -> Unit,
    onSaveMarker: (MapMarker) -> Unit,
    onDeleteMarker: (String) -> Unit,
    onAddMarker: () -> Unit,
    onClose: () -> Unit,
) {
    Card(
        modifier = Modifier.padding(36.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        ),
        elevation = CardDefaults.cardElevation(6.dp),

    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("User Markers", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                itemsIndexed(markers) { index, marker ->
                    MarkerItem(
                        index = index + 1,
                        marker = marker,
                        isEditing = editingMarkerId == marker.id,
                        onEditClick = { onEditMarker(marker.id) },
                        onSaveClick = {
                            onSaveMarker(marker)
                        },
                        onDeleteClick = { onDeleteMarker(marker.id) },
                    )
                    if (index < markers.size - 1) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAddMarker,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add marker")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New Marker")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Text(
                        "Close",
                        style = TextStyle.Default.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
fun UsersMarkersDialog(
    component: UsersMarkersComponent,
) {
    MviScreen(
        component = component,
        onEffect = { effect ->
            when (effect) {
                is UsersMarkersEffect.CloseScreen -> {
                    // No-op: dialog will be closed by parent
                }
            }
        },
    ) { state, dispatch ->
        val mode = component.mode
        // Determine selected marker from parent state (if any)
        val selectedMarkerId = when (mode) {
            Mode.CHOOSE -> state.markers.find { it.id == state.currentLocation?.id }?.id ?: state.currentLocation?.id
            else -> null
        }
        AppDialog(onDismissRequest = { dispatch(UsersMarkersIntent.CloseScreen) }, content = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = if (mode == Mode.CHOOSE) "Оберіть маркер" else "Мої маркери",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Divider()
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.uiMarkers) { marker ->
                            val isSelected = selectedMarkerId == marker.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.primary.copy(
                                                alpha = 0.15f,
                                            )
                                        } else {
                                            Color.Transparent
                                        },
                                    )
                                    .clickable(enabled = mode == Mode.CHOOSE) {
                                        dispatch(UsersMarkersIntent.SelectMarker(marker))
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("${marker.title} &${marker.code}", style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        text = "%.6f, %.6f".format(
                                            marker.latitude,
                                            marker.longitude,
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        if (mode == Mode.CHOOSE && selectedMarkerId != null) {
                            TextButton(onClick = {
                                dispatch(UsersMarkersIntent.SelectMarker(null))
                            }) {
                                Text("Зняти вибір")
                            }
                        }
                        Row {
                            TextButton(onClick = { dispatch(UsersMarkersIntent.CloseScreen) }) {
                                Text("Відмінити")
                            }
                        }
                    }
                }
            }
        })
    }
}
