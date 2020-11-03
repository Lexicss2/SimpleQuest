package com.lex.simplequest.presentation.screen.home.home

import android.os.Bundle
import com.softeq.android.mvp.PresenterStateHolder

class HomeFragmentPresenterStateHolder :
    PresenterStateHolder<HomeFragmentContract.Presenter.State> {

    companion object {
        private const val ERROR = "error"
        private const val IS_LOCATION_AVAILABLE = "isLocationAvailable"
        private const val LOCATION_SUSPENDED_REASON = "locationSuspendedReason"
    }

    override fun create(): HomeFragmentContract.Presenter.State? =
        State()

    override fun save(state: HomeFragmentContract.Presenter.State, bundle: Bundle) {
        state.error?.let { bundle.putSerializable(ERROR, it) }
        state.isLocationAvailable?.let {
            bundle.putBoolean(IS_LOCATION_AVAILABLE, it)
        } ?: bundle.remove(IS_LOCATION_AVAILABLE)
        state.locationSuspendedReason?.let {
            bundle.putInt(LOCATION_SUSPENDED_REASON, it)
        } ?: bundle.remove(LOCATION_SUSPENDED_REASON)
    }

    override fun restore(bundle: Bundle?): HomeFragmentContract.Presenter.State? =
        bundle?.let {
            State().apply {
                error = it.getSerializable(ERROR) as? Throwable
                isLocationAvailable = if (it.containsKey(IS_LOCATION_AVAILABLE)) {
                    it.getBoolean(IS_LOCATION_AVAILABLE)
                } else null
                locationSuspendedReason = if (it.containsKey(LOCATION_SUSPENDED_REASON)) {
                    it.getInt(LOCATION_SUSPENDED_REASON)
                } else null
            }
        }

    class State : HomeFragmentContract.Presenter.State {
        override var error: Throwable? = null
        override var isLocationAvailable: Boolean? = null
        override var locationSuspendedReason: Int? = null
    }
}