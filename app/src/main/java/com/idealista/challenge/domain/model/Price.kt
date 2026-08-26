package com.idealista.challenge.domain.model

/** An amount together with the currency suffix the API already localizes it with (e.g. "€", "€/mes"). */
data class Price(
    val amount: Double,
    val currencySuffix: String,
)
