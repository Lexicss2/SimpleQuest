package com.lex.simplequest.device.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.BatteryManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.lex.core.utils.ObservableValue
import com.lex.simplequest.App
import com.lex.simplequest.R
import com.lex.simplequest.domain.locationmanager.LocationManager
import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.locationmanager.model.Location
import com.lex.simplequest.domain.model.Point
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.repository.LocationRepository
import com.lex.simplequest.domain.repository.SettingsRepository
import com.lex.simplequest.domain.settings.interactor.ReadSettingsInteractor
import com.lex.simplequest.domain.settings.interactor.ReadSettingsInteractorImpl
import com.lex.simplequest.domain.track.interactor.*
import com.lex.simplequest.presentation.screen.home.MainActivity
import com.lex.simplequest.presentation.utils.asRxObservable
import com.lex.simplequest.presentation.utils.asRxSingle
import com.lex.simplequest.presentation.utils.tasks.MultiResultTask
import com.lex.simplequest.presentation.utils.tasks.SingleResultTask
import io.reactivex.android.schedulers.AndroidSchedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class TrackLocationService() : Service(), LocationTracker {

    companion object {
        private const val TAG = "TrackLocationService"
        private const val TASK_START_TRACK = "startTrack"
        private const val TASK_STOP_TRACK = "stopTrack"
        private const val TASK_ADD_POINT = "addPoint"
        private const val TASK_READ_SETTINGS = "readSettings"

        private const val ONGOING_NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL_ID = "trackLocationNotificationId"
    }

    private lateinit var locationManager: LocationManager
    private lateinit var locationRepository: LocationRepository
    private lateinit var settingsRepository: SettingsRepository
    private val binder = TrackLocationBinder()

    private var status: LocationTracker.Status = LocationTracker.Status.NONE
    private var activeTrackId: Long? = null
    private var configDistance: Long? = null
    private var configBatteryLevel: Int? = null
    private val taskStartTrack = createStartTrackTask()
    private val taskStopTrack = createStopTrackTask()
    private val taskAddPoint = createAddPointTask()
    private val taskReadSettings = createReadSettingsTask()

    private val pointObservableValue = ObservableValue(Point(-1, -1, 0.0, 0.0, null, 0L))

    private val log = App.instance.logFactory.get(TAG)

    private val _startTrackIneractor: StartTrackInteractor by lazy {
        StartTrackInteractorImpl(App.instance.locationRepository)
    }
    private val _stopTrackInteractor: StopTrackInteractor by lazy {
        StopTrackInteractorImpl(App.instance.locationRepository)
    }
    private val _addPointInteractor: AddPointInteractor by lazy {
        AddPointInteractorImpl(App.instance.locationRepository)
    }
    private val _readSettingsInteractor: ReadSettingsInteractor by lazy {
        ReadSettingsInteractorImpl(App.instance.settingsRepository)
    }

    override val startTrackInteractor: StartTrackInteractor
        get() = _startTrackIneractor
    override val stopTrackInteractor: StopTrackInteractor
        get() = _stopTrackInteractor
    override val addPointInteractor: AddPointInteractor
        get() = _addPointInteractor
    override val readSettingsInteractor: ReadSettingsInteractor
        get() = _readSettingsInteractor

    private val locationListeners = CopyOnWriteArrayList<LocationTracker.Listener>()
    private var locationManagerCallback = object : LocationManager.Callback {

        override fun onConnected() {
            Log.i(TAG, "Manager onConnected")
            locationListeners.forEach {
                it.onLocationManagerConnected()
            }
            if (LocationTracker.Status.RECORDING != status || LocationTracker.Status.CONNECTED != status) {
                changeStatus(LocationTracker.Status.CONNECTED)
            }
        }

        override fun onConnectionSuspended(reason: Int) {
            Log.w(TAG, "Manager onSuspended")
            locationListeners.forEach {
                it.onLocationMangerConnectionSuspended(reason)
            }
            changeStatus(LocationTracker.Status.IDLE)
        }

        override fun onConnectionFailed(error: Throwable) {
            Log.e(TAG, "Manager onFailed")
            locationListeners.forEach {
                it.onLocationMangerConnectionFailed(error)
            }
            changeStatus(LocationTracker.Status.IDLE)
        }

        override fun onLocationChanged(location: Location) {
            Log.v(TAG, "Manager onChanged")
            locationListeners.forEach {
                it.onLocationUpdated(location)
            }

            if (null != activeTrackId && LocationTracker.Status.RECORDING != status) {
                changeStatus(LocationTracker.Status.RECORDING)
            }

            activeTrackId?.let { trackId ->
                Log.v(TAG, "location: ${location.latitude}: ${location.longitude}")
                val point =
                    Point(
                        -1,
                        trackId,
                        location.latitude,
                        location.longitude,
                        location.altitude,
                        System.currentTimeMillis()
                    )
                pointObservableValue.set(point)

                if (taskAddPoint.isRunning()) {
                    pointObservableValue.set(point)
                } else {
                    taskAddPoint.start(AddPointInteractor.Param(pointObservableValue), Unit)
                }
            }
        }

        override fun onLocationAvailable(available: Boolean) {
            Log.w(TAG, "onLocationAvailable called, available: $available")
            locationListeners.forEach {
                it.onLocationAvailable(available)
            }
        }
    }

    private val batteryInfoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            Log.v(TAG, "battery level changed to $level")

            if (LocationTracker.Status.RECORDING == status ) {
                configBatteryLevel?.let { configgedLevel ->
                    if (level < configgedLevel) {
                        Log.w(TAG, "Recording is stopped because of low battery level")
                        stopRecording()
                    }
                }
            }
        }
    }

    override fun onCreate() {
        Log.i(TAG, "LT onCreate --------")
        super.onCreate()
        changeStatus(LocationTracker.Status.IDLE)
        registerReceiver(batteryInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(
            TAG,
            "<<Service LT started, startId = $startId>>, intent = $intent, activeTrackId = $activeTrackId"
        )
        locationManager = App.instance.locationManager
        locationRepository = App.instance.locationRepository
        settingsRepository = App.instance.settingsRepository

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.i(TAG, "onBind")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.e(TAG, "LT onDestroy ---------")
        super.onDestroy()
        unregisterReceiver(batteryInfoReceiver)
        changeStatus(LocationTracker.Status.NONE)
        locationListeners.clear()
        taskStartTrack.stop()
        taskStopTrack.stop()
        taskAddPoint.stop()
        stopRecording()
    }

    override fun connect(): Boolean =
        if (LocationTracker.Status.IDLE == status) {
            if (taskReadSettings.isRunning()) {
                taskReadSettings.stop()
            }
            changeStatus(LocationTracker.Status.RETRIEVING_CONFIG)
            taskReadSettings.start(ReadSettingsInteractor.Param(), false)
        } else false

    override fun disconnect() =
        if (LocationTracker.Status.CONNECTED == status) {  // Disconnect only for CONNECTED state, otherwise ignore, e.g. for RECORDING state
            locationManager.disconnect()

            activeTrackId?.let { trackId ->
                taskStopTrack.start(
                    StopTrackInteractor.Param(trackId, System.currentTimeMillis(), configDistance),
                    Unit
                )
            }
            changeStatus(LocationTracker.Status.IDLE)
            true
        } else false


    override fun isConnecting(): Boolean =
        LocationTracker.Status.CONNECTING == status

    override fun isConnected(): Boolean =
        LocationTracker.Status.CONNECTED == status || LocationTracker.Status.RECORDING == status

    override fun startRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pendingIntent: PendingIntent =
                Intent(this, MainActivity::class.java).let { notificationIntent ->
                    PendingIntent.getActivity(this, 0, notificationIntent, 0)
                }

            val channelId =
                createNotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    resources.getString(R.string.service_notification_channel_name)
                )

            val notification: Notification = Notification.Builder(this, channelId)
                .setContentTitle(resources.getString(R.string.app_name))
                .setContentText(resources.getString(R.string.home_is_recording))
                .setSmallIcon(R.drawable.ic_directions_walk_24px)
                .setContentIntent(pendingIntent)
                .build()

            startForeground(ONGOING_NOTIFICATION_ID, notification)
        }

        if (status == LocationTracker.Status.IDLE) {
            if (taskReadSettings.isRunning()) {
                taskReadSettings.stop()
            }
            changeStatus(LocationTracker.Status.RETRIEVING_CONFIG)
            taskReadSettings.start(ReadSettingsInteractor.Param(), true)
        }
    }

    override fun stopRecording() {
        Log.d(TAG, "StopRecording")
        activeTrackId?.let { trackId ->
            Log.d(TAG, "!!! activeTrackId is not null")
            locationManager.disconnect()

            taskStopTrack.start(
                StopTrackInteractor.Param(trackId, System.currentTimeMillis(), configDistance),
                Unit
            )
        }
        activeTrackId = null

        changeStatus(LocationTracker.Status.IDLE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i(TAG, "stopForeground")
            stopForeground(true)
        }
    }

    override fun isRecording(): Boolean =
        LocationTracker.Status.RECORDING == status

    override fun getLastTrack(): Track? {
        return null
    }

    override fun addListener(listener: LocationTracker.Listener) {
        if (!locationListeners.contains(listener)) {
            locationListeners.add(listener)
        }
    }

    override fun removeListener(listener: LocationTracker.Listener) {
        if (locationListeners.contains(listener)) {
            locationListeners.remove(listener)
        }
    }

    override fun getStatus(): LocationTracker.Status =
        status

    inner class TrackLocationBinder : Binder() {
        fun getService(): TrackLocationService = this@TrackLocationService
    }

    private fun generateName(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS", Locale.US)
        return sdf.format(Date())
    }

    private fun changeStatus(newStatus: LocationTracker.Status) {
        val message = "<<< Changing status from $status to $newStatus >>>"
        when (newStatus) {
            LocationTracker.Status.NONE -> Log.v(TAG, message)
            LocationTracker.Status.IDLE -> Log.v(TAG, message)
            LocationTracker.Status.RETRIEVING_CONFIG -> Log.d(TAG, message)
            LocationTracker.Status.CONNECTING -> Log.w(TAG, message)
            LocationTracker.Status.CONNECTED -> Log.i(TAG, message)
            LocationTracker.Status.RECORDING -> Log.e(TAG, message)
        }
        status = newStatus
        locationListeners.forEach {
            it.onStatusUpdated(newStatus)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val channel = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
        return channelId
    }

    private fun handleStartTrack(trackId: Long?, error: Throwable?) {
        if (null != trackId) {
            Log.i(TAG, "track inserted successfully, id = $trackId")
            activeTrackId = trackId
        } else if (null != error) {
            Log.e(TAG, "error inserting track: ${error.localizedMessage}")
            if (LocationTracker.Status.RECORDING == status) {
                changeStatus(LocationTracker.Status.CONNECTED)
            }
        }
    }

    private fun createStartTrackTask() =
        SingleResultTask<StartTrackInteractor.Param, StartTrackInteractor.Result, Unit>(
            TASK_START_TRACK,
            { param, _ ->
                startTrackInteractor.asRxSingle(param)
                    .observeOn(AndroidSchedulers.mainThread())
            },
            { result, _ ->
                handleStartTrack(result.trackId, null)
            },
            { error, _ ->
                log.e(error, "Failed to start track")
                handleStartTrack(null, error)
            }
        )

    private fun handleStopTrack(succeeded: Boolean?, error: Throwable?) {
        if (null != succeeded) {
            if (!succeeded) {
                Toast.makeText(
                    this@TrackLocationService,
                    resources.getString(R.string.service_track_was_not_saved),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else if (null != error) {
            Log.e(TAG, "Failed to stopTrack")

        } else {
            Log.i(TAG, "Stop track succeedded: $succeeded")
        }
        activeTrackId = null
    }

    private fun createStopTrackTask() =
        SingleResultTask<StopTrackInteractor.Param, StopTrackInteractor.Result, Unit>(
            TASK_STOP_TRACK,
            { param, _ ->
                stopTrackInteractor.asRxSingle(param)
                    .observeOn(AndroidSchedulers.mainThread())
            },
            { result, _ ->
                handleStopTrack(result.succeeded, null)
            },
            { error, _ ->
                log.e(error, "Failed to stop track")
                handleStopTrack(null, error)
            }
        )

    private fun handleAddPointTask(error: Throwable?) {
        if (null != error) {
            Log.e(TAG, "Failed to add point: ${error.localizedMessage}")
            // TODO: Handle somehow, needs to restart task
        } else {
            Log.d(TAG, "Point add succeeded")
        }
    }

    private fun createAddPointTask() =
        MultiResultTask<AddPointInteractor.Param, Unit, Unit>(
            TASK_ADD_POINT,
            { param, _ ->
                addPointInteractor.asRxObservable(param)
                    .observeOn(AndroidSchedulers.mainThread())
            },
            { _, _ ->
                handleAddPointTask(null)
            },
            { error, _ ->
                handleAddPointTask(error)
            }
        )

    private fun handleReadSettings(
        result: ReadSettingsInteractor.Result?,
        error: Throwable?,
        recRequest: Boolean
    ) {
        val config = if (null != result) {
            LocationManager.ConnectionConfig(result.timePeriod, result.displacement)
        } else null

        if (null != result) {
            configDistance = result.distance
            configBatteryLevel = result.batteryLevel
        }

        Log.d(TAG, "config = $config")
        locationManager.connect(config, locationManagerCallback)
        changeStatus(LocationTracker.Status.CONNECTING)

        if (recRequest) {
            taskStartTrack.start(
                StartTrackInteractor.Param(
                    generateName(),
                    System.currentTimeMillis()
                ), Unit
            )
        }

    }

    private fun createReadSettingsTask() =
        SingleResultTask<ReadSettingsInteractor.Param, ReadSettingsInteractor.Result, Boolean>(
            TASK_READ_SETTINGS,
            { param, _ ->
                readSettingsInteractor.asRxSingle(param)
                    .observeOn(AndroidSchedulers.mainThread())
            },
            { result, rec ->
                handleReadSettings(result, null, rec)

            },
            { error, rec ->
                log.e(error, "Failed to read settings")
                handleReadSettings(null, error, rec)
            }
        )
}