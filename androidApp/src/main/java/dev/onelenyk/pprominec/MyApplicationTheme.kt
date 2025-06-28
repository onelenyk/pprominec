package dev.onelenyk.pprominec

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import darkScheme
import lightScheme

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> darkScheme
            else -> lightScheme
        }

    val typography =
        Typography(
            bodyMedium =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
            ),
        )
    val shapes =
        Shapes(
            small = RoundedCornerShape(4.dp),
            medium = RoundedCornerShape(4.dp),
            large = RoundedCornerShape(0.dp),
        )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content,
    )
}

@Composable
fun PreviewThemeColors() {
    val colorScheme = MaterialTheme.colorScheme
    val colors =
        listOf(
            colorScheme.primary,
            colorScheme.onPrimary,
            colorScheme.primaryContainer,
            colorScheme.onPrimaryContainer,
            colorScheme.secondary,
            colorScheme.onSecondary,
            colorScheme.secondaryContainer,
            colorScheme.onSecondaryContainer,
            colorScheme.tertiary,
            colorScheme.onTertiary,
            colorScheme.tertiaryContainer,
            colorScheme.onTertiaryContainer,
            colorScheme.background,
            colorScheme.onBackground,
            colorScheme.surface,
            colorScheme.onSurface,
            colorScheme.surfaceVariant,
            colorScheme.onSurfaceVariant,
            colorScheme.error,
            colorScheme.onError,
            colorScheme.errorContainer,
            colorScheme.onErrorContainer,
        )

    Column(
        modifier = Modifier.background(Color.White),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        colors.forEach { color ->
            Box(
                modifier =
                Modifier
                    .size(16.dp)
                    .background(color),
            )
        }
    }
}

@Preview(showBackground = false, showSystemUi = false)
@Composable
fun PreviewThemeColorsPreview() {
    MyApplicationTheme {
        PreviewThemeColors()
    }
}
