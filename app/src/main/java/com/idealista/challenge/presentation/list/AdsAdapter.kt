package com.idealista.challenge.presentation.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.idealista.challenge.R
import com.idealista.challenge.databinding.ItemAdBinding
import com.idealista.challenge.domain.model.Ad
import com.idealista.challenge.domain.model.Operation
import com.idealista.challenge.presentation.common.FavoriteDateFormatter
import com.idealista.challenge.presentation.common.PriceFormatter

class AdsAdapter(
    private val onAdClick: (Ad) -> Unit,
    private val onFavoriteClick: (Ad) -> Unit,
) : ListAdapter<Ad, AdsAdapter.AdViewHolder>(AdDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdViewHolder {
        val binding = ItemAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdViewHolder(binding, onAdClick, onFavoriteClick)
    }

    override fun onBindViewHolder(holder: AdViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AdViewHolder(
        private val binding: ItemAdBinding,
        private val onAdClick: (Ad) -> Unit,
        private val onFavoriteClick: (Ad) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ad: Ad) = with(binding) {
            thumbnail.load(ad.thumbnailUrl) {
                crossfade(true)
            }

            price.text = PriceFormatter.format(ad.price)

            operationBadge.text = root.context.getString(
                if (ad.operation == Operation.RENT) R.string.operation_rent else R.string.operation_sale,
            )
            summary.text = root.context.getString(
                R.string.ad_summary_format,
                ad.rooms,
                ad.size.toInt().toString(),
            )

            address.text = listOfNotNull(ad.location.district, ad.location.municipality)
                .joinToString(separator = ", ")

            val favoritedAt = ad.favoritedAt
            favoritedDate.isVisible = favoritedAt != null
            if (favoritedAt != null) {
                favoritedDate.text = root.context.getString(
                    R.string.favorited_since_format,
                    FavoriteDateFormatter.format(favoritedAt),
                )
            }

            favoriteButton.setImageResource(
                if (ad.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border,
            )

            root.setOnClickListener { onAdClick(ad) }
            favoriteButton.setOnClickListener { onFavoriteClick(ad) }
        }
    }

    private object AdDiffCallback : DiffUtil.ItemCallback<Ad>() {
        override fun areItemsTheSame(oldItem: Ad, newItem: Ad) = oldItem.propertyCode == newItem.propertyCode
        override fun areContentsTheSame(oldItem: Ad, newItem: Ad) = oldItem == newItem
    }
}
