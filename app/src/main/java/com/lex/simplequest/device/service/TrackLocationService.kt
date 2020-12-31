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
import com.lex.simplequest.domain.model.CheckPoint
import com.lex.simplequest.domain.model.Point
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
import java.lang.IllegalStateException
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
        private const val TASK_ADD_CHECK_POINT = "addCheckPoint"

        private const val ONGOING_NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL_ID = "trackLocationNotificationId"
    }

    private lateinit var locationManager: LocationManager
    private lateinit var locationRepository: LocationRepository
    private lateinit var settingsRepository: SettingsRepository
    private val binder = TrackLocationBinder()

    private var status: LocationTracker.Status = LocationTracker.Status.NONE
    private var activeTrackId: Long? = null
    private var trackerConfig: LocationTracker.TrackerConfig? = null
    private var connectionConfig: LocationManager.ConnectionConfig? = null
    private var isLocationAvailable: Boolean = false

    private val taskStartTrack = createStartTrackTask()
    private val taskStopTrack = createStopTrackTask()
    private val taskAddPoint = createAddPointTask()
    private val taskReadSettings = createReadSettingsTask()
    private val taskAddCheckPoint = createAddCheckPointTask()

    private val pointObservableValue = ObservableValue(Point(-1, -1, 0.0, 0.0, null, 0L))

    private val log = App.instance.logFactory.get(TAG)

    private var _recordingEventsListener: LocationTracker.RecordingEventsListener? = null
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
    private val _addCheckPointInteractor: AddCheckPointInteractor by lazy {
        AddCheckPointInteractorImpl(App.instance.locationRepository)
    }

    override var recordingEventsListener: LocationTracker.RecordingEventsListener?
        get() = _recordingEventsListener
        set(value) {
            _recordingEventsListener = value
        }
    override val startTrackInteractor: StartTrackInteractor
        get() = _startTrackIneractor
    override val stopTrackInteractor: StopTrackInteractor
        get() = _stopTrackInteractor
    override val addPointInteractor: AddPointInteractor
        get() = _addPointInteractor
    override val readSettingsInteractor: ReadSettingsInteractor
        get() = _readSettingsInteractor
    override val addCheckPointInteractor: AddCheckPointInteractor
        get() = _addCheckPointInteractor

    private val locationListeners = CopyOnWriteArrayList<LocationTracker.Listener>()
    private var locationManagerCallback = object : LocationManager.Callback {

        override fun onConnected() {
            log.i("Manager onConnected")
            locationListeners.forEach {
                it.onLocationManagerConnected()
            }
            if (LocationTracker.Status.RECORDING != status || LocationTracker.Status.CONNECTED != status) {
                changeStatus(LocationTracker.Status.CONNECTED)
            }
        }

        override fun onConnectionSuspended(reason: Int) {
            log.w("Manager onSuspended")
            locationListeners.forEach {
                it.onLocationMangerConnectionSuspended(reason)
            }
            changeStatus(LocationTracker.Status.IDLE)
        }

        override fun onConnectionFailed(error: Throwable) {
            log.e( "Manager onFailed")
            locationListeners.forEach {
                it.onLocationMangerConnectionFailed(error)
            }
            changeStatus(LocationTracker.Status.IDLE)
        }

        override fun onLocationChanged(location: Location) {
            log.v("Manager onChanged")
            if (null != activeTrackId && LocationTracker.Status.RECORDING != status) {
                changeStatus(LocationTracker.Status.RECORDING)
            }

            if (isLocationAvailable) {
                locationListeners.forEach {
                    it.onLocationUpdated(location)
                }

                activeTrackId?.let { trackId ->
                    log.v("location: ${location.latitude}: ${location.longitude}")
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
            } else {
                log.w("Location is not available yet. Skip saving point")
            }
        }

        override fun onLocationAvailable(available: Boolean) {
            log.w("onLocationAvailable called, available: $available")
            isLocationAvailable = available
            locationListeners.forEach {
                it.onLocationAvailable(available)
            }
        }
    }

    private val batteryInfoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            log.v("battery level changed to $level")

            if (LocationTracker.Status.RECORDING == status) {
                trackerConfig?.let { config ->
                    if (level < config.batteryLevelPc) {
                        log.w("Recording is stopped because of low battery level")
                        stopRecording()
                    }
                }
            }
        }
    }

    override fun onCreate() {
        log.i("LT onCreate --------")
        super.onCreate()
        changeStatus(LocationTracker.Status.IDLE)
        registerReceiver(batteryInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log.d(
            "<<Service LT started, startId = $startId>>, intent = $intent, activeTrackId = $activeTrackId"
        )
        locationManager = App.instance.locationManager
        locationRepository = App.instance.locationRepository
        settingsRepository = App.instance.settingsRepository

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        log.i("onBind")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        log.d("onUnbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        log.e("LT onDestroy ---------")
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
                    StopTrackInteractor.Param(
                        trackId,
                        System.currentTimeMillis(),
                        trackerConfig?.distanceM
                    ),
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
        } else {
            _recordingEventsListener?.onRecordStartFailed(IllegalStateException("Recording already started, status = $status"))
        }
    }

    override fun stopRecording() {
        log.d("StopRecording")
        activeTrackId?.let { trackId ->
            log.d("!!! activeTrackId is not null")
            locationManager.disconnect()

            taskStopTrack.start(
                StopTrackInteractor.Param(
                    trackId,
                    System.currentTimeMillis(),
                    trackerConfig?.distanceM
                ),
                Unit
            )
        }
        activeTrackId = null

        changeStatus(LocationTracker.Status.IDLE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            log.i("stopForeground")
            stopForeground(true)
        }
    }

    override fun pauseOrResume() {
        val trackId = activeTrackId!!
        when (status) {
            LocationTracker.Status.RECORDING -> {
                val pausePoint = CheckPoint(-1, trackId, CheckPoint.Type.PAUSE, System.currentTimeMillis(), null)
                taskAddCheckPoint.start(AddCheckPointInteractor.Param(pausePoint), Unit)
                locationManager.disconnect()
                changeStatus(LocationTracker.Status.PAUSED)
            }
            LocationTracker.Status.PAUSED -> {
                changeStatus(LocationTracker.Status.RECORDING)
                locationManager.connect(connectionConfig, locationManagerCallback)
                val resumePoint = CheckPoint(-1, trackId, CheckPoint.Type.RESUME, System.currentTimeMillis(), null)
                taskAddCheckPoint.start(AddCheckPointInteractor.Param(resumePoint), Unit)
            }
            else -> {
                // do nothing
            }
        }


    }

    override fun isRecording(): Boolean =
        LocationTracker.Status.RECORDING == status || LocationTracker.Status.PAUSED == status

    override fun isRecordingPaused(): Boolean =
        LocationTracker.Status.PAUSED == status

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
            LocationTracker.Status.NONE -> log.v(message)
            LocationTracker.Status.IDLE -> log.v(message)
            LocationTracker.Status.RETRIEVING_CONFIG -> log.d(message)
            LocationTracker.Status.CONNECTING -> log.w(message)
            LocationTracker.Status.CONNECTED -> log.i(message)
            LocationTracker.Status.RECORDING -> log.e(message)
            LocationTracker.Status.PAUSED -> log.w(message)
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
            log.i("track inserted successfully, id = $trackId")
            activeTrackId = trackId
            _recordingEventsListener?.onRecordStartSucceeded(trackId)
        } else if (null != error) {
            log.e("error inserting track: ${error.localizedMessage}")
            if (LocationTracker.Status.RECORDING == status) {
                changeStatus(LocationTracker.Status.CONNECTED)
            }
            _recordingEventsListener?.onRecordStartFailed(error)
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
        when {
            null != succeeded -> {
                if (!succeeded) {
                    Toast.makeText(
                        this@TrackLocationService,
                        resources.getString(R.string.service_track_was_not_saved),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                _recordingEventsListener?.onRecordStopSucceeded(succeeded)
            }
            null != error -> {
                log.e("Failed to stopTrack")
                _recordingEventsListener?.onRecordStopFailed(error)
            }
            else -> {
                log.i("Stop track succeedded: $succeeded")
            }
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
            log.e("Failed to add point: ${error.localizedMessage}")
        } else {
            log.d("Point add succeeded")
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
        connectionConfig = if (null != result) {
            trackerConfig = LocationTracker.TrackerConfig(result.distance, result.batteryLevel)
            LocationManager.ConnectionConfig(result.timePeriod, result.displacement)
        } else null

        log.d("config = $connectionConfig")
        locationManager.connect(connectionConfig, locationManagerCallback)
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

    private fun handleAddCheckPoint(error: Throwable?) {
        if (null == error) {
            _recordingEventsListener?.onPauseResumeSucceeded(true)
        } else {
            _recordingEventsListener?.onPauseResumeFailed(error)
        }
    }

    private fun createAddCheckPointTask() =
        SingleResultTask<AddCheckPointInteractor.Param, Unit, Unit>(
            TASK_ADD_CHECK_POINT,
            { param, _ ->
                addCheckPointInteractor.asRxSingle(param)
                    .observeOn(AndroidSchedulers.mainThread())
            },
            { _, _ ->
                handleAddCheckPoint(null)
            },
            { error, _ ->
                log.e(error, "Failed to add checkpoint")
                handleAddCheckPoint(error)
            }
        )
}