@file:OptIn(ExperimentalMaterial3Api::class)

package dev.onelenyk.pprominec.presentation.ui.screens.main

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.onelenyk.pprominec.bussines.GeoCoordinate
import dev.onelenyk.pprominec.presentation.components.main.InputSource
import dev.onelenyk.pprominec.presentation.components.main.LocationButtonType
import dev.onelenyk.pprominec.presentation.components.main.MainComponent
import dev.onelenyk.pprominec.presentation.components.main.MainEffect
import dev.onelenyk.pprominec.presentation.components.main.MainIntent
import dev.onelenyk.pprominec.presentation.components.main.MainState
import dev.onelenyk.pprominec.presentation.components.main.OutputData
import dev.onelenyk.pprominec.presentation.components.main.Sample
import dev.onelenyk.pprominec.presentation.mvi.MviScreen
import dev.onelenyk.pprominec.presentation.ui.AppScreen
import dev.onelenyk.pprominec.presentation.ui.MapMarker
import dev.onelenyk.pprominec.presentation.ui.components.AppTextField
import dev.onelenyk.pprominec.presentation.ui.components.AppToolbar
import dev.onelenyk.pprominec.presentation.ui.screens.map.UsersMarkersDialog

@Composable
fun MainScreen(component: MainComponent) {
    val dialogSlot by component.dialog.subscribeAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    MviScreen(
        component = component,
        onEffect = { effect ->
            when (effect) {
                is MainEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }

                is MainEffect.CopyToClipboard -> {
                    clipboardManager.setText(AnnotatedString(effect.text))
                    Toast.makeText(context, "Скопійовано", Toast.LENGTH_SHORT).show()
                }
            }
        },
    ) { state, dispatch ->
        AppScreen(
            showInnerPadding = true,
            toolbar = { AppToolbar(title = "Калькулятор") },
            content = {
                InputAndResultScreen(
                    modifier = Modifier,
                    state = state,
                    dispatch = dispatch,
                )
                // Render dialog if present
                dialogSlot.child?.instance?.also {
                    when (it) {
                        is MainComponent.Dialog.UserMarkers -> {
                            UsersMarkersDialog(component = it.usersMarkersComponent)
                        }
                    }
                }
            },
        )
    }
}

// Helper functions for GeoCoordinate <-> String
private fun geoToLat(coordinate: GeoCoordinate?): String = coordinate?.lat?.toString() ?: ""
private fun geoToLon(coordinate: GeoCoordinate?): String = coordinate?.lon?.toString() ?: ""
private fun latLonToGeo(lat: String, lon: String): GeoCoordinate? =
    if (lat.isNotBlank() && lon.isNotBlank()) {
        lat.toDoubleOrNull()?.let { latVal ->
            lon.toDoubleOrNull()?.let { lonVal ->
                GeoCoordinate(latVal, lonVal)
            }
        }
    } else {
        null
    }

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InputAndResultScreen(
    modifier: Modifier = Modifier,
    state: MainState,
    dispatch: (MainIntent) -> Unit,
) {
    val latA = state.inputData.pointALat
    val lonA = state.inputData.pointALon
    val latB = state.inputData.pointBLat
    val lonB = state.inputData.pointBLon

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IntroCard()
        // In InputAndResultScreen, extract input sources from state.inputData
        val pointAInputSource = state.inputData.pointAInputSource
        val pointAMapMarker = state.inputData.pointAMapMarker
        val pointBInputSource = state.inputData.pointBInputSource
        val pointBMapMarker = state.inputData.pointBMapMarker
        PointACoordinatesCard(
            latA = latA,
            lonA = lonA,
            onLatAChange = { dispatch(MainIntent.OnPointALatChange(it)) },
            onLonAChange = { dispatch(MainIntent.OnPointALonChange(it)) },
            onLocationClick = { dispatch(MainIntent.OnLocationButtonClick(LocationButtonType.POINT_A)) },
            inputSource = pointAInputSource,
            pointAMapMarker = pointAMapMarker,

        )
        TargetCalculationCard(
            azimuth = state.inputData.azimuthFromA,
            distance = state.inputData.distanceKm,
            onAzimuthChange = { dispatch(MainIntent.OnAzimuthFromAChange(it)) },
            onDistanceChange = { dispatch(MainIntent.OnDistanceKmChange(it)) },
            isRenderMode = state.isRenderModeB,
            onModeChange = { dispatch(MainIntent.SetRenderModeB(it)) },
            result = state.outputData,
            onLocationClick = { dispatch(MainIntent.OnLocationButtonClick(LocationButtonType.TARGET)) },

        )
        ObservationPointCard(
            latB = latB,
            lonB = lonB,
            onLatBChange = { dispatch(MainIntent.OnPointBLatChange(it)) },
            onLonBChange = { dispatch(MainIntent.OnPointBLonChange(it)) },
            isRenderMode = state.isRenderModeC,
            onModeChange = { dispatch(MainIntent.SetRenderModeC(it)) },
            result = state.outputData,
            onLocationClick = { dispatch(MainIntent.OnLocationButtonClick(LocationButtonType.POINT_B)) },
            inputSource = pointBInputSource,
            pointBMapMarker = pointBMapMarker,
        )
    }
}

