package com.idealista.challenge.presentation.common

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Formats a favorite's timestamp as e.g. "12 Mar 2026". Locale is pinned to
 * English regardless of the device's locale, since the rest of the app's UI
 * strings are English-only - a locale-dependent month name here would mix
 * languages within the same "Favorited on ..." sentence.
 */
object FavoriteDateFormatter {

    private val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)

    fun format(instant: Instant): String = formatter.format(instant.atZone(ZoneId.systemDefault()))
}
