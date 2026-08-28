package com.idealista.challenge.presentation.list

import com.idealista.challenge.domain.model.Ad

data class ListUiState(
    val isLoading: Boolean = true,
    val ads: List<Ad> = emptyList(),
    val error: Throwable? = null,
) {
    val isEmpty: Boolean get() = !isLoading && ads.isEmpty() && error == null
}
