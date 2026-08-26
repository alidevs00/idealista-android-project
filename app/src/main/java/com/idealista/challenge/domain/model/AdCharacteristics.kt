package com.idealista.challenge.domain.model

/** Extended attributes only the detail endpoint exposes. */
data class AdCharacteristics(
    val constructedArea: Int?,
    val rooms: Int,
    val bathrooms: Int,
    val floor: String?,
    val exterior: Boolean,
    val hasLift: Boolean?,
    val hasBoxroom: Boolean?,
    val communityCosts: Double?,
    val energyCertification: String?,
)
