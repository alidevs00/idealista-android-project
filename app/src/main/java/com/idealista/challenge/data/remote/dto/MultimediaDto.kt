package com.idealista.challenge.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MultimediaDto(
    val images: List<ImageDto> = emptyList(),
)

@Serializable
data class ImageDto(
    val url: String,
    val tag: String? = null,
)
