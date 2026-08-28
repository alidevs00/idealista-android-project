package com.idealista.challenge.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idealista.challenge.domain.usecase.ObserveAdDetailUseCase
import com.idealista.challenge.domain.usecase.RefreshAdDetailUseCase
import com.idealista.challenge.domain.usecase.ToggleFavoriteUseCase
import com.idealista.challenge.presentation.common.FavoriteToggleEvent
import com.idealista.challenge.presentation.common.NavArgs
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
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeAdDetail: ObserveAdDetailUseCase,
    private val refreshAdDetail: RefreshAdDetailUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : ViewModel() {

    private val propertyCode: String = requireNotNull(savedStateHandle[NavArgs.PROPERTY_CODE]) {
        "DetailViewModel requires a '${NavArgs.PROPERTY_CODE}' navigation argument"
    }

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // Same reasoning as ListViewModel: one-off Snackbar feedback lives outside
    // UiState so it fires exactly once instead of replaying on recomposition.
    private val _favoriteToggleEvents = MutableSharedFlow<FavoriteToggleEvent>(extraBufferCapacity = 1)
    val favoriteToggleEvents: SharedFlow<FavoriteToggleEvent> = _favoriteToggleEvents.asSharedFlow()

    init {
        // Same split as ListViewModel: reactive data goes through the observe
        // flow, isLoading/error are only ever touched by refresh().
        observeAdDetail(propertyCode)
            .onEach { detail -> _uiState.update { it.copy(adDetail = detail) } }
            .launchIn(viewModelScope)

        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                refreshAdDetail(propertyCode)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onFavoriteClick() {
        val wasFavorite = _uiState.value.adDetail?.isFavorite == true
        viewModelScope.launch { toggleFavorite(propertyCode) }
        _favoriteToggleEvents.tryEmit(if (wasFavorite) FavoriteToggleEvent.REMOVED else FavoriteToggleEvent.ADDED)
    }
}
