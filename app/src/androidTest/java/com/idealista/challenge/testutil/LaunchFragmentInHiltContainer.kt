package com.idealista.challenge.testutil

import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import com.idealista.challenge.HiltTestActivity

/**
 * Launches [T] hosted inside [HiltTestActivity] so its Hilt-injected
 * ViewModel(s) resolve correctly.
 *
 * `androidx.fragment.testing.FragmentScenario`'s own launch helpers host the
 * fragment inside a generic, non-Hilt activity, which breaks `by viewModels()`
 * for an `@AndroidEntryPoint` fragment - this is the standard workaround,
 * adding the fragment directly to a Hilt-enabled activity's content view.
 */
inline fun <reified T : Fragment> launchFragmentInHiltContainer(): ActivityScenario<HiltTestActivity> {
    val scenario = ActivityScenario.launch(HiltTestActivity::class.java)
    scenario.onActivity { activity ->
        val fragment = activity.supportFragmentManager.fragmentFactory.instantiate(
            requireNotNull(T::class.java.classLoader),
            T::class.java.name,
        )
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "test_fragment")
            .commitNow()
    }
    return scenario
}
