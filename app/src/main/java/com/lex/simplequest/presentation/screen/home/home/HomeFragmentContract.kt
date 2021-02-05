package com.lex.simplequest.presentation.screen.home.home

import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.permission.repository.PermissionChecker
import com.lex.simplequest.presentation.base.BaseMvpLceContract

interface HomeFragmentContract {

    interface Ui : BaseMvpLceContract.Ui {
        fun setButtonStyleRecording(recordingStatus: RecordingStatus?)
        fun showLastTrackName(trackName: String?, recordingStatus: RecordingStatus?)
        fun showProgress(show: Boolean)
        fun showLastTrackDuration(minutes: String?, seconds: String?)
        fun showLastTrackDistance(distance: String?, withBoldStyle: Boolean)
        fun showSpeed(speed: String?, isCurrent: Boolean)
        fun setLocationAvailableStatus(isAvailable: Boolean?)
        fun setLocationSuspendedStatus(reason: Int?)
        fun setError(error: Throwable?)
        fun setTrackerStatus(status: LocationTracker.Status?, tag: String?)
        fun requestPermissions(permissions: Set<PermissionChecker.Permission>)
        fun showLocationPermissionRationale()
    }

    interface Presenter : BaseMvpLceContract.Presenter<Ui, Presenter.State> {
        fun startStopClicked()
        fun pauseResumeClicked()
        fun locationTrackerServiceConnected(locationTracker: LocationTracker)
        fun locationTrackerServiceDisconnected()
        fun permissionsGranted()
        fun permissionsDenied()

        interface State : BaseMvpLceContract.Presenter.State {
            var isLocationAvailable: Boolean?
            var locationSuspendedReason: Int?
            var isRecordingRequested: Boolean?
        }
    }
}