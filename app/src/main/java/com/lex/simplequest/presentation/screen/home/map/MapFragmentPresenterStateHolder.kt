package com.lex.simplequest.presentation.screen.home.map

import android.os.Bundle
import com.lex.simplequest.domain.locationmanager.model.Location
import com.lex.simplequest.presentation.screen.model.toDomainModel
import com.lex.simplequest.presentation.screen.model.toUiModel
import com.softeq.android.mvp.PresenterStateHolder

class MapFragmentPresenterStateHolder : PresenterStateHolder<MapFragmentContract.Presenter.State> {
    companion object {
        private const val LOCATION = "location"
        private const val LOCATIONS_RECEIVED_COUNT = "locationsReceivedCount"
    }

    override fun create(): MapFragmentContract.Presenter.State? =
        State()

    override fun save(state: MapFragmentContract.Presenter.State, bundle: Bundle) {
        state.location?.let {
            bundle.putParcelable(LOCATION, it.toUiModel())
        }
        bundle.putInt(LOCATIONS_RECEIVED_COUNT, state.locationsReceivedCount)
    }

    override fun restore(bundle: Bundle?): MapFragmentContract.Presenter.State? =
        bundle?.let {
            State().apply {
                location = it.getParcelable<com.lex.simplequest.presentation.screen.model.Location>(LOCATION)!!.toDomainModel()
                locationsReceivedCount = it.getInt(LOCATIONS_RECEIVED_COUNT, 0)
            }
        }

    class State: MapFragmentContract.Presenter.State {
        override var location: Location? = null
        override var locationsReceivedCount: Int = 0
    }
}