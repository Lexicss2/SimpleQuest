package com.lex.simplequest.presentation.screen.home.tracks

import android.util.Log
import com.lex.core.log.LogFactory
import com.lex.simplequest.Config
import com.lex.simplequest.data.location.repository.queries.TrackByIdQuerySpecification
import com.lex.simplequest.domain.model.*
import com.lex.simplequest.domain.permission.repository.PermissionChecker
import com.lex.simplequest.domain.track.interactor.DeleteTrackInteractor
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
    private val deleteTrackInteractor: DeleteTrackInteractor,
    private val permissionChecker: PermissionChecker,
    logFactory: LogFactory,
    router: MainRouter
) : BaseMvpPresenter<TrackDetailsFragmentContract.Ui, TrackDetailsFragmentContract.Presenter.State, MainRouter>(
    router
),
    TrackDetailsFragmentContract.Presenter {

    companion object {
        private const val TAG = "TrackDetailsFragmentPresenter"
        private const val TASK_READ_TRACK = "taskReadTrack"
        private const val TASK_UPDATE_TRACK = "taskUpdateTrack"
        private const val TASK_DELETE_TRACK = "taskDeleteTrack"
    }

    private val log = logFactory.get(TAG)
    private val taskReadTrack = createReadTrackTask()

    private var track: Track? = null
    private val nameToUpdate: BehaviorSubject<String> = BehaviorSubject.create()
    private val taskUpdateTrackWhenChanged =
        createUpdateTrackWhenChangedTask(nameToUpdate as Observable<String>)
    private val taskDeleteTrack = createDeleteTrackTask()

    override fun start() {
        super.start()
        taskReadTrack.start(ReadTracksInteractor.Param(TrackByIdQuerySpecification(trackId)), Unit)
        updateUi()
    }

    override fun stop() {
        super.stop()
        taskReadTrack.stop()
        taskUpdateTrackWhenChanged.stop()
        taskDeleteTrack.stop()
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
        track?.let {
            val permissionsSet = setOf(
                PermissionChecker.Permission.READ_EXTERNAL_STORAGE,
                PermissionChecker.Permission.WRITE_EXTERNAL_STORAGE
            )
            if (permissionChecker.checkAllPermissionGranted(permissionsSet)
            ) {
                ui.shareTrack(it)
            } else {
                ui.requestPermissions(permissionsSet)
            }
        }
    }

    override fun permissionsGranted() {
        track?.let {
            ui.shareTrack(it)
        }
    }

    override fun deleteClicked() {
        ui.showDeletePopup()
    }

    override fun deleteConfirmed() {
        track?.let {
            if (!taskDeleteTrack.isRunning()) {
                taskDeleteTrack.start(DeleteTrackInteractor.Param(it.id), Unit)
            }
        }
    }

    private fun updateUi() {
        if (isUiBinded) {
            ui.showProgress(taskReadTrack.isRunning() || taskDeleteTrack.isRunning())
            ui.setName(track?.name)
            track?.fullDistance()?.let { d ->
                if (d >= Config.METERS_IN_KILOMETER) {
                    ui.setDistance(String.format("%.2f km", d / Config.METERS_IN_KILOMETER))
                } else {
                    ui.setDistance(String.format("%.2f m", d))
                }
            } ?: ui.setDistance(null)

            track?.averageSpeed()?.let { v ->
                ui.setSpeed(String.format("%.2f km/h", v))
            } ?: ui.setSpeed(null)

            track?.fullDuration()?.let { t ->
                val durationStr = t.toSingleStringDurations()
                ui.setDuration(durationStr)
            } ?: ui.setDuration(null)

            track?.let { t ->
                val pausesCount = t.checkPoints.filter { it.type == CheckPoint.Type.PAUSE }.size
                ui.setPausesCount(pausesCount)
            } ?: ui.setPausesCount(null)
        }
    }

    private fun handleReadTrack(track: Track?, error: Throwable?) {
        if (null != track) {
            // ui.setTrack(track)
            this.track = track
            val param = UpdateTrackInteractor.Param(track)
            taskUpdateTrackWhenChanged.start(param, Unit)
        } else if (null != error) {
            ui.showError(error)
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
            ui.showError(error)
        }
        updateUi()
    }

    private fun createUpdateTrackWhenChangedTask(textObservable: Observable<String>) =
        MultiResultTask<UpdateTrackInteractor.Param, UpdateTrackInteractor.Result, Unit>(
            TASK_UPDATE_TRACK,
            { param, _ ->
                textObservable.debounce(500L, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .switchMap { name ->
                        val updatedParam = param.copy(track = param.track.copy(name = name))
                        updateTrackInteractor.asRxSingle(updatedParam)
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

    private fun handleDeleteTrack(succeeded: Boolean?, error: Throwable?) {
        if (null != succeeded) {
            if (succeeded) {
                router.goBack()
            }
        } else if (null != error) {
            ui.showError(error)
        }
    }

    private fun createDeleteTrackTask() =
        SingleResultTask<DeleteTrackInteractor.Param, DeleteTrackInteractor.Result, Unit>(
            TASK_DELETE_TRACK,
            { param, _ ->
                deleteTrackInteractor.asRxSingle(param)
                    .observeOn(AndroidSchedulers.mainThread())
            },
            { result, _ ->
                handleDeleteTrack(result.succeeded, null)
            },
            { error, _ ->
                log.e(error, "Faild to read track $trackId")
                handleDeleteTrack(null, error)
            }
        )
}