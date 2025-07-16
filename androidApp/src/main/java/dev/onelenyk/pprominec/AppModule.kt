package dev.onelenyk.pprominec

import dev.onelenyk.pprominec.data.DataStoreManager
import dev.onelenyk.pprominec.data.MapSettingsRepository
import dev.onelenyk.pprominec.presentation.components.main.FileManager
import dev.onelenyk.pprominec.presentation.components.main.FileStorage
import dev.onelenyk.pprominec.presentation.components.main.UsersMarkersRepository
import dev.onelenyk.pprominec.presentation.components.permissions.PermissionsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule =
    module {
        single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
        single { FileStorage(androidContext(), get()) }
        single { FileManager(androidContext(), get(), get()) }
        single { PermissionsManager() }
        single { DataStoreManager(androidContext()) }
        single { MapSettingsRepository(get<DataStoreManager>().mapSettingsDataStore) }
        single { UsersMarkersRepository() }
    }
