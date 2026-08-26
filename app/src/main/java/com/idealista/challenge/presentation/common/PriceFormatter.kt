package com.idealista.challenge.presentation.common

import com.idealista.challenge.domain.model.Price
import java.text.NumberFormat
import java.util.Locale

/** Formats [Price] as e.g. "1.195.000 €" using the amount's own currency suffix. */
object PriceFormatter {

    private val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale("es", "ES")).apply {
        maximumFractionDigits = 0
    }

    fun format(price: Price): String = "${numberFormat.format(price.amount)} ${price.currencySuffix}"
}
