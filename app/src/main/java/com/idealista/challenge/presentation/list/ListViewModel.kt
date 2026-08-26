package com.idealista.challenge.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idealista.challenge.domain.usecase.ObserveAdsUseCase
import com.idealista.challenge.domain.usecase.RefreshAdsUseCase
import com.idealista.challenge.domain.usecase.ToggleFavoriteUseCase
import com.idealista.challenge.presentation.common.FavoriteToggleEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val observeAds: ObserveAdsUseCase,
    private val refreshAds: RefreshAdsUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListUiState())
    val uiState: StateFlow<ListUiState> = _uiState.asStateFlow()

    // One-off UI feedback (a Snackbar) for a favorite toggle. Kept out of UiState
    // on purpose: a state field would replay the last event on every config
    // change/recomposition instead of firing once (see CLAUDE.md §3).
    private val _favoriteToggleEvents = MutableSharedFlow<FavoriteToggleEvent>(extraBufferCapacity = 1)
    val favoriteToggleEvents: SharedFlow<FavoriteToggleEvent> = _favoriteToggleEvents.asSharedFlow()

    init {
        // Ads/favorite state is pushed here reactively; isLoading/error are only
        // ever touched by refresh() below, so a favorite toggle never flashes a
        // loading state.
        observeAds()
            .onEach { ads -> _uiState.update { it.copy(ads = ads) } }
            .launchIn(viewModelScope)

        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                refreshAds()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onFavoriteClick(propertyCode: String) {
        // Snapshot the pre-toggle state before dispatching, since the toggle
        // itself is fire-and-forget and observeAds() reflects it reactively
        // afterwards - reading it after would race that update.
        val wasFavorite = _uiState.value.ads.firstOrNull { it.propertyCode == propertyCode }?.isFavorite == true
        viewModelScope.launch { toggleFavorite(propertyCode) }
        _favoriteToggleEvents.tryEmit(if (wasFavorite) FavoriteToggleEvent.REMOVED else FavoriteToggleEvent.ADDED)
    }
}
