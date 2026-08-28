package com.idealista.challenge.presentation.detail

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.idealista.challenge.domain.model.AdImage
import com.idealista.challenge.presentation.common.IdealistaTheme
import org.junit.Rule
import org.junit.Test

/** Compose UI test for the detail screen's image gallery - see HeroImagePager.kt. */
class HeroImagePagerTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun noImages_showsPhotoUnavailablePlaceholder() {
        composeRule.setContent {
            IdealistaTheme { HeroImagePager(images = emptyList()) }
        }

        composeRule.onNodeWithTag(HeroImagePagerTestTags.PHOTO_UNAVAILABLE).assertIsDisplayed()
    }

    @Test
    fun singleImage_hidesPagerDots() {
        composeRule.setContent {
            IdealistaTheme { HeroImagePager(images = listOf(AdImage(url = "", tag = null))) }
        }

        composeRule.onNodeWithTag(HeroImagePagerTestTags.PAGER_DOTS).assertDoesNotExist()
    }

    @Test
    fun multipleImages_showsPagerDots() {
        composeRule.setContent {
            IdealistaTheme {
                HeroImagePager(
                    images = listOf(
                        AdImage(url = "", tag = null),
                        AdImage(url = "", tag = null),
                        AdImage(url = "", tag = null),
                    ),
                )
            }
        }

        composeRule.onNodeWithTag(HeroImagePagerTestTags.PAGER_DOTS).assertIsDisplayed()
    }
}
