package dev.onelenyk.pprominec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arkivanov.decompose.defaultComponentContext
import dev.onelenyk.pprominec.presentation.components.root.DefaultRootComponent
import dev.onelenyk.pprominec.presentation.ui.RootContainer
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize osmdroid configuration
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName

        setContent {
            MyApplicationTheme {
                RootContainer(
                    component = DefaultRootComponent(defaultComponentContext()),
                )
            }
        }
    }
}
