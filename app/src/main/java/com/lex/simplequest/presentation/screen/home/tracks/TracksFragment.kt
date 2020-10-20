package com.lex.simplequest.presentation.screen.home.tracks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lex.simplequest.App
import com.lex.simplequest.R
import com.lex.simplequest.presentation.base.BaseMvpFragment
import com.lex.simplequest.presentation.screen.home.MainRouter
import com.softeq.android.mvp.PresenterStateHolder
import com.softeq.android.mvp.VoidPresenterStateHolder

class TracksFragment : BaseMvpFragment<TracksFragmentContract.Ui, TracksFragmentContract.Presenter.State, TracksFragmentContract.Presenter>(),
    TracksFragmentContract.Ui {
    companion object {
        fun newInstance(): TracksFragment =
            TracksFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_tracks, container, false)

    override fun setTracks(items: List<Any>) {
        TODO("Not yet implemented")
    }

    override fun getUi(): TracksFragmentContract.Ui =
        this

    override fun createPresenter(): TracksFragmentContract.Presenter =
        TracksFragmentPresenter(
            App.instance.internetConnectivityTracker,
            getTarget(MainRouter::class.java)!!
        )

    override fun createPresenterStateHolder(): PresenterStateHolder<TracksFragmentContract.Presenter.State> =
        VoidPresenterStateHolder()

}