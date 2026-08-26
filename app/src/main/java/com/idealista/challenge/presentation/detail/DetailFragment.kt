package com.idealista.challenge.presentation.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.idealista.challenge.R
import com.idealista.challenge.presentation.common.FavoriteToggleEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailFragment : Fragment() {

    private val viewModel: DetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        // Dispose Composition when the view tree lifecycle owner (this Fragment)
        // is destroyed, not just when the ComposeView is detached - the correct
        // strategy for a Fragment-hosted single Compose screen.
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }

            // Favorite feedback is a one-off event, not screen state (see
            // CLAUDE.md §3), so it's collected here from the ViewModel's
            // SharedFlow rather than derived inside DetailScreen from
            // click-time state.
            LaunchedEffect(Unit) {
                viewModel.favoriteToggleEvents.collect { event ->
                    val messageRes = if (event == FavoriteToggleEvent.ADDED) {
                        R.string.added_to_favorites
                    } else {
                        R.string.removed_from_favorites
                    }
                    snackbarHostState.showSnackbar(getString(messageRes))
                }
            }

            DetailScreen(
                uiState = uiState,
                snackbarHostState = snackbarHostState,
                onBackClick = { findNavController().navigateUp() },
                onFavoriteClick = viewModel::onFavoriteClick,
                onRetryClick = viewModel::refresh,
            )
        }
    }
}
