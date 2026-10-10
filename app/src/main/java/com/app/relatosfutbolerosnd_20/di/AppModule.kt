package com.app.relatosfutbolerosnd_20.di

import com.app.relatosfutbolerosnd_20.data.repository.LicenseRepository
import com.app.relatosfutbolerosnd_20.data.repository.SettingsRepository
import com.app.relatosfutbolerosnd_20.ui.screens.LicenseViewModel
import com.app.relatosfutbolerosnd_20.ui.screens.LiveStreamViewModel
import com.app.relatosfutbolerosnd_20.ui.screens.MatchSetupViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { LicenseRepository(androidContext()) }
    single { SettingsRepository(androidContext()) }
    
    viewModel { LicenseViewModel(get()) }
    viewModel { MatchSetupViewModel(androidApplication(), get()) }
    viewModel { LiveStreamViewModel(androidApplication()) }
}