package com.idealista.challenge.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.idealista.challenge.R
import com.idealista.challenge.domain.model.AdCharacteristics
import com.idealista.challenge.domain.model.AdDetail
import com.idealista.challenge.domain.model.AdImage
import com.idealista.challenge.domain.model.Location
import com.idealista.challenge.domain.model.Operation
import com.idealista.challenge.domain.model.Price
import com.idealista.challenge.presentation.common.FavoriteDateFormatter
import com.idealista.challenge.presentation.common.PriceFormatter
import java.io.IOException
import java.time.Instant

/**
 * Stateless screen: all data comes from [uiState], all Snackbar/navigation
 * plumbing is owned by [DetailFragment] and handed in ([snackbarHostState],
 * the click callbacks) so this composable stays trivial to preview and test.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    uiState: DetailUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back_content_description))
                    }
                },
            )
        },
    ) { contentPadding ->
        val detail = uiState.adDetail
        when {
            detail != null -> DetailContent(
                detail = detail,
                onFavoriteClick = onFavoriteClick,
                contentPadding = contentPadding,
            )

            uiState.error != null -> DetailErrorState(
                error = uiState.error,
                onRetryClick = onRetryClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            )

            else -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun DetailContent(
    detail: AdDetail,
    onFavoriteClick: () -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(contentPadding = contentPadding) {
        item { ImageGallery(images = detail.images) }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                PriceAndFavoriteRow(
                    price = detail.price,
                    favoritedAt = detail.favoritedAt,
                    onFavoriteClick = onFavoriteClick,
                )

                Text(
                    text = operationTypeLabel(detail.operation, detail.propertyType),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 4.dp),
                )

                locationLabel(detail.location)?.let { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }

        item { CharacteristicsSection(detail.characteristics) }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = stringResource(R.string.detail_description_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = detail.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun ImageGallery(images: List<AdImage>) {
    if (images.isEmpty()) return
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(images, key = { it.url }) { image ->
            AsyncImage(
                model = image.url,
                contentDescription = image.tag,
                modifier = Modifier
                    .size(width = 280.dp, height = 210.dp),
            )
        }
    }
}

@Composable
private fun PriceAndFavoriteRow(
    price: Price,
    favoritedAt: Instant?,
    onFavoriteClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(text = PriceFormatter.format(price), style = MaterialTheme.typography.headlineSmall)
            favoritedAt?.let {
                Text(
                    text = stringResource(R.string.favorited_since_format, FavoriteDateFormatter.format(it)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        IconButton(onClick = onFavoriteClick, modifier = Modifier.align(Alignment.CenterEnd)) {
            Icon(
                imageVector = if (favoritedAt != null) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = stringResource(R.string.toggle_favorite_content_description),
                tint = if (favoritedAt != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CharacteristicsSection(characteristics: AdCharacteristics) {
    val labels = buildList {
        add(stringResource(R.string.detail_characteristic_rooms, characteristics.rooms))
        add(stringResource(R.string.detail_characteristic_bathrooms, characteristics.bathrooms))
        characteristics.constructedArea?.let { add(stringResource(R.string.detail_characteristic_size, it)) }
        characteristics.floor?.let { add(stringResource(R.string.detail_characteristic_floor, it)) }
        add(
            stringResource(
                if (characteristics.exterior) R.string.detail_characteristic_exterior else R.string.detail_characteristic_interior,
            ),
        )
        if (characteristics.hasLift == true) add(stringResource(R.string.detail_characteristic_lift))
        if (characteristics.hasBoxroom == true) add(stringResource(R.string.detail_characteristic_boxroom))
        characteristics.energyCertification?.let {
            add(stringResource(R.string.detail_characteristic_energy_format, it.uppercase()))
        }
        characteristics.communityCosts?.let {
            add(stringResource(R.string.detail_community_costs_format, it.toInt().toString()))
        }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Simple two-column-ish wrap using chained rows keeps this dependency-free;
        // a real FlowRow (compose-foundation 1.7+) would be a drop-in upgrade here.
        labels.chunked(2).forEach { rowLabels ->
            Box(modifier = Modifier.fillMaxWidth()) {
                Column {
                    rowLabels.forEach { label ->
                        Text(
                            text = "• $label",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailErrorState(
    error: Throwable,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(if (error is IOException) R.string.error_network else R.string.error_generic),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        OutlinedButton(onClick = onRetryClick, modifier = Modifier.padding(top = 16.dp)) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
private fun operationTypeLabel(operation: Operation, propertyType: String): String {
    val operationLabel = stringResource(if (operation == Operation.RENT) R.string.operation_rent else R.string.operation_sale)
    return "$operationLabel · $propertyType"
}

private fun locationLabel(location: Location): String? =
    listOfNotNull(location.district, location.municipality).takeIf { it.isNotEmpty() }?.joinToString(", ")
