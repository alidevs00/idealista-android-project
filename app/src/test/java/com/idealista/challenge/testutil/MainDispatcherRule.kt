package com.idealista.challenge.testutil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Swaps `Dispatchers.Main` for a [TestDispatcher] for the duration of each test.
 *
 * Defaults to [UnconfinedTestDispatcher] (not [kotlinx.coroutines.test.StandardTestDispatcher])
 * so ViewModel `init` blocks and `viewModelScope.launch` calls run eagerly, the
 * same way `Dispatchers.Main.immediate` behaves in production - this avoids
 * needing manual `advanceUntilIdle()` calls scattered through every test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
