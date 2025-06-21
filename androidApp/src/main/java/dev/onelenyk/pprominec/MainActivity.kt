package dev.onelenyk.pprominec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.onelenyk.pprominec.presentation.components.DefaultRootComponent
import dev.onelenyk.pprominec.presentation.`interface`.RootContainer
import com.arkivanov.decompose.defaultComponentContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                RootContainer(
                    component = DefaultRootComponent(defaultComponentContext())
                )
            }
        }
    }
}
