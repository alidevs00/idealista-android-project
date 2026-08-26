package com.idealista.challenge.domain.usecase

import com.idealista.challenge.domain.model.AdDetail
import com.idealista.challenge.domain.repository.AdsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes a single ad's detail, favorite state included. Does not trigger a network fetch. */
class ObserveAdDetailUseCase @Inject constructor(
    private val repository: AdsRepository,
) {
    operator fun invoke(propertyCode: String): Flow<AdDetail> = repository.observeAdDetail(propertyCode)
}
