package com.idealista.challenge.domain.usecase

import com.idealista.challenge.domain.repository.AdsRepository
import javax.inject.Inject

/** Fetches a single ad's detail from the network and refreshes the local cache. */
class RefreshAdDetailUseCase @Inject constructor(
    private val repository: AdsRepository,
) {
    suspend operator fun invoke(propertyCode: String) = repository.refreshAdDetail(propertyCode)
}
