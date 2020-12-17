package com.lex.simplequest.presentation.screen.home.tracks.map

import com.lex.simplequest.domain.locationmanager.model.Location
import com.lex.core.log.LogFactory
import com.lex.simplequest.data.location.repository.queries.TrackByIdQuerySpecification
import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.track.interactor.ReadTracksInteractor
import com.lex.simplequest.presentation.base.BaseMvpPresenter
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.lex.simplequest.presentation.utils.asRxSingle
import com.lex.simplequest.presentation.utils.tasks.SingleResultTask
import io.reactivex.android.schedulers.AndroidSchedulers

class TrackViewFragmentPresenter(
    private val trackId: Long,
    private val readTracksInteractor: ReadTracksInteractor,
    internetConnectivityTracker: InternetConnectivityTracker,
    logFactory: LogFactory,
    router: MainRouter
) : BaseMvpPresenter<TrackViewFragmentContract.Ui, TrackViewFragmentContract.Presenter.State, MainRouter>(
    router
),
    TrackViewFragmentContract.Presenter {

    companion object {
        private const val TAG = "TrackViewFragmentPresenter"
        private const val TASK_READ_TRACKS = "taskReadTracks"
    }

    private val log = logFactory.get(TAG)
    private val taskReadTracks = createReadTracksTask()
    private var tracks: List<Track>? = null
    private var wasCameraMoved: Boolean = false

    override fun start() {
        super.start()
        taskReadTracks.start(ReadTracksInteractor.Param(TrackByIdQuerySpecification(trackId)), Unit)
    }

    override fun stop() {
        super.stop()
        taskReadTracks.stop()
    }

    override fun mapReady() {
        updateUi()
    }

    override fun detailsClicked() {
        router.showTrackDetails(trackId, switchFromTrackView = true)
    }

    private fun updateUi() {
        val track = tracks?.let {
            if (it.isNotEmpty()) it.first() else null
        }

        if (null != track) {
            if (track.points.isNotEmpty()) {
                val firstPoint = track.points.first()
                val startLocation =
                    Location(firstPoint.latitude, firstPoint.longitude, firstPoint.altitude)
                if (1 == track.points.size) {
                    ui.showStartMarker(null)
                    ui.showFinishMarker(startLocation, shouldMoveCamera = !wasCameraMoved)
                } else {
                    val lastPoint = track.points.last()
                    val lastLocation =
                        Location(lastPoint.latitude, lastPoint.longitude, lastPoint.altitude)
                    ui.showStartMarker(startLocation)
                    ui.showFinishMarker(lastLocation, shouldMoveCamera = false)
                    ui.showTrack(track, shouldMoveCamera = !wasCameraMoved)
                }

                if (!wasCameraMoved) {
                    wasCameraMoved = true
                }
            } else {
                ui.showStartMarker(null)
                ui.showFinishMarker(null)
                ui.showTrack(null)
            }
        }
    }

    private fun handleReadTracks(tracks: List<Track>?, error: Throwable?) {
        if (null != tracks) {
            wasCameraMoved = false
            this.tracks = tracks
        } else if (null != error) {
            ui.showError(error)
        }

        updateUi()
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