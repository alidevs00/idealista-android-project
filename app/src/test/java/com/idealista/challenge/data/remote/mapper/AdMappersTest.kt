package com.idealista.challenge.data.remote.mapper

import com.google.common.truth.Truth.assertThat
import com.idealista.challenge.domain.model.Location
import com.idealista.challenge.domain.model.Operation
import com.idealista.challenge.testutil.Fixtures
import org.junit.Test
import java.time.Instant

class AdMappersTest {

    @Test
    fun `list item maps operation, price and location`() {
        val dto = Fixtures.adListItemDto(operation = "rent", amount = 1200.0, currencySuffix = "€/mes")

        val ad = dto.toDomain(favoritedAt = null)

        assertThat(ad.operation).isEqualTo(Operation.RENT)
        assertThat(ad.price.amount).isEqualTo(1200.0)
        assertThat(ad.price.currencySuffix).isEqualTo("€/mes")
        assertThat(ad.location.address).isEqualTo(dto.address)
        assertThat(ad.isFavorite).isFalse()
    }

    @Test
    fun `unknown operation string maps to UNKNOWN`() {
        val dto = Fixtures.adListItemDto(operation = "auction")

        assertThat(dto.toDomain(favoritedAt = null).operation).isEqualTo(Operation.UNKNOWN)
    }

    @Test
    fun `favoritedAt is carried through as isFavorite`() {
        val dto = Fixtures.adListItemDto()
        val now = Instant.now()

        val ad = dto.toDomain(favoritedAt = now)

        assertThat(ad.isFavorite).isTrue()
        assertThat(ad.favoritedAt).isEqualTo(now)
    }

    @Test
    fun `detail uses the requested propertyCode, not anything from the response`() {
        val dto = Fixtures.adDetailDto()

        val detail = dto.toDomain(propertyCode = "42", favoritedAt = null, fallbackLocation = null)

        assertThat(detail.propertyCode).isEqualTo("42")
    }

    @Test
    fun `detail falls back to the list item's location for address fields`() {
        val dto = Fixtures.adDetailDto()
        val fallback = Location(
            address = "calle de Lagasca",
            municipality = "Madrid",
            district = "Barrio de Salamanca",
            neighborhood = "Castellana",
            latitude = 0.0,
            longitude = 0.0,
        )

        val detail = dto.toDomain(propertyCode = "1", favoritedAt = null, fallbackLocation = fallback)

        // Address fields come from the fallback...
        assertThat(detail.location.address).isEqualTo("calle de Lagasca")
        assertThat(detail.location.district).isEqualTo("Barrio de Salamanca")
        // ...but coordinates always come from the detail response itself.
        assertThat(detail.location.latitude).isEqualTo(dto.ubication.latitude)
        assertThat(detail.location.longitude).isEqualTo(dto.ubication.longitude)
    }

    @Test
    fun `detail with no fallback location has null address fields but real coordinates`() {
        val dto = Fixtures.adDetailDto()

        val detail = dto.toDomain(propertyCode = "1", favoritedAt = null, fallbackLocation = null)

        assertThat(detail.location.address).isNull()
        assertThat(detail.location.latitude).isEqualTo(dto.ubication.latitude)
    }

    @Test
    fun `detail characteristics map from moreCharacteristics`() {
        val dto = Fixtures.adDetailDto()

        val characteristics = dto.toDomain(propertyCode = "1", favoritedAt = null, fallbackLocation = null).characteristics

        assertThat(characteristics.rooms).isEqualTo(dto.moreCharacteristics.roomNumber)
        assertThat(characteristics.bathrooms).isEqualTo(dto.moreCharacteristics.bathNumber)
        assertThat(characteristics.hasLift).isEqualTo(dto.moreCharacteristics.lift)
        assertThat(characteristics.energyCertification).isEqualTo(dto.moreCharacteristics.energyCertificationType)
    }
}
