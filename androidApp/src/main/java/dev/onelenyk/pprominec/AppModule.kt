package dev.onelenyk.pprominec

import MapFilesRepository
import dev.onelenyk.pprominec.presentation.components.main.MapFileStorage
import dev.onelenyk.pprominec.presentation.components.permissions.PermissionsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule =
    module {
        single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
        single { MapFilesRepository(androidContext()) }
        single { MapFileStorage(androidContext()) }
        single { PermissionsManager() }
    }
