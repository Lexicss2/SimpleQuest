package com.lex.simplequest.presentation.screen.home.map

import com.lex.simplequest.domain.locationmanager.LocationTracker
import com.lex.simplequest.domain.locationmanager.model.Location
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.permission.repository.PermissionChecker
import com.lex.simplequest.presentation.base.BaseMvpContract

interface MapFragmentContract {
    interface Ui : BaseMvpContract.Ui {
        fun showStartMarker(location: Location?)
        fun showFinishMarker(
            location: Location?,
            isRecording: Boolean = false,
            shouldMoveCamera: Boolean = false
        )

        fun showTrack(
            track: Track?,
            isRecording: Boolean = false,
            shouldMoveCamera: Boolean = false
        )

        fun showIndicatorProgress(text: String)
        fun showError(error: Throwable)
        fun requestPermissions(permissions: Set<PermissionChecker.Permission>)
        fun showLocationPermissionRationale()
    }

    interface Presenter : BaseMvpContract.Presenter<Ui, Presenter.State> {
        fun locationTrackerServiceConnected(locationTracker: LocationTracker)
        fun locationTrackerServiceDisconnected()
        fun mapReady()
        fun refreshClicked()
        fun permissionsGranted()
        fun permissionsDenied()

        interface State : BaseMvpContract.Presenter.State {
            var location: Location?
            var locationsReceivedCount: Int
        }
    }
}