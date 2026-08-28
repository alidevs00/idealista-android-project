package com.idealista.challenge.presentation.list

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.idealista.challenge.R
import com.idealista.challenge.di.RepositoryModule
import com.idealista.challenge.domain.model.Ad
import com.idealista.challenge.domain.model.Location
import com.idealista.challenge.domain.model.Operation
import com.idealista.challenge.domain.model.Price
import com.idealista.challenge.domain.repository.AdsRepository
import com.idealista.challenge.testutil.FakeAdsRepository
import com.idealista.challenge.testutil.launchFragmentInHiltContainer
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso test for the ad list screen, wired against a fake [AdsRepository]
 * (via Hilt's test DI) instead of the real network + Room stack - fast,
 * deterministic, and independent of any device having network access.
 */
@HiltAndroidTest
@UninstallModules(RepositoryModule::class)
@RunWith(AndroidJUnit4::class)
class ListFragmentTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    val repository: AdsRepository = FakeAdsRepository(baseAds = listOf(sampleAd()))

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun adsFromRepository_areDisplayedInTheList() {
        launchFragmentInHiltContainer<ListFragment>()

        onView(withId(R.id.ads_recycler_view)).check(matches(isDisplayed()))
        onView(withText(R.string.operation_sale)).check(matches(isDisplayed()))
    }

    private companion object {
        fun sampleAd(propertyCode: String = "1") = Ad(
            propertyCode = propertyCode,
            thumbnailUrl = "",
            price = Price(amount = 1_195_000.0, currencySuffix = "€"),
            operation = Operation.SALE,
            propertyType = "flat",
            size = 133.0,
            rooms = 3,
            bathrooms = 2,
            floor = "2",
            exterior = false,
            location = Location(
                address = "calle de Lagasca",
                municipality = "Madrid",
                district = "Barrio de Salamanca",
                neighborhood = "Castellana",
                latitude = 40.4362687,
                longitude = -3.6833686,
            ),
            description = "A lovely flat.",
            favoritedAt = null,
        )
    }
}
