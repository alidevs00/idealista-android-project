package com.idealista.challenge.domain.model

/**
 * Where an ad is located.
 *
 * The address-like fields are nullable because the detail endpoint's payload
 * does not include them at all (only coordinates) — see [AdDetail]. When a
 * detail is opened from the listing screen the repository backfills these from
 * the already-known list item; a detail reached with no such match on hand
 * (e.g. a future deep link) would legitimately have them blank.
 */
data class Location(
    val address: String?,
    val municipality: String?,
    val district: String?,
    val neighborhood: String?,
    val latitude: Double,
    val longitude: Double,
)
