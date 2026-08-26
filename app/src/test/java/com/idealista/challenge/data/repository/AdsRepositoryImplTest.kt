package com.idealista.challenge.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.idealista.challenge.testutil.FakeAdsApi
import com.idealista.challenge.testutil.FakeFavoriteDao
import com.idealista.challenge.testutil.Fixtures
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

class AdsRepositoryImplTest {

    private lateinit var api: FakeAdsApi
    private lateinit var favoriteDao: FakeFavoriteDao
    private lateinit var repository: AdsRepositoryImpl

    @Before
    fun setUp() {
        api = FakeAdsApi()
        favoriteDao = FakeFavoriteDao()
        repository = AdsRepositoryImpl(api, favoriteDao)
    }

    @Test
    fun `observeAds is empty until refreshAds populates the cache`() = runTest {
        repository.observeAds().test {
            assertThat(awaitItem()).isEmpty()

            repository.refreshAds()

            val ads = awaitItem()
            assertThat(ads).hasSize(1)
            assertThat(ads.first().propertyCode).isEqualTo("1")
            assertThat(ads.first().isFavorite).isFalse()
        }
    }

    @Test
    fun `toggling a favorite re-emits observeAds with the updated state`() = runTest {
        repository.refreshAds()

        repository.observeAds().test {
            assertThat(awaitItem().first().isFavorite).isFalse()

            repository.toggleFavorite("1")
            assertThat(awaitItem().first().isFavorite).isTrue()

            repository.toggleFavorite("1")
            assertThat(awaitItem().first().isFavorite).isFalse()
        }
    }

    @Test
    fun `refreshAds propagates network failures`() = runTest {
        api.setFailure(IOException("no network"))

        try {
            repository.refreshAds()
            error("Expected an IOException")
        } catch (e: IOException) {
            assertThat(e).hasMessageThat().isEqualTo("no network")
        }
    }

    @Test
    fun `observeAdDetail only emits after refreshAdDetail and uses the requested propertyCode`() = runTest {
        repository.observeAdDetail("7").test {
            expectNoEvents()

            repository.refreshAdDetail("7")

            val detail = awaitItem()
            assertThat(detail.propertyCode).isEqualTo("7")
        }
    }

    @Test
    fun `observeAdDetail backfills location from a matching cached list item`() = runTest {
        api.setAds(listOf(Fixtures.adListItemDto(propertyCode = "1", address = "calle Real")))
        repository.refreshAds()
        repository.refreshAdDetail("1")

        repository.observeAdDetail("1").test {
            assertThat(awaitItem().location.address).isEqualTo("calle Real")
        }
    }

    @Test
    fun `toggling a favorite re-emits observeAdDetail with the updated state`() = runTest {
        repository.refreshAdDetail("1")

        repository.observeAdDetail("1").test {
            assertThat(awaitItem().isFavorite).isFalse()

            repository.toggleFavorite("1")

            assertThat(awaitItem().isFavorite).isTrue()
        }
    }
}
