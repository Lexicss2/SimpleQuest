package com.lex.simplequest.presentation.screen.home.tracks

import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.permission.repository.PermissionChecker
import com.lex.simplequest.presentation.base.BaseMvpContract

interface TrackDetailsFragmentContract {
    interface Ui : BaseMvpContract.Ui {
        fun showProgress(show: Boolean)
        fun setName(trackName: String?)
        fun setDistance(distance: String?)
        fun setSpeed(speed: String?)
        fun setDuration(duration: String?)
        fun setPausesCount(pausesCount: Int?)
        fun shareTrack(track: Track)
        fun requestPermissions(permissions: Set<PermissionChecker.Permission>)
        fun showDeletePopup()
        fun showError(error: Throwable)
    }

    interface Presenter : BaseMvpContract.Presenter<Ui, Presenter.State> {
        fun nameChanged(name: String)
        fun shareClicked()
        fun deleteClicked()
        fun deleteConfirmed()
        fun permissionsGranted()

        interface State : BaseMvpContract.Presenter.State {
            var name: String?
        }
    }
}