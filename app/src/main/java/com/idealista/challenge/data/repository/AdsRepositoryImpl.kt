package com.idealista.challenge.data.repository

import com.idealista.challenge.data.local.dao.FavoriteDao
import com.idealista.challenge.data.local.entity.FavoriteEntity
import com.idealista.challenge.data.remote.AdsApi
import com.idealista.challenge.data.remote.dto.AdDetailDto
import com.idealista.challenge.data.remote.dto.AdListItemDto
import com.idealista.challenge.data.remote.mapper.toDomain
import com.idealista.challenge.domain.model.Ad
import com.idealista.challenge.domain.model.AdDetail
import com.idealista.challenge.domain.repository.AdsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Combines [AdsApi] (remote) with [FavoriteDao] (local) into the single stream
 * of decorated [Ad]/[AdDetail] the domain layer works with.
 *
 * Fetched network responses are cached in memory ([adsCache], [detailCache]) so
 * that toggling a favorite - a purely local write - re-emits instantly through
 * [observeAds]/[observeAdDetail] without needing another network round trip.
 */
@Singleton
class AdsRepositoryImpl @Inject constructor(
    private val api: AdsApi,
    private val favoriteDao: FavoriteDao,
) : AdsRepository {

    private val adsCache = MutableStateFlow<List<AdListItemDto>>(emptyList())

    // Keyed by the propertyCode the caller asked for, not anything in the
    // response itself - see AdDetail's KDoc on why that id can't be trusted.
    private val detailCache = MutableStateFlow<Map<String, AdDetailDto>>(emptyMap())

    override fun observeAds(): Flow<List<Ad>> =
        combine(adsCache, favoriteDao.observeAll()) { ads, favorites ->
            val favoritedAtByCode = favorites.associate { it.propertyCode to it.favoritedAtInstant() }
            ads.map { dto -> dto.toDomain(favoritedAt = favoritedAtByCode[dto.propertyCode]) }
        }

    override suspend fun refreshAds() {
        adsCache.value = api.getAds()
    }

    override fun observeAdDetail(propertyCode: String): Flow<AdDetail> =
        combine(detailCache, favoriteDao.observeOne(propertyCode)) { cache, favorite ->
            val dto = cache[propertyCode] ?: return@combine null
            val fallbackLocation = adsCache.value
                .firstOrNull { it.propertyCode == propertyCode }
                ?.toDomain(favoritedAt = null)
                ?.location
            dto.toDomain(
                propertyCode = propertyCode,
                favoritedAt = favorite?.favoritedAtInstant(),
                fallbackLocation = fallbackLocation,
            )
        }.filterNotNull()

    override suspend fun refreshAdDetail(propertyCode: String) {
        val dto = api.getAdDetail()
        detailCache.update { it + (propertyCode to dto) }
    }

    override suspend fun toggleFavorite(propertyCode: String) {
        favoriteDao.toggle(propertyCode, Instant.now().toEpochMilli())
    }

    private fun FavoriteEntity.favoritedAtInstant(): Instant = Instant.ofEpochMilli(favoritedAtEpochMillis)
}
