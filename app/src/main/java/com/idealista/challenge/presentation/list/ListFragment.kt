package com.idealista.challenge.presentation.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.idealista.challenge.R
import com.idealista.challenge.databinding.FragmentListBinding
import com.idealista.challenge.domain.model.Ad
import com.idealista.challenge.presentation.common.FavoriteToggleEvent
import com.idealista.challenge.presentation.common.NavArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.IOException

@AndroidEntryPoint
class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val viewModel: ListViewModel by viewModels()

    private val adapter = AdsAdapter(
        onAdClick = { ad -> navigateToDetail(ad) },
        onFavoriteClick = { ad -> viewModel.onFavoriteClick(ad.propertyCode) },
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.adsRecyclerView.adapter = adapter
        binding.adsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }
        binding.retryButton.setOnClickListener { viewModel.refresh() }
        binding.errorTitle.text = getString(R.string.list_error_title)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { state -> render(state) } }
                // Favorite feedback is a one-off event, not screen state (see
                // CLAUDE.md §3), so it's collected from its own SharedFlow
                // instead of being derived from click-time state in this Fragment.
                launch { viewModel.favoriteToggleEvents.collect { event -> showFavoriteSnackbar(event) } }
            }
        }
    }

    private fun render(state: ListUiState) {
        binding.swipeRefresh.isRefreshing = state.isLoading && adapter.itemCount > 0
        binding.loadingIndicator.visibility = viewIf(state.isLoading && adapter.itemCount == 0)
        binding.errorView.visibility = viewIf(state.error != null)
        binding.emptyView.visibility = viewIf(state.isEmpty)
        binding.swipeRefresh.visibility = viewIf(state.error == null && !state.isEmpty)

        state.error?.let { error ->
            binding.errorMessage.text = getString(
                if (error is IOException) R.string.error_network else R.string.error_generic,
            )
        }

        adapter.submitList(state.ads)
    }

    private fun showFavoriteSnackbar(event: FavoriteToggleEvent) {
        val messageRes = if (event == FavoriteToggleEvent.ADDED) {
            R.string.added_to_favorites
        } else {
            R.string.removed_from_favorites
        }
        Snackbar.make(binding.root, messageRes, Snackbar.LENGTH_SHORT).show()
    }

    private fun navigateToDetail(ad: Ad) {
        val bundle = Bundle().apply { putString(NavArgs.PROPERTY_CODE, ad.propertyCode) }
        findNavController().navigate(R.id.action_listFragment_to_detailFragment, bundle)
    }

    private fun viewIf(condition: Boolean) = if (condition) View.VISIBLE else View.GONE

    override fun onDestroyView() {
        super.onDestroyView()
        binding.adsRecyclerView.adapter = null
        _binding = null
    }
}
