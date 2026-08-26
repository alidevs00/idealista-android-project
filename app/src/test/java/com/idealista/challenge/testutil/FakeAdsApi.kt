package com.idealista.challenge.testutil

import com.idealista.challenge.data.remote.AdsApi
import com.idealista.challenge.data.remote.dto.AdDetailDto
import com.idealista.challenge.data.remote.dto.AdListItemDto

class FakeAdsApi(
    private var ads: List<AdListItemDto> = listOf(Fixtures.adListItemDto()),
    private var detail: AdDetailDto = Fixtures.adDetailDto(),
    private var failure: Throwable? = null,
) : AdsApi {

    fun setAds(value: List<AdListItemDto>) {
        ads = value
    }

    fun setDetail(value: AdDetailDto) {
        detail = value
    }

    fun setFailure(value: Throwable?) {
        failure = value
    }

    override suspend fun getAds(): List<AdListItemDto> {
        failure?.let { throw it }
        return ads
    }

    override suspend fun getAdDetail(): AdDetailDto {
        failure?.let { throw it }
        return detail
    }
}
