package com.idealista.challenge.testutil

import com.idealista.challenge.data.remote.dto.AdDetailDto
import com.idealista.challenge.data.remote.dto.AdListItemDto
import com.idealista.challenge.data.remote.dto.ImageDto
import com.idealista.challenge.data.remote.dto.ListPriceInfoDto
import com.idealista.challenge.data.remote.dto.MoreCharacteristicsDto
import com.idealista.challenge.data.remote.dto.MultimediaDto
import com.idealista.challenge.data.remote.dto.PriceAmountDto
import com.idealista.challenge.data.remote.dto.UbicationDto

/** Small builders for the fixtures shared across data/domain tests, mirroring the real challenge JSON shapes. */
object Fixtures {

    fun adListItemDto(
        propertyCode: String = "1",
        amount: Double = 1_195_000.0,
        currencySuffix: String = "€",
        operation: String = "sale",
        address: String = "calle de Lagasca",
        municipality: String = "Madrid",
        district: String = "Barrio de Salamanca",
    ) = AdListItemDto(
        propertyCode = propertyCode,
        thumbnail = "https://example.com/thumb.webp",
        floor = "2",
        priceInfo = ListPriceInfoDto(PriceAmountDto(amount, currencySuffix)),
        propertyType = "flat",
        operation = operation,
        size = 133.0,
        exterior = false,
        rooms = 3,
        bathrooms = 2,
        address = address,
        municipality = municipality,
        district = district,
        neighborhood = "Castellana",
        latitude = 40.4362687,
        longitude = -3.6833686,
        description = "A lovely flat.",
    )

    fun adDetailDto(
        amount: Double = 1_195_000.0,
        currencySuffix: String = "€",
        operation: String = "sale",
    ) = AdDetailDto(
        priceInfo = PriceAmountDto(amount, currencySuffix),
        operation = operation,
        extendedPropertyType = "flat",
        propertyComment = "A lovely flat, in detail.",
        multimedia = MultimediaDto(images = listOf(ImageDto(url = "https://example.com/1.webp", tag = "livingRoom"))),
        ubication = UbicationDto(latitude = 40.4362687, longitude = -3.6833686),
        moreCharacteristics = MoreCharacteristicsDto(
            communityCosts = 330.0,
            roomNumber = 3,
            bathNumber = 2,
            exterior = false,
            energyCertificationType = "e",
            constructedArea = 133,
            lift = true,
            boxroom = false,
            floor = "2",
        ),
    )
}
