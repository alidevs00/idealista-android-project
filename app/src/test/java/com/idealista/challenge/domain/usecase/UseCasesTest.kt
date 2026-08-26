package com.idealista.challenge.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.idealista.challenge.domain.repository.AdsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Each use case here is a single-line delegation to [AdsRepository], so rather
 * than one file per (nearly identical) test, this verifies the whole set
 * delegates correctly with the right arguments.
 */
class UseCasesTest {

    private val repository: AdsRepository = mockk()

    @Before
    fun setUp() {
        every { repository.observeAds() } returns flowOf(emptyList())
        every { repository.observeAdDetail(any()) } returns flowOf()
        coEvery { repository.refreshAds() } returns Unit
        coEvery { repository.refreshAdDetail(any()) } returns Unit
        coEvery { repository.toggleFavorite(any()) } returns Unit
    }

    @Test
    fun `ObserveAdsUseCase delegates to repository`() {
        val result = ObserveAdsUseCase(repository)()

        assertThat(result).isNotNull()
    }

    @Test
    fun `RefreshAdsUseCase delegates to repository`() = runTest {
        RefreshAdsUseCase(repository)()

        coVerify(exactly = 1) { repository.refreshAds() }
    }

    @Test
    fun `ObserveAdDetailUseCase passes the propertyCode through`() {
        ObserveAdDetailUseCase(repository)("42")

        verify { repository.observeAdDetail("42") }
    }

    @Test
    fun `RefreshAdDetailUseCase passes the propertyCode through`() = runTest {
        RefreshAdDetailUseCase(repository)("42")

        coVerify(exactly = 1) { repository.refreshAdDetail("42") }
    }

    @Test
    fun `ToggleFavoriteUseCase passes the propertyCode through`() = runTest {
        ToggleFavoriteUseCase(repository)("42")

        coVerify(exactly = 1) { repository.toggleFavorite("42") }
    }
}