@Composable
fun IntroCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier,
    ) {
        Row(modifier = Modifier.padding(20.dp)) {
            Icon(
                imageVector = Icons.Default.Create,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp),
            )
            Column {
                Text(
                    text = "Введіть вихідні дані для розрахунку цілі",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Вкажіть координати спостерігача, напрямок (азимут) та відстань до цілі. Це дозволить точно розрахувати положення цілі на місцевості.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
fun SampleSelectorCard(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    selectedSampleText: String,
    onSampleSelected: (sample: Sample) -> Unit,
    samples: List<Sample>,
    hideSamples: Boolean,
    onHideSamples: () -> Unit,
) {
    AnimatedVisibility(visible = !hideSamples) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "Оберіть зразок розрахунку",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF4527A0),
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = onExpandedChange,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = selectedSampleText,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7E57C2),
                            unfocusedBorderColor = Color(0xFF7E57C2),
                        ),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { onExpandedChange(false) },
                    ) {
                        samples.forEach { sample ->
                            DropdownMenuItem(text = { Text(sample.name) }, onClick = {
                                onSampleSelected(sample)
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultCard(result: OutputData?) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Результат розрахунку",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            if (result != null) {
                ResultScreen(
                    result.targetPosition?.lat ?: 0.0,
                    result.targetPosition?.lon ?: 0.0,
                    result.azimuthFromB,
                )
            } else {
                Text(
                    "Будь ласка, введіть коректні значення у всі поля.",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
fun ResultScreen(
    latTarget: Double,
    lonTarget: Double,
    azimuthFromB: Double?,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val coordsText = "%.6f, %.6f".format(latTarget, lonTarget)
    val azimuthText = "%.2f°".format(azimuthFromB ?: 0.0)
    Column(modifier = modifier) {
        Text(
            text = "Координати цілі:",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF1B5E20), // dark green
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Column(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .background(Color(0xFFF1F8E9), RoundedCornerShape(12.dp))
                .clickable {
                    clipboardManager.setText(AnnotatedString(coordsText))
                    Toast.makeText(
                        context,
                        "Координати скопійовано",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                .padding(12.dp),
        ) {
            Text(
                text = "Широта: %.6f".format(latTarget),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF222222),
            )
            Text(
                text = "Довгота: %.6f".format(lonTarget),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF222222),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "(Натисніть, щоб скопіювати)",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF388E3C),
            )
        }
        Text(
            text = "Азимут на ціль:",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF01579B), // dark blue
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Column(
            modifier = Modifier
                .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp))
                .clickable {
                    clipboardManager.setText(AnnotatedString(azimuthText))
                    Toast.makeText(context, "Азимут скопійовано", Toast.LENGTH_SHORT).show()
                }
                .padding(12.dp),
        ) {
            Text(
                text = azimuthText,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF222222),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "(Натисніть, щоб скопіювати)",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF01579B),
            )
        }
    }
}

