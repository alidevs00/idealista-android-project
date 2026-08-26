package com.idealista.challenge.data.remote.dto

import kotlinx.serialization.Serializable

/** One entry of the GET list.json response. Only the fields this app uses are declared. */
@Serializable
data class AdListItemDto(
    val propertyCode: String,
    val thumbnail: String,
    val floor: String? = null,
    val priceInfo: ListPriceInfoDto,
    val propertyType: String,
    val operation: String,
    val size: Double,
    val exterior: Boolean,
    val rooms: Int,
    val bathrooms: Int,
    val address: String,
    val municipality: String,
    val district: String,
    val neighborhood: String? = null,
    val latitude: Double,
    val longitude: Double,
    val description: String,
)
