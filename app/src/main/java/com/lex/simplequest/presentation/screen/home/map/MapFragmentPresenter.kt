package com.lex.simplequest.presentation.screen.home.map

import com.lex.core.log.LogFactory
import com.lex.simplequest.data.location.repository.queries.LatestTrackQuerySpecification
import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.locationmanager.model.Location
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.track.interactor.ReadTracksInteractor
import com.lex.simplequest.presentation.base.BaseMvpPresenter
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.lex.simplequest.presentation.utils.asRxSingle
import com.lex.simplequest.presentation.utils.tasks.SingleResultTask
import io.reactivex.android.schedulers.AndroidSchedulers

class MapFragmentPresenter(
    private val readTracksInteractor: ReadTracksInteractor,
    internetConnectivityTracker: InternetConnectivityTracker,
    logFactory: LogFactory,
    router: MainRouter
) : BaseMvpPresenter<MapFragmentContract.Ui, MapFragmentContract.Presenter.State, MainRouter>(router),
    MapFragmentContract.Presenter {

    companion object {
        private const val TAG = "MapFragmentPresenter"
        private const val TASK_READ_TRACKS = "taskReadTracks"
        private const val FLAG_LOCATION = 0x0001
        private const val FLAG_TRACK = 0x0002
        private const val FLAG_SETUP_ALL = FLAG_LOCATION or FLAG_TRACK
    }

    private val log = logFactory.get(TAG)
    private val taskReadTracks = createReadTracksTask()

    private var location: Location? = null
    private var connectedLocationTracker: LocationTracker? = null
    private var locationsReceivedCount = 0
    private var latestTrack: Track? = null

    private val trackingListener = object : LocationTracker.Listener {
        override fun onLocationManagerConnected() {
            updateUi(FLAG_LOCATION)
        }

        override fun onLocationMangerConnectionSuspended(reason: Int) {

        }

        override fun onLocationMangerConnectionFailed(error: Throwable) {

        }

        override fun onLocationUpdated(location: Location) {
            this@MapFragmentPresenter.location = location
            if (++locationsReceivedCount % 10 == 0) {
                latestTrack?.let { track ->
                    if (null == track.endTime) {
                        updateUi(FLAG_SETUP_ALL)
                    }
                }
            }
            updateUi(FLAG_LOCATION)
        }

        override fun onStatusUpdated(status: LocationTracker.Status) {

        }

        override fun onLocationAvailable(isAvailable: Boolean) {

        }
    }

    override fun saveState(state: MapFragmentContract.Presenter.State) {
        super.saveState(state)
        location?.let {
            state.location = it
        }
        state.locationsReceivedCount = locationsReceivedCount
    }

    override fun restoreState(savedState: MapFragmentContract.Presenter.State?) {
        super.restoreState(savedState)
        savedState?.let {
            location = it.location
            locationsReceivedCount = it.locationsReceivedCount
        }
    }

    override fun start() {
        super.start()
        taskReadTracks.start(ReadTracksInteractor.Param(LatestTrackQuerySpecification()), Unit)
    }

    override fun stop() {
        super.stop()
        taskReadTracks.stop()
    }

    override fun locationTrackerConnected(locationTracker: LocationTracker) {
        connectedLocationTracker = locationTracker
        connectedLocationTracker?.addListener(trackingListener)
        connectedLocationTracker?.connect()
        updateUi(0)
    }

    override fun locationTrackerDisconnected() {
        connectedLocationTracker?.disconnect()
        connectedLocationTracker?.removeListener(trackingListener)
        connectedLocationTracker = null
        updateUi(0)
    }

    override fun mapReady() {
        updateUi(FLAG_LOCATION)
    }

    override fun refreshClicked() {
        location?.let {
            ui.updateMarker(it)
        }
    }

    private fun updateUi(flag: Int) {
        if (flag and FLAG_LOCATION != 0) {
            location?.let {
                ui.showMarkerIfNeeded(it)
            }
        }

        if (flag and FLAG_TRACK != 0) {
            latestTrack?.let { track ->
                ui.setTrack(track)
            }
        }
    }

    private fun handleReadTracks(tracks: List<Track>?, error: Throwable?) {
        var flags = 0
        if (null != tracks) {
            val latestTrack = if (tracks.isNotEmpty()) tracks[0] else null
            this.latestTrack = latestTrack
            val isRecording = latestTrack != null && null == latestTrack.endTime

            if (null == location && null != latestTrack && latestTrack.points.isNotEmpty()) {
                val lastPoint = latestTrack.points.last()
                location = Location(lastPoint.latitude, lastPoint.longitude, lastPoint.altitude)
            }

            flags = if (isRecording) {
                FLAG_TRACK
            } else {
                FLAG_LOCATION
            }
        } else if (null != error) {
            // handle error
        }
        updateUi(flags)
    }

    private fun createReadTracksTask() =
        SingleResultTask<ReadTracksInteractor.Param, ReadTracksInteractor.Result, Unit>(
            TASK_READ_TRACKS,
            { param, _ ->
                readTracksInteractor.asRxSingle(param)
                    .observeOn(AndroidSchedulers.mainThread())
            },
            { result, _ ->
                handleReadTracks(result.tracks, null)
            },
            { error, _ ->
                log.e(error, "Failed to read tracks")
                handleReadTracks(null, error)
            }
        )
}