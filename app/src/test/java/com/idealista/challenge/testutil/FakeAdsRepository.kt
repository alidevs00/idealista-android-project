package com.idealista.challenge.testutil

import com.idealista.challenge.domain.model.Ad
import com.idealista.challenge.domain.model.AdDetail
import com.idealista.challenge.domain.repository.AdsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import java.time.Instant

/**
 * In-memory [AdsRepository] fake for ViewModel tests - no network/DB involved.
 * Ads/detail are supplied with `favoritedAt = null`; favorite state is tracked
 * separately here and combined in, the same shape as the real implementation.
 */
class FakeAdsRepository(
    private val baseAds: List<Ad> = emptyList(),
    private val baseDetail: AdDetail? = null,
) : AdsRepository {

    private val favoritedAt = MutableStateFlow<Map<String, Instant>>(emptyMap())

    var refreshAdsFailure: Throwable? = null
    var refreshAdDetailFailure: Throwable? = null
    var refreshAdsCallCount = 0
        private set
    var refreshAdDetailCallCount = 0
        private set

    override fun observeAds(): Flow<List<Ad>> =
        favoritedAt.combine(MutableStateFlow(baseAds)) { favorites, ads ->
            ads.map { it.copy(favoritedAt = favorites[it.propertyCode]) }
        }

    override suspend fun refreshAds() {
        refreshAdsCallCount++
        refreshAdsFailure?.let { throw it }
    }

    override fun observeAdDetail(propertyCode: String): Flow<AdDetail> =
        favoritedAt.combine(MutableStateFlow(baseDetail)) { favorites, detail ->
            detail?.copy(favoritedAt = favorites[propertyCode])
        }.filterNotNull()

    override suspend fun refreshAdDetail(propertyCode: String) {
        refreshAdDetailCallCount++
        refreshAdDetailFailure?.let { throw it }
    }

    override suspend fun toggleFavorite(propertyCode: String) {
        favoritedAt.value = favoritedAt.value.toMutableMap().apply {
            if (containsKey(propertyCode)) remove(propertyCode) else put(propertyCode, Instant.now())
        }
    }
}
