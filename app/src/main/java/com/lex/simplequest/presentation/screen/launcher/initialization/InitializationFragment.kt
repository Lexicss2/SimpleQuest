package com.lex.simplequest.presentation.screen.launcher.initialization

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.lex.simplequest.App
import com.lex.simplequest.R
import com.lex.simplequest.data.location.repository.LocationRepositoryImpl
import com.lex.simplequest.domain.application.interactor.StartApplicationInteractorImpl
import com.lex.simplequest.presentation.base.BaseDialogFragment
import com.lex.simplequest.presentation.base.BaseMvpFragment
import com.lex.simplequest.presentation.base.DefaultErrorHandler
import com.lex.simplequest.presentation.dialog.DialogFragmentClickListener
import com.lex.simplequest.presentation.dialog.DialogFragmentDismissListener
import com.lex.simplequest.presentation.dialog.SimpleDialogFragment
import com.lex.simplequest.presentation.screen.launcher.LauncherRouter
import com.lex.simplequest.presentation.utils.isDialogShown
import com.lex.simplequest.presentation.utils.showDialog
import com.softeq.android.mvp.PresenterStateHolder

class InitializationFragment :
    BaseMvpFragment<InitializationFragmentContract.Ui, InitializationFragmentContract.Presenter.State, InitializationFragmentContract.Presenter>(),
    InitializationFragmentContract.Ui,
    DialogFragmentClickListener, DialogFragmentDismissListener {

    companion object {
        private const val DLG_ENABLE_LOCATION_SERVICES = "enableLocationServices"
        private const val DLG_LOCATION_PERMISSION_RATIONALE = "locationPermissionRationale"
        private const val REQUEST_CODE_LOCATION_PERMISSIONS = 1000
        private const val REQUEST_CODE_LOCATION_SERVICES = 1001

        fun newInstance(): InitializationFragment =
            InitializationFragment().apply {
                arguments = Bundle()
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_initialization, container, false)

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (REQUEST_CODE_LOCATION_PERMISSIONS == requestCode) {
            presenter.locationPermissionsUpdated()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (REQUEST_CODE_LOCATION_SERVICES == requestCode) {
            presenter.locationServicesUpdated(true)
        }
    }

    override fun onStart() {
        super.onStart()

    }

    override fun showErrorPopup(error: Throwable) {
        // do nothing
    }

    override fun requestLocationPermissions() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            val dlg = SimpleDialogFragment.newInstance(
                context!!.getString(R.string.init_location_permission_rationale_title),
                context!!.getString(R.string.init_location_permission_rationale_message),
                context!!.getString(R.string.ok),
                null
            )
            dlg.isCancelable = false
            childFragmentManager.showDialog(dlg, DLG_LOCATION_PERMISSION_RATIONALE)
        } else {
            requestLocationPermissionsInt()
        }
    }

    override fun requestEnableLocationServices() {
        if (!childFragmentManager.isDialogShown(DLG_ENABLE_LOCATION_SERVICES)) {
            val dlg = SimpleDialogFragment.newInstance(
                context!!.getString(R.string.init_location_services_disabled_title),
                context!!.getString(R.string.init_location_services_disabled_message),
                context!!.getString(R.string.go_to_settings),
                context!!.getString(R.string.no_thanks)
            )
            dlg.isCancelable = false
            childFragmentManager.showDialog(dlg, DLG_ENABLE_LOCATION_SERVICES)
        }
    }

    override fun showProgress(show: Boolean) {
        // do nothing
    }

    override fun onDialogFragmentClick(
        dialogFragment: DialogFragment,
        dialog: DialogInterface,
        which: Int
    ) {
        when (dialogFragment.tag) {
            DLG_ENABLE_LOCATION_SERVICES -> {
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        val locationServicesIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivityForResult(locationServicesIntent, REQUEST_CODE_LOCATION_SERVICES)
                    }
                    else -> {
                        presenter.locationServicesUpdated(false)
                    }
                }
            }
        }
    }

    override fun onDialogFragmentDismiss(
        dialogFragment: BaseDialogFragment,
        dialog: DialogInterface
    ) {
        when (dialogFragment.tag) {
            DefaultErrorHandler.DLG_ERROR -> {
                presenter.errorPopupDismissed()
            }
            DLG_LOCATION_PERMISSION_RATIONALE -> {
                requestLocationPermissionsInt()
            }
        }
    }

    private fun requestLocationPermissionsInt() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_CODE_LOCATION_PERMISSIONS
        )
    }

    override fun getUi(): InitializationFragmentContract.Ui =
        this

    override fun createPresenter(): InitializationFragmentContract.Presenter =
        InitializationFragmentPresenter(StartApplicationInteractorImpl(App.instance.permissionChecker, App.instance.locationRepository),App.instance.logFactory, activity as LauncherRouter)

    override fun createPresenterStateHolder(): PresenterStateHolder<InitializationFragmentContract.Presenter.State> =
        InitializationFragmentPresenterStateHolder()

}