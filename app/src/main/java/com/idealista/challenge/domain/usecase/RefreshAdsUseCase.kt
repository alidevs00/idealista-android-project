package com.idealista.challenge.domain.usecase

import com.idealista.challenge.domain.repository.AdsRepository
import javax.inject.Inject

/** Fetches the ad list from the network and refreshes the local cache. */
class RefreshAdsUseCase @Inject constructor(
    private val repository: AdsRepository,
) {
    suspend operator fun invoke() = repository.refreshAds()
}
