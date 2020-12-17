package com.lex.simplequest.presentation.screen.home

import com.lex.simplequest.presentation.base.Router

interface MainRouter : Router {
    fun showHome()
    fun showMap()
    fun showTracks()
    fun showSettings()
    fun showTrackView(trackId: Long, switchFromTrackDetails: Boolean)
    fun showTrackDetails(trackId: Long, switchFromTrackView: Boolean)
}