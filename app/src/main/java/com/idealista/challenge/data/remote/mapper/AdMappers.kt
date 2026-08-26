package com.idealista.challenge.data.remote.mapper

import com.idealista.challenge.data.remote.dto.AdDetailDto
import com.idealista.challenge.data.remote.dto.AdListItemDto
import com.idealista.challenge.domain.model.Ad
import com.idealista.challenge.domain.model.AdCharacteristics
import com.idealista.challenge.domain.model.AdDetail
import com.idealista.challenge.domain.model.AdImage
import com.idealista.challenge.domain.model.Location
import com.idealista.challenge.domain.model.Operation
import com.idealista.challenge.domain.model.Price
import java.time.Instant

fun AdListItemDto.toDomain(favoritedAt: Instant?): Ad = Ad(
    propertyCode = propertyCode,
    thumbnailUrl = thumbnail,
    price = Price(priceInfo.price.amount, priceInfo.price.currencySuffix),
    operation = operation.toOperation(),
    propertyType = propertyType,
    size = size,
    rooms = rooms,
    bathrooms = bathrooms,
    floor = floor,
    exterior = exterior,
    location = Location(
        address = address,
        municipality = municipality,
        district = district,
        neighborhood = neighborhood,
        latitude = latitude,
        longitude = longitude,
    ),
    description = description,
    favoritedAt = favoritedAt,
)

/**
 * Maps the (always identical) detail response into a domain [AdDetail] for the
 * ad the caller actually asked for.
 *
 * @param propertyCode the code the caller requested — used as-is instead of
 *   anything in [this] response, see the KDoc on [AdDetail].
 * @param fallbackLocation address-like fields to use for [Location], since the
 *   detail payload only carries coordinates; normally the matching [Ad] from
 *   the already-loaded list.
 */
fun AdDetailDto.toDomain(
    propertyCode: String,
    favoritedAt: Instant?,
    fallbackLocation: Location?,
): AdDetail = AdDetail(
    propertyCode = propertyCode,
    price = Price(priceInfo.amount, priceInfo.currencySuffix),
    operation = operation.toOperation(),
    propertyType = extendedPropertyType,
    description = propertyComment,
    images = multimedia.images.map { AdImage(url = it.url, tag = it.tag) },
    characteristics = AdCharacteristics(
        constructedArea = moreCharacteristics.constructedArea,
        rooms = moreCharacteristics.roomNumber,
        bathrooms = moreCharacteristics.bathNumber,
        floor = moreCharacteristics.floor,
        exterior = moreCharacteristics.exterior,
        hasLift = moreCharacteristics.lift,
        hasBoxroom = moreCharacteristics.boxroom,
        communityCosts = moreCharacteristics.communityCosts,
        energyCertification = moreCharacteristics.energyCertificationType,
    ),
    location = fallbackLocation?.copy(latitude = ubication.latitude, longitude = ubication.longitude)
        ?: Location(
            address = null,
            municipality = null,
            district = null,
            neighborhood = null,
            latitude = ubication.latitude,
            longitude = ubication.longitude,
        ),
    favoritedAt = favoritedAt,
)

private fun String.toOperation(): Operation = when (this.lowercase()) {
    "sale" -> Operation.SALE
    "rent" -> Operation.RENT
    else -> Operation.UNKNOWN
}
