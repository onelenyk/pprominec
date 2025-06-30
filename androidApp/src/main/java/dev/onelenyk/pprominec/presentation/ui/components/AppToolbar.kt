package dev.onelenyk.pprominec.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.onelenyk.pprominec.MyApplicationTheme
import dev.onelenyk.pprominec.presentation.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppToolbar(
    title: String,
    showBack: Boolean = false,
    onBackClick: (() -> Unit) = {},
) {
    Column {
        TopAppBar(
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (showBack) {
                        IconButton(modifier = Modifier.size(48.dp), onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    Text(
                        modifier = Modifier.weight(1f),
                        text = title,
                        textAlign = TextAlign.Center,
                        style = Typography.H1,
                    )

                    // Spacer to balance the back button
                    if (showBack) {
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }
            },
            colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
            expandedHeight = 44.dp,
        )
        HorizontalDivider()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppToolbarPreview() {
    MyApplicationTheme {
        Column(
            modifier = Modifier.background(Color.White),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppToolbar(title = "Toolbar Title")
            AppToolbar(title = "With Back Button", showBack = true, onBackClick = {})
        }
    }
}
