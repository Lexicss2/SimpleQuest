package com.lex.simplequest.presentation.screen.home.tracks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lex.simplequest.App
import com.lex.simplequest.databinding.FragmentTracksBinding
import com.lex.simplequest.domain.model.Track
import com.lex.simplequest.domain.track.interactor.ReadTracksInteractorImpl
import com.lex.simplequest.presentation.base.BaseMvpFragment
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.softeq.android.mvp.PresenterStateHolder
import com.softeq.android.mvp.VoidPresenterStateHolder
import kotlinx.android.synthetic.main.fragment_tracks.*

class TracksFragment : BaseMvpFragment<TracksFragmentContract.Ui, TracksFragmentContract.Presenter.State, TracksFragmentContract.Presenter>(),
    TracksFragmentContract.Ui {
    companion object {
        fun newInstance(): TracksFragment =
            TracksFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    private var _viewBinding: FragmentTracksBinding? = null
    private val viewBinding: FragmentTracksBinding
    get() = _viewBinding!!

    private var adapterTracks: AdapterTracks? = null

    private val tracksClickListener = object : AdapterTracks.ItemClickListener {
        override fun onTrackClicked(track: Track) {

        }

        override fun onInfoClicked(track: Track) {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentTracksBinding.inflate(inflater, container, false)
        .also { _viewBinding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        tracksListView.layoutManager = LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)
//        tracksListView.adapter =
        adapterTracks = AdapterTracks(context!!, tracksClickListener)
        tracksListView.apply {
            layoutManager = LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)
            adapter = adapterTracks
            setHasFixedSize(true)
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun setTracks(items: List<Track>) {
        adapterTracks?.set(items)
    }

    override fun getUi(): TracksFragmentContract.Ui =
        this

    override fun createPresenter(): TracksFragmentContract.Presenter =
        TracksFragmentPresenter(
            ReadTracksInteractorImpl(App.instance.locationRepository),
            App.instance.internetConnectivityTracker,
            App.instance.logFactory,
            getTarget(MainRouter::class.java)!!
        )

    override fun createPresenterStateHolder(): PresenterStateHolder<TracksFragmentContract.Presenter.State> =
        VoidPresenterStateHolder()

}