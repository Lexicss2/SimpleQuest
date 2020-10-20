package com.lex.simplequest.presentation.base

import android.os.Bundle
import android.view.View
import com.softeq.android.mvp.MvpFragmentDelegate
import com.softeq.android.mvp.PresenterStateHolder

abstract class BaseMvpDialogFragment<U : BaseMvpContract.Ui, S : BaseMvpContract.Presenter.State, P : BaseMvpContract.Presenter<U, S>> :
    BaseDialogFragment(), MvpFragmentDelegate.Callback<U, S, P>, BaseMvpContract.Ui {

    private lateinit var mMvpDelegate: MvpFragmentDelegate<U, S, P>

    protected val presenter: P
        get() = mMvpDelegate.presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMvpDelegate = MvpFragmentDelegate(this, createPresenterStateHolder())
        mMvpDelegate.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        mMvpDelegate.onStart()
        presenter.start()
    }

    override fun onStop() {
        super.onStop()
        mMvpDelegate.onStop()
        presenter.stop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mMvpDelegate.onViewCreated(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mMvpDelegate.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMvpDelegate.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mMvpDelegate.onSaveInstanceState(outState)
    }

    override fun showErrorPopup(
        errorType: BaseMvpContract.ErrorType,
        flags: Int,
        message: String?,
        error: Throwable,
        vararg args: Any
    ) {
        DefaultErrorHandler.showError(this, message, error, args)
    }

    protected abstract fun createPresenterStateHolder(): PresenterStateHolder<S>
}