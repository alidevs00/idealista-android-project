package com.idealista.challenge.data.remote

import com.idealista.challenge.data.remote.dto.AdDetailDto
import com.idealista.challenge.data.remote.dto.AdListItemDto
import retrofit2.http.GET

/**
 * Static-JSON "API" the challenge provides. Neither endpoint takes a request
 * parameter — [getAdDetail] always returns the same fixed payload, by design
 * of the challenge, regardless of which ad the caller is interested in.
 */
interface AdsApi {

    @GET("list.json")
    suspend fun getAds(): List<AdListItemDto>

    @GET("detail.json")
    suspend fun getAdDetail(): AdDetailDto
}
