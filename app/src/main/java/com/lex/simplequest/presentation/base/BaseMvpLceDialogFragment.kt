package com.lex.simplequest.presentation.base

import android.os.Bundle
import android.view.View
import com.lex.simplequest.R
import com.lex.simplequest.presentation.utils.bind
import com.lex.simplequest.presentation.utils.isInternetConnectivityError

abstract class BaseMvpLceDialogFragment<U : BaseMvpLceContract.Ui, S : BaseMvpLceContract.Presenter.State, P : BaseMvpLceContract.Presenter<U, S>> :
    BaseMvpDialogFragment<U, S, P>(), BaseMvpLceContract.Ui {

    private var layoutContent: View? = null
    private var layoutLoading: View? = null
    private var layoutErrorInternet: View? = null
    private var layoutErrorLoadingData: View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        layoutContent = view.bind(R.id.layout_content)
        layoutLoading = view.bind(R.id.layout_loading)
        layoutErrorInternet = view.bind(R.id.layout_error_no_internet)
        layoutErrorLoadingData = view.bind(R.id.layout_error_loading_data)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        layoutContent = null
        layoutLoading = null
        layoutErrorInternet = null
        layoutErrorLoadingData = null
    }

    override fun showLoading() {
        layoutContent!!.visibility = View.GONE
        layoutLoading!!.visibility = View.VISIBLE
        layoutErrorInternet!!.visibility = View.GONE
        layoutErrorLoadingData!!.visibility = View.GONE
    }

    override fun showContent() {
        layoutContent!!.visibility = View.VISIBLE
        layoutLoading!!.visibility = View.GONE
        layoutErrorInternet!!.visibility = View.GONE
        layoutErrorLoadingData!!.visibility = View.GONE
    }

    override fun showError(error: Throwable) {
        layoutContent!!.visibility = View.GONE
        layoutLoading!!.visibility = View.GONE
        if (error.isInternetConnectivityError()) {
            layoutErrorInternet!!.visibility = View.VISIBLE
            layoutErrorLoadingData!!.visibility = View.GONE
        } else {
            layoutErrorInternet!!.visibility = View.GONE
            layoutErrorLoadingData!!.visibility = View.VISIBLE
        }
    }

    protected fun isContentVisible(): Boolean =
        View.VISIBLE == layoutContent!!.visibility

    protected fun isLoadingVisible(): Boolean =
        View.VISIBLE == layoutLoading!!.visibility

    protected fun isErrorVisible(): Boolean =
        View.VISIBLE == layoutErrorInternet!!.visibility ||
                View.VISIBLE == layoutErrorLoadingData!!.visibility
}