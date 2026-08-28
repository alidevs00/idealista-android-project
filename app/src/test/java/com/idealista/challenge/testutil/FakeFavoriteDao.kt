package com.idealista.challenge.testutil

import com.idealista.challenge.data.local.dao.FavoriteDao
import com.idealista.challenge.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory [FavoriteDao] fake. [toggle] is not overridden - it's inherited as-is
 * from the interface's default (`findOne` + `insert`/`delete`), which is exactly
 * what real Room does under `@Transaction`, so the fake exercises the same logic
 * the production DAO runs.
 */
class FakeFavoriteDao : FavoriteDao {

    private val favorites = MutableStateFlow<List<FavoriteEntity>>(emptyList())

    val current: List<FavoriteEntity> get() = favorites.value

    override fun observeAll(): StateFlow<List<FavoriteEntity>> = favorites

    override fun observeOne(propertyCode: String) =
        favorites.map { list -> list.find { it.propertyCode == propertyCode } }

    override suspend fun findOne(propertyCode: String): FavoriteEntity? =
        favorites.value.find { it.propertyCode == propertyCode }

    override suspend fun insert(favorite: FavoriteEntity) {
        favorites.value = favorites.value.filterNot { it.propertyCode == favorite.propertyCode } + favorite
    }

    override suspend fun delete(favorite: FavoriteEntity) {
        favorites.value = favorites.value.filterNot { it.propertyCode == favorite.propertyCode }
    }
}
