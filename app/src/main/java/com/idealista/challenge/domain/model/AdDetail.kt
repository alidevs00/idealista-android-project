package com.idealista.challenge.domain.model

import java.time.Instant

/**
 * Full detail of a single ad.
 *
 * [propertyCode] is taken from the request the user made (i.e. the code tapped
 * on the listing screen), not from the detail payload itself: the challenge's
 * detail endpoint always returns the same fixed response regardless of which ad
 * was requested (documented in CLAUDE.md and the README), so trusting an id
 * echoed back by that response would silently break favorite-matching between
 * the list and detail screens. The repository is responsible for this
 * substitution — see [com.idealista.challenge.data.repository.AdsRepositoryImpl].
 */
data class AdDetail(
    val propertyCode: String,
    val price: Price,
    val operation: Operation,
    val propertyType: String,
    val description: String,
    val images: List<AdImage>,
    val characteristics: AdCharacteristics,
    val location: Location,
    val favoritedAt: Instant?,
) {
    val isFavorite: Boolean get() = favoritedAt != null
}
