package dev.onelenyk.pprominec.presentation.components.bottom_nav

import MapFilesRepository
import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import dev.onelenyk.pprominec.presentation.components.main.DefaultMainComponent
import dev.onelenyk.pprominec.presentation.components.main.DefaultMapComponent
import dev.onelenyk.pprominec.presentation.components.main.MapComponent
import dev.onelenyk.pprominec.presentation.components.main.MainComponent
import dev.onelenyk.pprominec.presentation.components.main.MapFileStorage
import dev.onelenyk.pprominec.presentation.components.settings.DefaultSettingsComponent
import dev.onelenyk.pprominec.presentation.components.settings.SettingsComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.serialization.Serializable
import org.koin.java.KoinJavaComponent.getKoin

interface BottomNavComponent {
    val stack: Value<ChildStack<*, Child>>

    fun onMainTabClicked()
    fun onSettingsTabClicked()
    fun onMapTabClicked()
    fun onPermissionsClicked()

    sealed class Child {
        data class Main(val component: MainComponent) : Child()
        data class Map(val component: MapComponent) : Child()
        data class Settings(val component: SettingsComponent) : Child()
    }
}

class DefaultBottomNavComponent(
    componentContext: ComponentContext,
    private val onPermissionsClicked: () -> Unit
) : BottomNavComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, BottomNavComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Main,
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun child(config: Config, componentContext: ComponentContext): BottomNavComponent.Child =
        when (config) {
            is Config.Main -> BottomNavComponent.Child.Main(DefaultMainComponent(componentContext))
            is Config.Settings -> BottomNavComponent.Child.Settings(DefaultSettingsComponent(componentContext, onPermissionsClicked))
            is Config.Map -> {
                val appContext = getKoin().get<Context>()
                val repository = getKoin().get<MapFilesRepository>()
                val mapFileStorage = getKoin().get<MapFileStorage>()
                val scope = getKoin().get<CoroutineScope>()

                BottomNavComponent.Child.Map(
                    DefaultMapComponent(
                        componentContext = componentContext,
                        appContext = appContext,
                        repository = repository,
                        mapFileStorage = mapFileStorage,
                        coroutineScope = scope
                    )
                )
            }
        }

    override fun onMainTabClicked() {
        navigation.bringToFront(Config.Main)
    }

    override fun onSettingsTabClicked() {
        navigation.bringToFront(Config.Settings)
    }

    override fun onMapTabClicked() {
        navigation.bringToFront(Config.Map)
    }

    override fun onPermissionsClicked() {
        onPermissionsClicked()
    }

    @Serializable
    private sealed class Config {
        @Serializable
        data object Main : Config()

        @Serializable
        data object Settings : Config()

        @Serializable
        data object Map : Config()
    }
} 