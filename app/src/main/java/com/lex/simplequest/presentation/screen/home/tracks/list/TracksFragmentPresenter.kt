package com.lex.simplequest.presentation.screen.home.tracks.list

import android.util.Log
import com.lex.core.log.LogFactory
import com.lex.simplequest.data.location.repository.queries.AllTracksQuerySpecification
import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.track.interactor.ReadTracksCountInteractor
import com.lex.simplequest.domain.track.interactor.ReadTracksInteractor
import com.lex.simplequest.presentation.base.BaseMvpPresenter
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.lex.simplequest.presentation.utils.asRxSingle
import com.lex.simplequest.presentation.utils.tasks.SingleResultTask
import io.reactivex.android.schedulers.AndroidSchedulers

class TracksFragmentPresenter(
    private val readTracksInteractor: ReadTracksInteractor,
    private val readTracksCountInteractor: ReadTracksCountInteractor,
    internetConnectivityTracker: InternetConnectivityTracker,
    logFactory: LogFactory,
    router: MainRouter
) : BaseMvpPresenter<TracksFragmentContract.Ui, TracksFragmentContract.Presenter.State, MainRouter>(
    router
),
    TracksFragmentContract.Presenter {

    companion object {
        private const val TAG = "TracksFragmentPresenter"
        private const val TASK_READ_TRACKS = "taskReadTracks"
        private const val TASK_READ_TRACKS_COUNT = "taskReadTracksCount"

        private const val FLAG_SET_TRACKS = 0x0001
    }

    private val log = logFactory.get(TAG)
    private val taskReadTracks = createReadTracksTask()
    private val taskReadTracksCount = createReadTracksCountTask()
    private var tracks = emptyList<Track>()

    override fun start() {
        super.start()
        if (tracks.isEmpty()) {
            taskReadTracks.start(ReadTracksInteractor.Param(AllTracksQuerySpecification()), Unit)
        } else {
            taskReadTracksCount.start(ReadTracksCountInteractor.Param(), Unit)
        }
        updateUi(FLAG_SET_TRACKS)
    }

    override fun stop() {
        super.stop()
        taskReadTracks.stop()
        taskReadTracksCount.stop()
    }

    override fun trackClicked(track: Track) {
        router.showTrackView(track.id, switchFromTrackDetails = false)
    }

    override fun trackInfoClicked(track: Track) {
        router.showTrackDetails(track.id, switchFromTrackView = false)
    }

    private fun updateUi(flags: Int) {
        val inProgress = taskReadTracks.isRunning()
        ui.showProgress(inProgress)

        if (!inProgress) {
            if (tracks.isNotEmpty()) {
                if (0 != (flags and FLAG_SET_TRACKS)) {
                    ui.setTracks(tracks)
                }
            } else {
                ui.showNoContent()
            }
        }
    }

    private fun handleReadTracks(tracks: List<Track>?, error: Throwable?) {
        if (null != tracks) {
            this.tracks = tracks
            updateUi(FLAG_SET_TRACKS)
        } else if (null != error) {
            ui.showError(error)
            updateUi(0)
        }
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

    private fun handleReadTracksCount(count: Int?, error: Throwable?) {
        if (null != count) {
            Log.d("qaz", "count = $count")
            if (tracks.size != count) {
                taskReadTracks.stop()
                taskReadTracks.start(ReadTracksInteractor.Param(AllTracksQuerySpecification()), Unit)
                updateUi(0)
            } else {
                updateUi(FLAG_SET_TRACKS)
            }

        } else if (error != null) {
            ui.showError(error)
            updateUi(0)
        }
    }

    private fun createReadTracksCountTask() =
        SingleResultTask<ReadTracksCountInteractor.Param, ReadTracksCountInteractor.Result, Unit>(
            TASK_READ_TRACKS_COUNT,
            { param, _ ->
                readTracksCountInteractor.asRxSingle(param)
                    .observeOn(AndroidSchedulers.mainThread())
            },
            {result, _ ->
                handleReadTracksCount(result.count, null)
            },
            { error, _ ->
                log.e(error, "Read tracks count failed")
                handleReadTracksCount(null, error)
            }
        )
}