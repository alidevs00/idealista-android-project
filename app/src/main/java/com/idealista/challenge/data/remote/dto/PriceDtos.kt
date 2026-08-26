package com.idealista.challenge.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PriceAmountDto(
    val amount: Double,
    val currencySuffix: String,
)

/** Shape used by list.json: the amount is nested one level deeper than in detail.json. */
@Serializable
data class ListPriceInfoDto(
    val price: PriceAmountDto,
)