@Composable
fun PointACoordinatesCard(
    latA: String,
    lonA: String,
    onLatAChange: (String) -> Unit,
    onLonAChange: (String) -> Unit,
    onLocationClick: () -> Unit,
    inputSource: InputSource,
    pointAMapMarker: MapMarker? = null,
) {
    val bgColor =
        if (inputSource == InputSource.MARKER) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.surfaceVariant
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "1",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Координати точки A",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    ),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(
                modifier = Modifier.height(8.dp),
            )
            // Always show input mode for Card A
            Box(
                modifier = Modifier
                    .background(
                        color = bgColor,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(vertical = 6.dp)
                    .padding(horizontal = 6.dp)
                    .fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // Coordinate fields
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        AppTextField(
                            modifier = Modifier.weight(1f),
                            value = latA,
                            onValueChange = onLatAChange,
                            maxLines = 1,
                            isRequired = false,
                            label = "Широта A:",
                            placeholder = "42.222",
                            leftContent = { Text("\uD83D\uDD2D") }, // 🧭
                            keyboardType = KeyboardType.Number,
                        )
                        AppTextField(
                            modifier = Modifier.weight(1f),
                            value = lonA,
                            onValueChange = onLonAChange,
                            maxLines = 1,
                            isRequired = false,
                            label = "Довгота А:",
                            placeholder = "42.222",
                            leftContent = { Text("\uD83D\uDDFA\uFE0F") }, // 🗺️
                            keyboardType = KeyboardType.Number,
                        )
                    }
                    Spacer(
                        modifier = Modifier.width(8.dp),
                    )
                    LocationButton(
                        onClick = onLocationClick,
                        mapMarker = pointAMapMarker,
                        modifier = Modifier,
                    )
                }
            }
        }
    }
}

