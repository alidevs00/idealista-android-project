package com.idealista.challenge.presentation.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.idealista.challenge.domain.model.AdCharacteristics
import com.idealista.challenge.domain.model.AdDetail
import com.idealista.challenge.domain.model.Location
import com.idealista.challenge.domain.model.Operation
import com.idealista.challenge.domain.model.Price
import com.idealista.challenge.domain.usecase.ObserveAdDetailUseCase
import com.idealista.challenge.domain.usecase.RefreshAdDetailUseCase
import com.idealista.challenge.domain.usecase.ToggleFavoriteUseCase
import com.idealista.challenge.presentation.common.FavoriteToggleEvent
import com.idealista.challenge.presentation.common.NavArgs
import com.idealista.challenge.testutil.FakeAdsRepository
import com.idealista.challenge.testutil.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException

/**
 * Same reasoning as [com.idealista.challenge.presentation.list.ListViewModelTest]:
 * with the unconfined [MainDispatcherRule], `DetailViewModel.init` (and its
 * `refresh()` call) has already settled by the time these tests inspect
 * [DetailViewModel.uiState], so assertions target the settled state rather
 * than the transient `isLoading = true` frame.
 */
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val propertyCode = "1"

    private val baseDetail = AdDetail(
        propertyCode = propertyCode,
        price = Price(1_195_000.0, "€"),
        operation = Operation.SALE,
        propertyType = "flat",
        description = "A lovely flat.",
        images = emptyList(),
        characteristics = AdCharacteristics(
            constructedArea = 133,
            rooms = 3,
            bathrooms = 2,
            floor = "2",
            exterior = false,
            hasLift = true,
            hasBoxroom = false,
            communityCosts = null,
            energyCertification = "B",
        ),
        location = Location("calle de Lagasca", "Madrid", "Barrio de Salamanca", "Castellana", 40.43, -3.68),
        favoritedAt = null,
    )

    private fun viewModel(repository: FakeAdsRepository) = DetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf(NavArgs.PROPERTY_CODE to propertyCode)),
        observeAdDetail = ObserveAdDetailUseCase(repository),
        refreshAdDetail = RefreshAdDetailUseCase(repository),
        toggleFavorite = ToggleFavoriteUseCase(repository),
    )

    @Test
    fun `refreshes on init and exposes the loaded detail`() = runTest {
        val repository = FakeAdsRepository(baseDetail = baseDetail)

        val state = viewModel(repository).uiState.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.adDetail).isEqualTo(baseDetail)
        assertThat(repository.refreshAdDetailCallCount).isEqualTo(1)
    }

    @Test
    fun `a refresh failure surfaces the error and stops loading`() = runTest {
        val repository = FakeAdsRepository(baseDetail = baseDetail).apply {
            refreshAdDetailFailure = IOException("offline")
        }

        val state = viewModel(repository).uiState.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isInstanceOf(IOException::class.java)
    }

    @Test
    fun `favorite click toggles the detail's favorite state reactively`() = runTest {
        val repository = FakeAdsRepository(baseDetail = baseDetail)
        val viewModel = viewModel(repository)
        assertThat(viewModel.uiState.value.adDetail?.isFavorite).isFalse()

        viewModel.uiState.test {
            awaitItem() // current value at subscription time, not yet toggled
            viewModel.onFavoriteClick()

            assertThat(awaitItem().adDetail?.isFavorite).isTrue()
        }
    }

    @Test
    fun `favorite click emits an ADDED then REMOVED toggle event`() = runTest {
        val repository = FakeAdsRepository(baseDetail = baseDetail)
        val viewModel = viewModel(repository)

        viewModel.favoriteToggleEvents.test {
            viewModel.onFavoriteClick()
            assertThat(awaitItem()).isEqualTo(FavoriteToggleEvent.ADDED)

            viewModel.onFavoriteClick()
            assertThat(awaitItem()).isEqualTo(FavoriteToggleEvent.REMOVED)
        }
    }

    @Test
    fun `manual refresh() clears a previous error`() = runTest {
        val repository = FakeAdsRepository(baseDetail = baseDetail).apply {
            refreshAdDetailFailure = IOException("offline")
        }
        val viewModel = viewModel(repository)
        assertThat(viewModel.uiState.value.error).isNotNull()

        repository.refreshAdDetailFailure = null
        viewModel.refresh()

        val recovered = viewModel.uiState.value
        assertThat(recovered.isLoading).isFalse()
        assertThat(recovered.error).isNull()
        assertThat(recovered.adDetail).isEqualTo(baseDetail)
    }

    @Test
    fun `missing propertyCode navigation argument fails fast`() {
        val repository = FakeAdsRepository(baseDetail = baseDetail)

        try {
            DetailViewModel(
                savedStateHandle = SavedStateHandle(),
                observeAdDetail = ObserveAdDetailUseCase(repository),
                refreshAdDetail = RefreshAdDetailUseCase(repository),
                toggleFavorite = ToggleFavoriteUseCase(repository),
            )
            error("Expected an IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertThat(e).hasMessageThat().contains(NavArgs.PROPERTY_CODE)
        }
    }
}
