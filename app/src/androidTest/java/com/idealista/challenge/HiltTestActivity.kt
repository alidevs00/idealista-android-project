package com.idealista.challenge

import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Minimal Hilt-enabled host Activity used only by instrumented Fragment tests
 * (see `launchFragmentInHiltContainer`). Fragment tests can't just host a
 * fragment inside any empty activity: a Hilt `@AndroidEntryPoint` Fragment's
 * `by viewModels()` resolves through the hosting Activity's Hilt component,
 * so the host itself needs to be `@AndroidEntryPoint` too. Registered only in
 * the androidTest manifest - never shipped in the app itself.
 */
@AndroidEntryPoint
class HiltTestActivity : AppCompatActivity()
