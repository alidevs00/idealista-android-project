package com.idealista.challenge.domain.usecase

import com.idealista.challenge.domain.repository.AdsRepository
import javax.inject.Inject

/** Favorites the given ad if it isn't already, or un-favorites it if it is. */
class ToggleFavoriteUseCase @Inject constructor(
    private val repository: AdsRepository,
) {
    suspend operator fun invoke(propertyCode: String) = repository.toggleFavorite(propertyCode)
}
