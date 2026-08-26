package com.idealista.challenge.presentation.common

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formats a favorite's timestamp as e.g. "12 mar 2026" for display. */
object FavoriteDateFormatter {

    private val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "ES"))

    fun format(instant: Instant): String = formatter.format(instant.atZone(ZoneId.systemDefault()))
}
