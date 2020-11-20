package com.lex.simplequest.presentation.screen.home.tracks

import android.util.Log
import com.lex.core.log.LogFactory
import com.lex.simplequest.Config
import com.lex.simplequest.data.location.repository.queries.TrackByIdQuerySpecification
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.model.averageSpeed
import com.lex.simplequest.domain.model.distance
import com.lex.simplequest.domain.model.duration
import com.lex.simplequest.domain.track.interactor.ReadTracksInteractor
import com.lex.simplequest.domain.track.interactor.UpdateTrackInteractor
import com.lex.simplequest.presentation.base.BaseMvpPresenter
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.lex.simplequest.presentation.utils.asRxSingle
import com.lex.simplequest.presentation.utils.tasks.MultiResultTask
import com.lex.simplequest.presentation.utils.tasks.SingleResultTask
import com.lex.simplequest.presentation.utils.toSingleStringDurations
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class TrackDetailsFragmentPresenter(
    private val trackId: Long,
    private val readTracksInteractor: ReadTracksInteractor,
    private val updateTrackInteractor: UpdateTrackInteractor,
    logFactory: LogFactory,
    router: MainRouter
) : BaseMvpPresenter<TrackDetailsFragmentContract.Ui, TrackDetailsFragmentContract.Presenter.State, MainRouter>(
    router
),
    TrackDetailsFragmentContract.Presenter {

    companion object {
        private const val TAG = "TrackDetailsFragmentPresenter"
        private const val TASK_READ_TRACK = "taskReadTrack"
        private const val TASK_UPDATE_TRACK = "taskReadTrack"
    }

    private val log = logFactory.get(TAG)
    private val taskReadTrack = createReadTrackTask()

    private var track: Track? = null
    private val nameToUpdate: BehaviorSubject<String> = BehaviorSubject.create()
    private val taskUpdateTrackWhenChanged =
        createUpdatTrackWhenChangedTask(nameToUpdate as Observable<String>)

    override fun start() {
        super.start()
        taskReadTrack.start(ReadTracksInteractor.Param(TrackByIdQuerySpecification(trackId)), Unit)
        updateUi()
    }

    override fun stop() {
        super.stop()
        taskReadTrack.stop()
        taskUpdateTrackWhenChanged.stop()
    }

    override fun nameChanged(name: String) {
        Log.d("qaz", "taskUpdater is running: ${taskUpdateTrackWhenChanged.isRunning()}")
        track?.let {
            if (name.isNotBlank() && it.name != name) {
                Log.d("qaz", "Update to $name")
                nameToUpdate.onNext(name)
            }
        }
    }

    override fun shareClicked() {
        TODO("Not yet implemented")
    }

    override fun deleteClicked() {
        TODO("Not yet implemented")
    }

    override fun deleteConfirmed() {
        TODO("Not yet implemented")
    }

    private fun updateUi() {
        if (isUiBinded) {
            ui.showProgress(taskReadTrack.isRunning() /*|| taskUpdateTrack.isRunning()*/)
            ui.setName(track?.name)
            track?.distance()?.let { d ->
                if (d >= Config.METERS_IN_KILOMETER) {
                    ui.setDistance(String.format("%.2f km", d / Config.METERS_IN_KILOMETER))
                } else {
                    ui.setDistance(String.format("%.2f m", d))
                }
            } ?: ui.setDistance(null)

            track?.averageSpeed()?.let { v ->
                ui.setSpeed(String.format("%.2f km/h", v))
            } ?: ui.setSpeed(null)

            track?.duration()?.let { t ->
                val durationStr = t.toSingleStringDurations()
                ui.setDuration(durationStr)
            } ?: ui.setDuration(null)
        }
    }

    private fun handleReadTrack(track: Track?, error: Throwable?) {
        if (null != track) {
            // ui.setTrack(track)
            this.track = track
            taskUpdateTrackWhenChanged.start(track, Unit)
        } else if (null != error) {
            // handle
        }

        updateUi()
    }

    private fun createReadTrackTask() =
        SingleResultTask<ReadTracksInteractor.Param, ReadTracksInteractor.Result, Unit>(
            TASK_READ_TRACK,
            { param, _ ->
                readTracksInteractor.asRxSingle(param)
                    .observeOn(AndroidSchedulers.mainThread())
            },
            { result, _ ->
                handleReadTrack(result.tracks.first(), null)
            },
            { error, _ ->
                log.e(error, "Faild to read track $trackId")
                handleReadTrack(null, error)
            }
        )

    private fun handleChange(track: Track?, error: Throwable?) {
        if (null != track) {
            this.track = track
        } else if (null != error) {
            // show error
        }
        updateUi()
    }

    private fun createUpdatTrackWhenChangedTask(textObservable: Observable<String>) =
        MultiResultTask<Track, UpdateTrackInteractor.Result, Unit>(
            TASK_UPDATE_TRACK,
            { track, _ ->
                textObservable.debounce(500L, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .switchMap { name ->
                        val updatedTrack = track.copy(name = name)
                        val param = UpdateTrackInteractor.Param(updatedTrack)
                        updateTrackInteractor.asRxSingle(param)
                            .observeOn(AndroidSchedulers.mainThread())
                            .toObservable()
                    }
            },
            { result, _ ->
                Log.i("qaz", "result: $result")
                handleChange(result.track, null)
            },
            { error, _ ->
                Log.e("qaz", "error: $error")
                handleChange(null, error)
            }
        )
}