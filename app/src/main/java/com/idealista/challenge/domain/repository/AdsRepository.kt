package com.idealista.challenge.domain.repository

import com.idealista.challenge.domain.model.Ad
import com.idealista.challenge.domain.model.AdDetail
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for ads and their local favorite state.
 *
 * Implemented in the data layer by combining the remote listing/detail
 * endpoints with the local favorites store, so callers never have to join the
 * two themselves.
 */
interface AdsRepository {

    /**
     * Ads currently cached, decorated with favorite state, re-emitting whenever
     * that favorite state changes locally. Does not by itself trigger a network
     * call — call [refreshAds] to (re)populate it.
     */
    fun observeAds(): Flow<List<Ad>>

    /** Fetches the ad list from the network and updates the local cache. */
    suspend fun refreshAds()

    /**
     * The detail for a single ad, decorated with favorite state, re-emitting
     * whenever that favorite state changes locally.
     */
    fun observeAdDetail(propertyCode: String): Flow<AdDetail>

    /** Fetches the given ad's detail from the network and updates the local cache. */
    suspend fun refreshAdDetail(propertyCode: String)

    /** Favorites [propertyCode] if it isn't already, or un-favorites it if it is. */
    suspend fun toggleFavorite(propertyCode: String)
}
