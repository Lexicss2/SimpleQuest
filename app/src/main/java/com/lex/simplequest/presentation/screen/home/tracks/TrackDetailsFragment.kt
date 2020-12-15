package com.lex.simplequest.presentation.screen.home.tracks

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.lex.simplequest.App
import com.lex.simplequest.R
import com.lex.simplequest.databinding.FragmentTrackDetailsBinding
import com.lex.simplequest.device.permission.repository.PermissionCheckerImpl
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.model.toGpxFile
import com.lex.simplequest.domain.permission.repository.PermissionChecker
import com.lex.simplequest.domain.track.interactor.DeleteTrackInteractorImpl
import com.lex.simplequest.domain.track.interactor.ReadTracksInteractorImpl
import com.lex.simplequest.domain.track.interactor.UpdateTrackInteractorImpl
import com.lex.simplequest.presentation.base.BaseMvpFragment
import com.lex.simplequest.presentation.dialog.DialogFragmentClickListener
import com.lex.simplequest.presentation.dialog.SimpleDialogFragment
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.lex.simplequest.presentation.utils.isDialogShown
import com.lex.simplequest.presentation.utils.showDialog
import com.softeq.android.mvp.PresenterStateHolder

class TrackDetailsFragment :
    BaseMvpFragment<TrackDetailsFragmentContract.Ui, TrackDetailsFragmentContract.Presenter.State, TrackDetailsFragmentContract.Presenter>(),
    TrackDetailsFragmentContract.Ui, DialogFragmentClickListener {

    companion object {
        private const val ARG_TRACK_ID = "track_id"
        private const val NO_VALUE = "???"
        private const val DLG_DELETE = "delete"

        private const val REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS = 1002

        fun newInstance(trackId: Long): TrackDetailsFragment =
            TrackDetailsFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_TRACK_ID, trackId)
                }
            }
    }

    private var _viewBinding: FragmentTrackDetailsBinding? = null
    private val viewBinding: FragmentTrackDetailsBinding
        get() = _viewBinding!!

    private val nameChangeListener = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {
            val text = s.toString().trim().replace("\n", "")
            presenter.nameChanged(text)
        }

        override fun afterTextChanged(p0: Editable?) {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentTrackDetailsBinding.inflate(inflater, container, false)
        .also { _viewBinding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewBinding.apply {
            trackNameEditText.addTextChangedListener(nameChangeListener)
            shareButton.setOnClickListener { presenter.shareClicked() }
            deleteButton.setOnClickListener { presenter.deleteClicked() }
        }
        super.onViewCreated(view, savedInstanceState)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding.trackNameEditText.removeTextChangedListener(nameChangeListener)
        _viewBinding = null
    }

    override fun showProgress(show: Boolean) {
        if (show) {
            viewBinding.layoutContent.visibility = View.GONE
            viewBinding.progressBar.visibility = View.VISIBLE
        } else {
            viewBinding.layoutContent.visibility = View.VISIBLE
            viewBinding.progressBar.visibility = View.GONE
        }
    }

    override fun setName(trackName: String?) {
        val value = trackName ?: NO_VALUE
        viewBinding.trackNameEditText.apply {
            removeTextChangedListener(nameChangeListener)
            var customSelectionStart = selectionStart
            var customSelectionEnd = selectionEnd
            setText(value)
            customSelectionStart = minOf(customSelectionStart, value.length)
            customSelectionEnd = minOf(customSelectionEnd, value.length)
            setSelection(customSelectionStart, customSelectionEnd)
            addTextChangedListener(nameChangeListener)
        }

        toolbarInfo.setTitle(value)
    }

    override fun setDistance(distance: String?, inKilo: Boolean) {
        val measure = if (inKilo) resources.getString(R.string.others_km) else resources.getString(R.string.others_m)
        val text = if (null != distance) String.format(
            resources.getString(R.string.track_details_distance),
            "$distance $measure"//distance
        ) else NO_VALUE
        viewBinding.distanceTextView.text = text
    }

    override fun setSpeed(speed: String?) {
        val measure = resources.getString(R.string.others_kmh)
        val text = if (null != speed) String.format(
            resources.getString(R.string.track_details_speed),
            "$speed $measure"//speed
        ) else NO_VALUE
        viewBinding.speedTextView.text = text
    }

    override fun setDuration(duration: String?) {
        val text = if (null != duration) String.format(
            resources.getString(R.string.track_details_duration),
            duration
        ) else NO_VALUE
        viewBinding.durationTextView.text = text
    }

    override fun setPausesCount(pausesCount: Int?) {
        val text = if (null != pausesCount) String.format(
            resources.getString(R.string.track_details_pauses),
            pausesCount
        ) else NO_VALUE
        viewBinding.pausesTextView.text = text
    }

    override fun shareTrack(track: Track) {
        val gpxFile = track.toGpxFile(context!!)
        if (gpxFile.exists()) {
            val intentShareFile = Intent(Intent.ACTION_SEND)
            intentShareFile.type = "vnd.android.cursor.dir/email"
            val path = Uri.fromFile(gpxFile)
            intentShareFile.putExtra(
                Intent.EXTRA_STREAM,
                path
            )
            intentShareFile.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.track_details_share_file_subject)) // Subject
            intentShareFile.putExtra(Intent.EXTRA_TEXT, String.format(resources.getString(R.string.track_details_share_file_text, track.name))) // Text
            startActivity(Intent.createChooser(intentShareFile, resources.getString(R.string.track_details_share_file))) // via
        }
    }

    override fun showDeletePopup() {
        if (!childFragmentManager.isDialogShown(DLG_DELETE)) {
            val dlg = SimpleDialogFragment.newInstance(
                context!!.getString(R.string.track_details_delete_title),
                context!!.getString(R.string.track_details_delete_description),
                context!!.getString(R.string.ok),
                context!!.getString(R.string.cancel)
            )
            childFragmentManager.showDialog(dlg, DLG_DELETE)
        }
    }

    override fun requestPermissions(permissions: Set<PermissionChecker.Permission>) {
        requestPermissions(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (REQUEST_CODE_EXTERNAL_STORAGE_PERMISSIONS == requestCode) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                presenter.permissionsGranted()
            }
        }
    }

    override fun onDialogFragmentClick(
        dialogFragment: DialogFragment,
        dialog: DialogInterface,
        which: Int
    ) {
        when (dialogFragment.tag) {
            DLG_DELETE -> {
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        presenter.deleteConfirmed()
                    }
                }
            }
        }
    }

    override fun showError(error: Throwable) {
        Toast.makeText(context, error.localizedMessage, Toast.LENGTH_SHORT).show()
    }

    override fun getUi(): TrackDetailsFragmentContract.Ui =
        this

    override fun createPresenter(): TrackDetailsFragmentContract.Presenter {
        val args = arguments!!
        return TrackDetailsFragmentPresenter(
            args.getLong(ARG_TRACK_ID),
            ReadTracksInteractorImpl(App.instance.locationRepository),
            UpdateTrackInteractorImpl(App.instance.locationRepository),
            DeleteTrackInteractorImpl(App.instance.locationRepository),
            PermissionCheckerImpl(context!!),
            App.instance.logFactory,
            getTarget(MainRouter::class.java)!!
        )
    }

    override fun createPresenterStateHolder(): PresenterStateHolder<TrackDetailsFragmentContract.Presenter.State> =
        TrackDetailsFragmentPresenterStateHolder()
}