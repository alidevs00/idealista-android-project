package com.idealista.challenge.presentation.detail

import com.idealista.challenge.domain.model.AdDetail

data class DetailUiState(
    val isLoading: Boolean = true,
    val adDetail: AdDetail? = null,
    val error: Throwable? = null,
)
