package com.idealista.challenge.domain.model

import java.time.Instant

/**
 * A single ad as shown in the listing screen.
 *
 * [favoritedAt] is populated from local storage and is `null` when the ad is not
 * favorited. It lives on the model itself (rather than as a separate lookup the
 * UI has to join) so both the list and detail screens always read one flat,
 * ready-to-render shape.
 */
data class Ad(
    val propertyCode: String,
    val thumbnailUrl: String,
    val price: Price,
    val operation: Operation,
    val propertyType: String,
    val size: Double,
    val rooms: Int,
    val bathrooms: Int,
    val floor: String?,
    val exterior: Boolean,
    val location: Location,
    val description: String,
    val favoritedAt: Instant?,
) {
    val isFavorite: Boolean get() = favoritedAt != null
}
