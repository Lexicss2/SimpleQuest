package com.lex.simplequest.presentation.screen.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

typealias DomainLocation = com.lex.simplequest.domain.locationmanager.model.Location
typealias UiLocation = Location

@Parcelize
data class Location(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?
) : Parcelable

fun DomainLocation.toUiModel(): UiLocation =
    UiLocation(latitude, longitude, altitude)

fun UiLocation.toDomainModel(): DomainLocation =
    DomainLocation(latitude, longitude, altitude)