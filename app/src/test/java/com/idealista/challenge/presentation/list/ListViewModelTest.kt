package com.idealista.challenge.presentation.list

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.idealista.challenge.domain.model.Ad
import com.idealista.challenge.domain.model.Location
import com.idealista.challenge.domain.model.Operation
import com.idealista.challenge.domain.model.Price
import com.idealista.challenge.domain.usecase.ObserveAdsUseCase
import com.idealista.challenge.domain.usecase.RefreshAdsUseCase
import com.idealista.challenge.domain.usecase.ToggleFavoriteUseCase
import com.idealista.challenge.presentation.common.FavoriteToggleEvent
import com.idealista.challenge.testutil.FakeAdsRepository
import com.idealista.challenge.testutil.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException

/**
 * [MainDispatcherRule] uses an unconfined dispatcher, so `ListViewModel.init`
 * (including its `refresh()` call) runs to completion synchronously as part of
 * constructing the ViewModel - by the time a test subscribes to [ListViewModel.uiState]
 * below, that first load has already settled. These tests assert on that
 * settled state and on call counts rather than chasing the transient
 * `isLoading = true` frame, which a fake resolving instantly makes effectively
 * unobservable (the same as it would be against a very fast real backend).
 */
class ListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sampleAd = Ad(
        propertyCode = "1",
        thumbnailUrl = "https://example.com/thumb.webp",
        price = Price(1_195_000.0, "€"),
        operation = Operation.SALE,
        propertyType = "flat",
        size = 133.0,
        rooms = 3,
        bathrooms = 2,
        floor = "2",
        exterior = false,
        location = Location("calle de Lagasca", "Madrid", "Barrio de Salamanca", "Castellana", 40.43, -3.68),
        description = "A lovely flat.",
        favoritedAt = null,
    )

    private fun viewModel(repository: FakeAdsRepository) = ListViewModel(
        observeAds = ObserveAdsUseCase(repository),
        refreshAds = RefreshAdsUseCase(repository),
        toggleFavorite = ToggleFavoriteUseCase(repository),
    )

    @Test
    fun `refreshes on init and exposes the loaded ads`() = runTest {
        val repository = FakeAdsRepository(baseAds = listOf(sampleAd))

        val state = viewModel(repository).uiState.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.ads).containsExactly(sampleAd)
        assertThat(repository.refreshAdsCallCount).isEqualTo(1)
    }

    @Test
    fun `a refresh failure surfaces the error and stops loading`() = runTest {
        val repository = FakeAdsRepository().apply { refreshAdsFailure = IOException("offline") }

        val state = viewModel(repository).uiState.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isInstanceOf(IOException::class.java)
        assertThat(state.isEmpty).isFalse() // there's an error, not legitimately empty
    }

    @Test
    fun `an empty successful load is reported as isEmpty`() = runTest {
        val repository = FakeAdsRepository(baseAds = emptyList())

        assertThat(viewModel(repository).uiState.value.isEmpty).isTrue()
    }

    @Test
    fun `favorite click toggles the ad's favorite state reactively`() = runTest {
        val repository = FakeAdsRepository(baseAds = listOf(sampleAd))
        val viewModel = viewModel(repository)
        assertThat(viewModel.uiState.value.ads.first().isFavorite).isFalse()

        viewModel.uiState.test {
            viewModel.onFavoriteClick("1")

            assertThat(awaitItem().ads.first().isFavorite).isTrue()
        }
    }

    @Test
    fun `favorite click emits an ADDED then REMOVED toggle event`() = runTest {
        val repository = FakeAdsRepository(baseAds = listOf(sampleAd))
        val viewModel = viewModel(repository)

        viewModel.favoriteToggleEvents.test {
            viewModel.onFavoriteClick("1")
            assertThat(awaitItem()).isEqualTo(FavoriteToggleEvent.ADDED)

            viewModel.onFavoriteClick("1")
            assertThat(awaitItem()).isEqualTo(FavoriteToggleEvent.REMOVED)
        }
    }

    @Test
    fun `manual refresh() clears a previous error`() = runTest {
        val repository = FakeAdsRepository(baseAds = listOf(sampleAd)).apply {
            refreshAdsFailure = IOException("offline")
        }
        val viewModel = viewModel(repository)
        assertThat(viewModel.uiState.value.error).isNotNull()

        repository.refreshAdsFailure = null
        viewModel.refresh()

        val recovered = viewModel.uiState.value
        assertThat(recovered.isLoading).isFalse()
        assertThat(recovered.error).isNull()
        assertThat(recovered.ads).containsExactly(sampleAd)
    }
}
