package com.idealista.challenge.domain.usecase

import com.idealista.challenge.domain.model.Ad
import com.idealista.challenge.domain.repository.AdsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes the cached ad list, favorite state included. Does not trigger a network fetch. */
class ObserveAdsUseCase @Inject constructor(
    private val repository: AdsRepository,
) {
    operator fun invoke(): Flow<List<Ad>> = repository.observeAds()
}
