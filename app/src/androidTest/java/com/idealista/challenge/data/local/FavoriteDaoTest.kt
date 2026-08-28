package com.idealista.challenge.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.idealista.challenge.data.local.dao.FavoriteDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises [FavoriteDao] against a real, in-memory Room database. This is the
 * one layer the JVM unit tests (AdsRepositoryImplTest) fake out entirely via
 * FakeFavoriteDao, so it's the one place the actual SQL/Room wiring - the
 * generated queries, the @Transaction toggle, primary key conflict handling -
 * gets verified instead of assumed.
 */
@RunWith(AndroidJUnit4::class)
class FavoriteDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: FavoriteDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.favoriteDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun toggle_withNoExistingRow_insertsIt() = runTest {
        dao.toggle(propertyCode = "1", nowEpochMillis = 1_000L)

        val stored = dao.findOne("1")

        assertThat(stored).isNotNull()
        assertThat(stored?.favoritedAtEpochMillis).isEqualTo(1_000L)
    }

    @Test
    fun toggle_withExistingRow_removesIt() = runTest {
        dao.toggle(propertyCode = "1", nowEpochMillis = 1_000L)

        dao.toggle(propertyCode = "1", nowEpochMillis = 2_000L)

        assertThat(dao.findOne("1")).isNull()
    }

    @Test
    fun observeAll_emitsCurrentFavorites() = runTest {
        dao.toggle(propertyCode = "1", nowEpochMillis = 1_000L)
        dao.toggle(propertyCode = "2", nowEpochMillis = 2_000L)

        val favorites = dao.observeAll().first()

        assertThat(favorites.map { it.propertyCode }).containsExactly("1", "2")
    }

    @Test
    fun observeOne_reflectsOnlyTheMatchingRow() = runTest {
        dao.toggle(propertyCode = "1", nowEpochMillis = 1_000L)

        val match = dao.observeOne("1").first()
        val noMatch = dao.observeOne("2").first()

        assertThat(match?.propertyCode).isEqualTo("1")
        assertThat(noMatch).isNull()
    }
}
