package com.idealista.challenge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A favorited ad. Presence of a row means favorited; [favoritedAtEpochMillis] is when. */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val propertyCode: String,
    val favoritedAtEpochMillis: Long,
)
