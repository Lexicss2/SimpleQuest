package com.lex.simplequest

import android.app.Application
import android.content.Intent
import android.util.Log
import com.lex.core.log.LogFactory
import com.lex.core.log.LogFactoryImpl
import com.lex.simplequest.data.location.repository.LocationRepositoryImpl
import com.lex.simplequest.device.connectivity.InternetConnectivityTrackerImpl
import com.lex.simplequest.device.permission.repository.PermissionCheckerImpl
import com.lex.simplequest.device.service.TrackLocationService
import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import com.lex.simplequest.domain.permission.repository.PermissionChecker
import com.lex.simplequest.domain.repository.LocationRepository
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
    val trackLocationService: TrackLocationService = TrackLocationService()

    override fun onCreate() {
        instance = this
        super.onCreate()

        // TODO: Setup Strict mode

//        val intent = Intent(applicationContext, TrackLocationService::class.java)
//        val res = applicationContext.startService(intent)
//        Log.d("qaz", "result: $res")

        RxJavaPlugins.setErrorHandler { throwable ->
            android.util.Log.e("app", "RxJava undelivered exception", throwable)
        }
    }
}