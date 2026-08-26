package com.idealista.challenge.domain.model

data class AdImage(
    val url: String,
    /** Room type shown in the source data (e.g. "livingRoom", "bedroom"); null when unclassified. */
    val tag: String?,
)
