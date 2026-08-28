package com.idealista.challenge.presentation.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.idealista.challenge.R
import com.idealista.challenge.databinding.FragmentDetailBinding
import com.idealista.challenge.domain.model.AdCharacteristics
import com.idealista.challenge.domain.model.AdDetail
import com.idealista.challenge.domain.model.Operation
import com.idealista.challenge.presentation.common.FavoriteDateFormatter
import com.idealista.challenge.presentation.common.FavoriteToggleEvent
import com.idealista.challenge.presentation.common.IdealistaTheme
import com.idealista.challenge.presentation.common.PriceFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * XML-based detail screen (View Binding, matching ListFragment's approach),
 * with a single Jetpack Compose component - [HeroImagePager] - embedded via
 * a ComposeView for the swipeable image gallery. See fragment_detail.xml.
 */
@AndroidEntryPoint
class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val viewModel: DetailViewModel by viewModels()

    private val characteristicsAdapter = CharacteristicsAdapter()

    private var descriptionExpanded = false
    private var lastRenderedDescription: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.heroPager.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        binding.heroPager.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            IdealistaTheme {
                HeroImagePager(images = uiState.adDetail?.images.orEmpty())
            }
        }

        binding.characteristicsGrid.adapter = characteristicsAdapter
        binding.characteristicsGrid.layoutManager = GridLayoutManager(requireContext(), 2)

        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.favoriteButton.setOnClickListener { viewModel.onFavoriteClick() }
        binding.retryButton.setOnClickListener { viewModel.refresh() }
        binding.descriptionToggle.setOnClickListener { toggleDescription() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { state -> render(state) } }
                // Favorite feedback is a one-off event, not screen state (see
                // CLAUDE.md §3), so it's collected from its own SharedFlow
                // instead of being derived from click-time state here.
                launch { viewModel.favoriteToggleEvents.collect { event -> showFavoriteSnackbar(event) } }
            }
        }
    }

    private fun render(state: DetailUiState) {
        val detail = state.adDetail
        val hasError = state.error != null

        binding.loadingIndicator.isVisible = state.isLoading && detail == null
        binding.errorView.isVisible = hasError
        // A background refresh can fail while a previously-loaded detail is
        // still cached (see AdsRepositoryImpl.detailCache) - without the
        // `!hasError` check here, the error view and the stale content would
        // both be visible at once, overlapping. Same convention ListFragment
        // already uses for its own error/content visibility.
        binding.contentScroll.isVisible = detail != null && !hasError
        binding.favoriteButton.isVisible = detail != null && !hasError

        state.error?.let { error ->
            binding.errorTitle.text = getString(
                if (error is IOException) R.string.error_network else R.string.error_generic,
            )
        }

        detail?.let { renderDetail(it) }
    }

    private fun renderDetail(detail: AdDetail) {
        binding.operationBadge.text = getString(
            R.string.detail_operation_type_format,
            getString(if (detail.operation == Operation.RENT) R.string.operation_rent else R.string.operation_sale),
            detail.propertyType,
        )

        binding.price.text = PriceFormatter.format(detail.price)

        val favoritedAt = detail.favoritedAt
        binding.favoritedDate.isVisible = favoritedAt != null
        if (favoritedAt != null) {
            binding.favoritedDate.text = getString(R.string.favorited_since_format, FavoriteDateFormatter.format(favoritedAt))
        }

        binding.favoriteButton.setImageResource(
            if (detail.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border,
        )

        // The detail endpoint carries no address fields of its own - when there's
        // no matching list item to fall back on (e.g. a deep link opened before
        // the list has loaded), district/municipality are both null and there's
        // nothing meaningful to show, so hide the row instead of an empty gap.
        val locationText = listOfNotNull(detail.location.district, detail.location.municipality)
            .joinToString(separator = ", ")
        binding.location.text = locationText
        binding.location.isVisible = locationText.isNotBlank()
        binding.locationIcon.isVisible = locationText.isNotBlank()

        characteristicsAdapter.submitList(buildCharacteristics(detail.characteristics))

        renderDescription(detail.description)
    }

    private fun buildCharacteristics(characteristics: AdCharacteristics): List<String> = buildList {
        add(getString(R.string.detail_characteristic_rooms, characteristics.rooms))
        add(getString(R.string.detail_characteristic_bathrooms, characteristics.bathrooms))
        characteristics.constructedArea?.let { add(getString(R.string.detail_characteristic_size, it)) }
        characteristics.floor?.let { add(getString(R.string.detail_characteristic_floor, it)) }
        add(
            getString(
                if (characteristics.exterior) R.string.detail_characteristic_exterior else R.string.detail_characteristic_interior,
            ),
        )
        if (characteristics.hasLift == true) add(getString(R.string.detail_characteristic_lift))
        if (characteristics.hasBoxroom == true) add(getString(R.string.detail_characteristic_boxroom))
        characteristics.energyCertification?.let {
            add(getString(R.string.detail_characteristic_energy_format, it.uppercase()))
        }
        characteristics.communityCosts?.let {
            add(getString(R.string.detail_community_costs_format, it.toInt().toString()))
        }
    }

    private fun renderDescription(description: String) {
        // Guard against re-collapsing the description every time uiState changes
        // for an unrelated reason (e.g. a favorite toggle emits a new AdDetail
        // copy) - only reset the expand/collapse state when the text is new.
        if (description == lastRenderedDescription) return
        lastRenderedDescription = description
        descriptionExpanded = false

        // Captured as locals rather than read through `binding` inside the
        // post{} callback below, which may run after onDestroyView() has
        // already nulled out _binding.
        val descriptionView = binding.description
        val toggleView = binding.descriptionToggle

        descriptionView.text = description
        descriptionView.maxLines = DESCRIPTION_COLLAPSED_MAX_LINES
        toggleView.text = getString(R.string.show_more)
        descriptionView.post {
            val layout = descriptionView.layout
            val overflows = layout != null && layout.lineCount > 0 && layout.getEllipsisCount(layout.lineCount - 1) > 0
            toggleView.isVisible = overflows
        }
    }

    private fun toggleDescription() {
        descriptionExpanded = !descriptionExpanded
        binding.description.maxLines = if (descriptionExpanded) Int.MAX_VALUE else DESCRIPTION_COLLAPSED_MAX_LINES
        binding.descriptionToggle.text = getString(if (descriptionExpanded) R.string.show_less else R.string.show_more)
    }

    private fun showFavoriteSnackbar(event: FavoriteToggleEvent) {
        val messageRes = if (event == FavoriteToggleEvent.ADDED) {
            R.string.added_to_favorites
        } else {
            R.string.removed_from_favorites
        }
        Snackbar.make(binding.root, messageRes, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.characteristicsGrid.adapter = null
        _binding = null
    }

    private companion object {
        const val DESCRIPTION_COLLAPSED_MAX_LINES = 4
    }
}
