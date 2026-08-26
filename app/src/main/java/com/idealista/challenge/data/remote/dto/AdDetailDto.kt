package com.idealista.challenge.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * GET detail.json response. Only the fields this app uses are declared.
 *
 * Note there is no address/municipality/district here at all (only
 * [UbicationDto] coordinates) — see the KDoc on the domain `Location` and
 * `AdDetail` models for how the repository fills that gap.
 */
@Serializable
data class AdDetailDto(
    val priceInfo: PriceAmountDto,
    val operation: String,
    val extendedPropertyType: String,
    val propertyComment: String,
    val multimedia: MultimediaDto,
    val ubication: UbicationDto,
    val moreCharacteristics: MoreCharacteristicsDto,
)

@Serializable
data class UbicationDto(
    val latitude: Double,
    val longitude: Double,
)

@Serializable
data class MoreCharacteristicsDto(
    val communityCosts: Double? = null,
    val roomNumber: Int,
    val bathNumber: Int,
    val exterior: Boolean,
    val energyCertificationType: String? = null,
    val constructedArea: Int? = null,
    val lift: Boolean? = null,
    val boxroom: Boolean? = null,
    val floor: String? = null,
)
