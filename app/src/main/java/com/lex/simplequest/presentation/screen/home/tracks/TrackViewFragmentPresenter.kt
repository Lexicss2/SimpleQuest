package com.lex.simplequest.presentation.screen.home.tracks

import com.lex.core.log.LogFactory
import com.lex.simplequest.data.location.repository.TrackByIdQuerySpecification
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

    override fun start() {
        super.start()
        taskReadTracks.start(ReadTracksInteractor.Param(TrackByIdQuerySpecification(trackId)), Unit)
    }

    override fun stop() {
        super.stop()
        taskReadTracks.stop()
    }

    private fun updateUi() {
        if (!tracks.isNullOrEmpty()) {
            ui.setTrack(tracks!![0])
        }
    }

    private fun handleReadTracks(tracks: List<Track>?, error: Throwable?) {
        if (null != tracks) {
            this.tracks = tracks
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