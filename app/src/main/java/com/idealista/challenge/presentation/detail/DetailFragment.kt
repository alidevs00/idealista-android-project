package com.idealista.challenge.presentation.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
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

            DetailScreen(
                uiState = uiState,
                onBackClick = { findNavController().navigateUp() },
                onFavoriteClick = viewModel::onFavoriteClick,
                onRetryClick = viewModel::refresh,
            )
        }
    }
}
