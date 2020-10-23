package com.lex.simplequest.presentation.screen.home.tracks

import com.lex.core.log.LogFactory
import com.lex.simplequest.data.location.repository.AllTracksSpecification
import com.lex.simplequest.domain.common.connectivity.InternetConnectivityTracker
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.repository.LocationRepository
import com.lex.simplequest.domain.track.interactor.ReadTracksInteractor
import com.lex.simplequest.presentation.base.BaseMvpPresenter
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.lex.simplequest.presentation.utils.asRxSingle
import com.lex.simplequest.presentation.utils.tasks.SingleResultTask
import io.reactivex.android.schedulers.AndroidSchedulers

class TracksFragmentPresenter(
    private val readTracksInteractor: ReadTracksInteractor,
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
    }

    private val log = logFactory.get(TAG)
    private val taskReadTracks = createReadTracksTask()

    override fun start() {
        super.start()
        taskReadTracks.start(ReadTracksInteractor.Param(AllTracksSpecification()), Unit)
    }

    override fun stop() {
        super.stop()
        taskReadTracks.stop()
    }

    private fun handleReadTracks(tracks: List<Track>?, error: Throwable?) {
        if (null != tracks) {
            ui.setTracks(tracks)
        }
        //
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