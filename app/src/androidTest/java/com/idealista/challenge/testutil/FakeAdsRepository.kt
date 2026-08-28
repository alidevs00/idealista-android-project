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
 * In-memory [AdsRepository] fake, bound over the real implementation in
 * instrumented UI tests via `@BindValue` + `@UninstallModules(RepositoryModule::class)`.
 *
 * Deliberately a near-identical copy of the JVM-test fake in
 * app/src/test/.../testutil - the `test` and `androidTest` source sets don't
 * share code without extra Gradle wiring (a third "sharedTest" source set),
 * which isn't worth the added build complexity for a fake this small.
 */
class FakeAdsRepository(
    private val baseAds: List<Ad> = emptyList(),
    private val baseDetail: AdDetail? = null,
) : AdsRepository {

    private val favoritedAt = MutableStateFlow<Map<String, Instant>>(emptyMap())

    override fun observeAds(): Flow<List<Ad>> =
        favoritedAt.combine(MutableStateFlow(baseAds)) { favorites, ads ->
            ads.map { it.copy(favoritedAt = favorites[it.propertyCode]) }
        }

    override suspend fun refreshAds() = Unit

    override fun observeAdDetail(propertyCode: String): Flow<AdDetail> =
        favoritedAt.combine(MutableStateFlow(baseDetail)) { favorites, detail ->
            detail?.copy(favoritedAt = favorites[propertyCode])
        }.filterNotNull()

    override suspend fun refreshAdDetail(propertyCode: String) = Unit

    override suspend fun toggleFavorite(propertyCode: String) {
        favoritedAt.value = favoritedAt.value.toMutableMap().apply {
            if (containsKey(propertyCode)) remove(propertyCode) else put(propertyCode, Instant.now())
        }
    }
}
