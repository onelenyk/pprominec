package dev.onelenyk.pprominec.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun AppScreen(
    toolbar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    showInnerPadding: Boolean = true,
    content: @Composable () -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            if (toolbar != null) {
                Box(
                    Modifier
                        .statusBarsPadding(),
                ) {
                    toolbar()
                }
            }
        },
        bottomBar = {
            if (bottomBar != null) {
                Box(
                    Modifier,
                ) {
                    bottomBar()
                }
            }
        },
    ) { innerPadding ->
        var modifier = if (showInnerPadding) {
            Modifier.padding(innerPadding)
        } else {
            Modifier
        }

        if (toolbar == null) {
            modifier = modifier.statusBarsPadding()
        }

        if (bottomBar == null) {
            modifier = modifier.navigationBarsPadding()
        }

        Box(modifier = modifier) {
            content()
        }
    }
}

@Composable
fun AppDialog(
    content: @Composable () -> Unit,
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(
        usePlatformDefaultWidth = false,
    ),
) {
    Dialog(
        content = content,
        onDismissRequest = onDismissRequest,
        properties = properties,
    )
}