@Composable
fun TargetCalculationCard(
    azimuth: String,
    distance: String,
    onAzimuthChange: (String) -> Unit,
    onDistanceChange: (String) -> Unit,
    isRenderMode: Boolean,
    onModeChange: (Boolean) -> Unit,
    result: OutputData?,
    onLocationClick: () -> Unit,
) {
    val arrowRotation by animateFloatAsState(if (isRenderMode) 180f else 0f)
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "2",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Азимут та відстань до цілі",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    ),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                AppTextField(
                    modifier = Modifier.weight(1f),
                    value = azimuth,
                    onValueChange = onAzimuthChange,
                    maxLines = 1,
                    isRequired = true,
                    label = "Азимут на ціль:",
                    placeholder = "80",
                    leftContent = { Text("\u2197\uFE0F") }, // ↗️
                    keyboardType = KeyboardType.Number,
                )
                AppTextField(
                    modifier = Modifier.weight(1f),
                    value = distance,
                    onValueChange = onDistanceChange,
                    maxLines = 1,
                    isRequired = false,
                    label = "Відстань до цілі:",
                    placeholder = "50",
                    leftContent = { Text("\uD83D\uDCCF") }, // 📏
                    keyboardType = KeyboardType.Number,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(onClick = { onModeChange(!isRenderMode) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = if (isRenderMode) "Згорнути" else "Розгорнути",
                        modifier = Modifier.rotate(arrowRotation),
                    )
                }
            }
            AnimatedVisibility(
                visible = isRenderMode,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (result?.targetPosition != null) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    AppTextField(
                                        modifier = Modifier.weight(1f),
                                        value = result.targetPosition.lat.toString(),
                                        onValueChange = { },
                                        maxLines = 3,
                                        isRequired = false,
                                        label = "Широта Цілі:",
                                        placeholder = "42.222",
                                        leftContent = { Text("\uD83D\uDD2D") }, // 🧭
                                        keyboardType = KeyboardType.Number,
                                    )
                                    AppTextField(
                                        modifier = Modifier.weight(1f),
                                        value = result.targetPosition.lon.toString(),
                                        onValueChange = { },
                                        maxLines = 3,
                                        isRequired = false,
                                        label = "Довгота Цілі:",
                                        placeholder = "42.222",
                                        leftContent = { Text("\uD83D\uDDFA\uFE0F") }, // 🗺️
                                        keyboardType = KeyboardType.Number,
                                    )
                                }
                                Spacer(
                                    modifier = Modifier.width(8.dp),
                                )
                                LocationButton(
                                    onClick = onLocationClick,
                                    modifier = Modifier,
                                )
                            }
                        }
                    } else {
                        Text(
                            "Помилка обчислення координат",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ObservationPointCard(
    latB: String,
    lonB: String,
    onLatBChange: (String) -> Unit,
    onLonBChange: (String) -> Unit,
    isRenderMode: Boolean,
    onModeChange: (Boolean) -> Unit,
    result: OutputData?,
    onLocationClick: () -> Unit,
    inputSource: InputSource,
    pointBMapMarker: MapMarker? = null,
) {
    val bgColor =
        if (inputSource == InputSource.MARKER) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondaryContainer
    val arrowRotation by animateFloatAsState(if (isRenderMode) 180f else 0f)
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "3",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Точка B (інша позиція)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    ),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .background(
                            color = bgColor,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .padding(vertical = 6.dp)
                        .padding(horizontal = 6.dp)
                        .fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            if (inputSource == InputSource.MARKER && pointBMapMarker != null) {
                                AppTextField(
                                    modifier = Modifier.weight(1f),
                                    value = pointBMapMarker.latitude.toString(),
                                    onValueChange = {},
                                    maxLines = 1,
                                    isRequired = false,
                                    label = "Широта B (маркер):",
                                    placeholder = "42.222",
                                    leftContent = { Text("\uD83D\uDD2D") },
                                    readOnly = true,
                                    keyboardType = KeyboardType.Number,
                                )
                                AppTextField(
                                    modifier = Modifier.weight(1f),
                                    value = pointBMapMarker.longitude.toString(),
                                    onValueChange = {},
                                    maxLines = 1,
                                    isRequired = false,
                                    label = "Довгота B (маркер):",
                                    placeholder = "42.222",
                                    leftContent = { Text("\uD83D\uDDFA\uFE0F") },
                                    readOnly = true,
                                    keyboardType = KeyboardType.Number,
                                )
                            } else {
                                AppTextField(
                                    modifier = Modifier.weight(1f),
                                    value = latB,
                                    onValueChange = onLatBChange,
                                    maxLines = 1,
                                    isRequired = false,
                                    label = "Широта B:",
                                    placeholder = "42.222",
                                    leftContent = { Text("\uD83D\uDD2D") },
                                    keyboardType = KeyboardType.Number,
                                )
                                AppTextField(
                                    modifier = Modifier.weight(1f),
                                    value = lonB,
                                    onValueChange = onLonBChange,
                                    maxLines = 1,
                                    isRequired = false,
                                    label = "Довгота B:",
                                    placeholder = "42.222",
                                    leftContent = { Text("\uD83D\uDDFA\uFE0F") },
                                    keyboardType = KeyboardType.Number,
                                )
                            }
                        }
                        Spacer(
                            modifier = Modifier.width(8.dp),
                        )
                        LocationButton(
                            onClick = onLocationClick,
                            mapMarker = if (inputSource == InputSource.MARKER) pointBMapMarker else null,
                            modifier = Modifier,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(onClick = { onModeChange(!isRenderMode) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = if (isRenderMode) "Згорнути" else "Розгорнути",
                        modifier = Modifier.rotate(arrowRotation),
                    )
                }
            }
            AnimatedVisibility(
                visible = isRenderMode,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (result?.azimuthFromB != null && result.distanceFromB != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            AppTextField(
                                modifier = Modifier.weight(1f),
                                value = "%.2f".format(result.azimuthFromB),
                                onValueChange = {},
                                maxLines = 1,
                                isRequired = false,
                                label = "Азимут з B на ціль:",
                                placeholder = "80",
                                leftContent = { Text("\u2197\uFE0F") }, // ↗️
                                readOnly = true,
                                keyboardType = KeyboardType.Number,
                            )
                            AppTextField(
                                modifier = Modifier.weight(1f),
                                value = "%.2f".format(result.distanceFromB),
                                onValueChange = {},
                                maxLines = 1,
                                isRequired = false,
                                label = "Відстань до цілі:",
                                placeholder = "50",
                                leftContent = { Text("\uD83D\uDCCF") }, // 📏
                                readOnly = true,
                                keyboardType = KeyboardType.Number,
                            )
                        }
                    } else {
                        Text(
                            "Введіть координати точки B для обчислення",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LocationButton(
    modifier: Modifier = Modifier,
    mapMarker: MapMarker? = null,
    borderColor: Color = MaterialTheme.colorScheme.onPrimary,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    iconColor: Color = MaterialTheme.colorScheme.onPrimary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape,
            )
            .background(backgroundColor, shape)
            .clip(shape)
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (mapMarker != null) {
            Text(
                text = mapMarker.code.toString(),
                modifier = Modifier.size(24.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                textAlign = TextAlign.Center,
            )
        } else {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "Відкрити карту з локацією",
                tint = iconColor,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
