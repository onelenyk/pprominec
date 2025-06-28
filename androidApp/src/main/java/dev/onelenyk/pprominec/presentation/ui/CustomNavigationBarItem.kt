package dev.onelenyk.pprominec.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.onelenyk.pprominec.presentation.ui.theme.Typography

@Composable
fun CustomNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String? = null,
    modifier: Modifier = Modifier,
    selectedContentColor: Color = MaterialTheme.colorScheme.primary,
    unselectedContentColor: Color = MaterialTheme.colorScheme.primary,
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) selectedContentColor else unselectedContentColor,
        label = "contentColor",
    )

    Column(
        modifier =
        modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // No ripple effect for a minimalistic feel
                onClick = onClick,
            )
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Icon(imageVector = icon, contentDescription = label)
            if (label != null) {
                val style = if (selected) Typography.T2 else Typography.T3
                Text(text = label, style = style)
            }
        }
    }
}
