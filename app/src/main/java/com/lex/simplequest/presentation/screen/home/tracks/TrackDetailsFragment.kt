package com.lex.simplequest.presentation.screen.home.tracks

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lex.simplequest.App
import com.lex.simplequest.R
import com.lex.simplequest.databinding.FragmentTrackDetailsBinding
import com.lex.simplequest.domain.track.interactor.ReadTracksInteractorImpl
import com.lex.simplequest.domain.track.interactor.UpdateTrackInteractorImpl
import com.lex.simplequest.presentation.base.BaseMvpFragment
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.softeq.android.mvp.PresenterStateHolder

class TrackDetailsFragment :
    BaseMvpFragment<TrackDetailsFragmentContract.Ui, TrackDetailsFragmentContract.Presenter.State, TrackDetailsFragmentContract.Presenter>(),
    TrackDetailsFragmentContract.Ui {

    companion object {
        private const val ARG_TRACK_ID = "track_id"
        private const val NO_VALUE = "???"

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
            val text = s.toString().trim().replace("\n","")
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
        viewBinding.trackNameEditText.addTextChangedListener(nameChangeListener)
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
        viewBinding.trackNameEditText.apply {
            removeTextChangedListener(nameChangeListener)
            val value = trackName ?: NO_VALUE
            var customSelectionStart = selectionStart
            var customSelectionEnd = selectionEnd
            setText(value)
            customSelectionStart = minOf(customSelectionStart, value.length)
            customSelectionEnd = minOf(customSelectionEnd, value.length)
            setSelection(customSelectionStart, customSelectionEnd)
            addTextChangedListener(nameChangeListener)
        }
    }

    override fun setDistance(distance: String?) {
        val text = if (null != distance) String.format(resources.getString(R.string.track_details_distance), distance) else NO_VALUE
        viewBinding.distanceTextView.text = text
    }

    override fun setSpeed(speed: String?) {
        val text = if (null != speed) String.format(resources.getString(R.string.track_details_speed), speed) else NO_VALUE
        viewBinding.speedTextView.text = text
    }

    override fun setDuration(duration: String?) {
        val text = if (null != duration) String.format(resources.getString(R.string.track_details_duration), duration) else NO_VALUE
        viewBinding.durationTextView.text = text
    }

    override fun getUi(): TrackDetailsFragmentContract.Ui =
        this

    override fun createPresenter(): TrackDetailsFragmentContract.Presenter {
        val args = arguments!!
        return TrackDetailsFragmentPresenter(
            args.getLong(ARG_TRACK_ID),
            ReadTracksInteractorImpl(App.instance.locationRepository),
            UpdateTrackInteractorImpl(App.instance.locationRepository),
            App.instance.logFactory,
            getTarget(MainRouter::class.java)!!
        )
    }

    override fun createPresenterStateHolder(): PresenterStateHolder<TrackDetailsFragmentContract.Presenter.State> =
        TrackDetailsFragmentPresenterStateHolder()
}