package dev.onelenyk.pprominec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.onelenyk.pprominec.presentation.ui.RootContainer
import com.arkivanov.decompose.defaultComponentContext
import dev.onelenyk.pprominec.presentation.components.root.DefaultRootComponent

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
