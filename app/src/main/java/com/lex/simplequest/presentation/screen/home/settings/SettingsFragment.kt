package com.lex.simplequest.presentation.screen.home.settings

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

class SettingsFragment :
    BaseMvpFragment<SettingsFragmentContract.Ui, SettingsFragmentContract.Presenter.State, SettingsFragmentContract.Presenter>(),
    SettingsFragmentContract.Ui {
    companion object {
        fun newInstance(): SettingsFragment =
            SettingsFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_settings, container, false)


    override fun getUi(): SettingsFragmentContract.Ui =
        this

    override fun createPresenter(): SettingsFragmentContract.Presenter =
        SettingsFragmentPresenter(
            App.instance.internetConnectivityTracker,
            getTarget(MainRouter::class.java)!!
        )

    override fun createPresenterStateHolder(): PresenterStateHolder<SettingsFragmentContract.Presenter.State> =
        VoidPresenterStateHolder()

}