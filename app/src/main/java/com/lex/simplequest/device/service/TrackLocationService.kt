package com.lex.simplequest.device.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.lex.core.utils.ObservableValue
import com.lex.simplequest.App
import com.lex.simplequest.domain.locationmanager.LocationManager
import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.locationmanager.model.Location
import com.lex.simplequest.domain.model.Point
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.repository.LocationRepository
import com.lex.simplequest.domain.track.interactor.*
import com.lex.simplequest.presentation.utils.asRxObservable
import com.lex.simplequest.presentation.utils.asRxSingle
import com.lex.simplequest.presentation.utils.tasks.MultiResultTask
import com.lex.simplequest.presentation.utils.tasks.SingleResultTask
import io.reactivex.android.schedulers.AndroidSchedulers
import java.text.SimpleDateFormat
import java.util.*

class TrackLocationService() : Service(), LocationTracker {

    companion object {
        private const val TAG = "TrackLoscationService"
        private const val TASK_START_TRACK = "startTrack"
        private const val TASK_STOP_TRACK = "stopTrack"
        private const val TASK_ADD_POINT = "addPoint"
    }

    private lateinit var locationManager: LocationManager
    private lateinit var locationRepository: LocationRepository
    private val binder = TrackLocationBinder()

    //private var isActive: Boolean = false
    private var status: LocationTracker.Status = LocationTracker.Status.NONE
    private var activeTrack: Track? = null // TODO: May be remove it
    private var activeTrackId: Long? = null
    private val taskStartTrack = createStartTrackTask()
    private val taskStopTrack = createStopTrackTask()
    private val taskAddPoint = createAddPointTask()

    private val pointObservableValue = ObservableValue(Point(-1, -1, 0.0, 0.0, null))

    private val log = App.instance.logFactory.get(TAG)

    private val startTrackInteractor: StartTrackInteractor =
        StartTrackInteractorImpl(App.instance.locationRepository)
    private val stopTrackInteractor: StopTrackInteractor =
        StopTrackInteractorImpl(App.instance.locationRepository)
    private val addPointInteractor: AddPointInteractor =
        AddPointInteractorImpl(App.instance.locationRepository)

    private var _locationTrackerListener: LocationTracker.Listener? = null

    override var locationTrackerListener: LocationTracker.Listener?
        get() = _locationTrackerListener
        set(value) {
            _locationTrackerListener = value
        }
    private var locationManagerCallback = object : LocationManager.Callback {

        override fun onConnected() {
            _locationTrackerListener?.onLocationManagerConnected()
            if (LocationTracker.Status.RECORDING != status || LocationTracker.Status.CONNECTED != status) {
                changeStatus(LocationTracker.Status.CONNECTED)
            }
        }

        override fun onConnectionSuspended(reason: Int) {
            _locationTrackerListener?.onLocationMangerConnectionSuspended(reason)
            changeStatus(LocationTracker.Status.IDLE)
        }

        override fun onConnectionFailed(error: Throwable) {
            _locationTrackerListener?.onLocationMangerConnectionFailed(error)
            changeStatus(LocationTracker.Status.IDLE)
        }

        override fun onLocationChanged(location: Location) {

            if (null != activeTrackId && LocationTracker.Status.RECORDING != status) {
                changeStatus(LocationTracker.Status.RECORDING)
            }

            activeTrackId?.let { trackId ->
                // TODO: RM if Location is recording update current Track
                Log.d("qaz", "location: ${location.latitude}: ${location.longitude}")
                val point =
                    Point(-1, trackId, location.latitude, location.longitude, location.altitude)
                pointObservableValue.set(point)

                if (taskAddPoint.isRunning()) {
                    pointObservableValue.set(point)
                } else {
                    taskAddPoint.start(AddPointInteractor.Param(pointObservableValue), Unit)
                }
            }
        }

        override fun onLocationAvailable(available: Boolean) {
            Log.w("qaz", "onLocationAvailable called, available: $available")
        }
    }

    override fun setup(lm: LocationManager, lr: LocationRepository) {
        locationManager = lm
        locationRepository = lr
    }

    override fun onCreate() {
        Log.i("qaz", "LT onCreate --------")
        super.onCreate()
        //isActive = true
        changeStatus(LocationTracker.Status.IDLE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(
            "qaz",
            "<<Service LT started, startId = $startId>>, intent = $intent, activeTrackId = $activeTrackId"
        )
        locationManager = App.instance.locationManager
        locationRepository = App.instance.locationRepository
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.i("qaz", "onBind")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("qaz", "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.e("qaz", "LT onDestroy ---------")
        super.onDestroy()
        //isActive = false
        changeStatus(LocationTracker.Status.NONE)
        taskStartTrack.stop()
        taskStopTrack.stop()
        taskAddPoint.stop()
        stopRecording()
    }

    override fun testMethod() {
        //Log.i("qaz", "testMethod, isActive: $isActive")
    }

    override fun connect() {
        locationManager.connect(locationManagerCallback)
        changeStatus(LocationTracker.Status.CONNECTING)
    }

    override fun disconnect() {
        locationManager.disconnect()

        activeTrackId?.let { trackId ->
            taskStopTrack.start(
                StopTrackInteractor.Param(trackId, System.currentTimeMillis()),
                Unit
            )
        }
        changeStatus(LocationTracker.Status.IDLE)
    }

    override fun isConnected(): Boolean =
        LocationTracker.Status.CONNECTED == status || LocationTracker.Status.RECORDING == status

    override fun startRecording() {
        if (status == LocationTracker.Status.IDLE) {
            locationManager.connect(locationManagerCallback)
            changeStatus(LocationTracker.Status.CONNECTING)
        }
        taskStartTrack.start(
            StartTrackInteractor.Param(
                generateName(),
                System.currentTimeMillis()
            ), Unit
        )
    }

    override fun stopRecording() {
        Log.d("qaz", "StopRecording")
        activeTrackId?.let { trackId ->
            Log.d("qaz", "!!! activeTrackId is not null")
            locationManager.disconnect()

            taskStopTrack.start(
                StopTrackInteractor.Param(trackId, System.currentTimeMillis()),
                Unit
            )
        }

        changeStatus(LocationTracker.Status.IDLE)
    }

    //    override fun isRecording(): Boolean =
//        locationManager.isConnected()
    override fun isRecording(): Boolean =
        LocationTracker.Status.RECORDING == status

    override fun getLastTrack(): Track? {
        return null
    }

    inner class TrackLocationBinder : Binder() {
        fun getService(): TrackLocationService = this@TrackLocationService
    }

    private fun generateName(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss_SSS", Locale.US)
        return sdf.format(Date())
    }

    private fun changeStatus(newStatus: LocationTracker.Status) {
        Log.d("qaz", "Changing status from $status to $newStatus")
        status = newStatus
    }

    private fun handleStartTrack(trackId: Long?, error: Throwable?) {
        if (null != trackId) {
            Log.i("qaz", "track inserted successfully, id = $trackId")
            activeTrackId = trackId
        } else if (null != error) {
            Log.e("qaz", "error inserting track: ${error.localizedMessage}")
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
        if (null != error) {
            Log.e("qaz", "Failed to stopTrack")
            activeTrackId = null
        } else {
            Log.i("qaz", "Stop track succeedded: $succeeded")
        }
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
            Log.e("qaz", "Failed to add point: ${error.localizedMessage}")
            // TODO: Handle somehow, needs to restart task
        } else {
            Log.d("qaz", "Point add succeeded")
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
}