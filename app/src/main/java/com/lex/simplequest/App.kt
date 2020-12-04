package com.lex.simplequest

import android.app.Application
import android.provider.Settings
import com.lex.core.log.LogFactory
import com.lex.core.log.LogFactoryImpl
import com.lex.simplequest.data.location.repository.LocationRepositoryImpl
import com.lex.simplequest.data.settings.repository.SettingsRepositoryImpl
import com.lex.simplequest.device.connectivity.InternetConnectivityTrackerImpl
import com.lex.simplequest.device.locationmanager.LocationManagerImpl
import com.lex.simplequest.device.permission.repository.PermissionCheckerImpl
import com.lex.simplequest.device.service.TrackLocationService
import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import com.lex.simplequest.domain.locationmanager.LocationManager
import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.permission.repository.PermissionChecker
import com.lex.simplequest.domain.repository.LocationRepository
import com.lex.simplequest.domain.repository.SettingsRepository
import com.lex.simplequest.presentation.utils.tasks.RxCache

import io.reactivex.plugins.RxJavaPlugins

class App : Application() {
    companion object {
        lateinit var instance: App
            private set
    }

    val logFactory: LogFactory = LogFactoryImpl()
    val rxCache = RxCache()
    val internetConnectivityTracker: InternetConnectivityTracker by lazy {
        InternetConnectivityTrackerImpl(this)
    }
    val permissionChecker: PermissionChecker by lazy { PermissionCheckerImpl(this) }
    val locationRepository: LocationRepository by lazy { LocationRepositoryImpl(this) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepositoryImpl(this) }

    val locationManager: LocationManager by lazy {
        LocationManagerImpl(
            this,
            permissionChecker
        )
    }

    override fun onCreate() {
        instance = this
        super.onCreate()

        // TODO: Setup Strict mode

        RxJavaPlugins.setErrorHandler { throwable ->
            android.util.Log.e("app", "RxJava undelivered exception", throwable)
        }
    }
}