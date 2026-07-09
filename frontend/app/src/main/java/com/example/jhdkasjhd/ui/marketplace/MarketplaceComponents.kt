package com.example.jhdkasjhd.ui.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.jhdkasjhd.data.dto.EventResponse
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusPill
import com.example.jhdkasjhd.ui.theme.CoinbaseCanvas
import com.example.jhdkasjhd.ui.theme.CoinbaseHairline
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbaseOnPrimary
import com.example.jhdkasjhd.ui.theme.CoinbasePrimary
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusLg
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusMd
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusXl
import com.example.jhdkasjhd.ui.theme.CoinbaseSemanticUp
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing
import com.example.jhdkasjhd.ui.theme.CoinbaseSurfaceSoft

private val DiscoverImageRadius = RoundedCornerShape(12.dp)

@Composable
internal fun EventbriteDiscoverSearchBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    locationLabel: String = "Panamá",
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(CoinbaseRadiusPill)
                .border(1.dp, CoinbaseHairline, CoinbaseRadiusPill)
                .background(CoinbaseCanvas)
                .padding(horizontal = CoinbaseSpacing.base, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = CoinbaseInk,
                modifier = Modifier.size(22.dp)
            )
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = CoinbaseInk,
                    fontWeight = FontWeight.SemiBold
                ),
                interactionSource = remember { MutableInteractionSource() },
                decorationBox = { inner ->
                    if (searchQuery.isEmpty()) {
                        Column {
                            Text(
                                text = "Encuentra qué hacer",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CoinbaseMuted
                            )
                            Text(
                                text = "en $locationLabel",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CoinbaseInk,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    inner()
                }
            )
        }

        IconButton(
            onClick = onFilterClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(1.dp, CoinbaseHairline, CircleShape)
                .background(CoinbaseCanvas)
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Filtrar",
                tint = CoinbaseInk,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
internal fun DiscoverEventCard(
    event: EventResponse,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    onShare: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.55f)
                .clip(DiscoverImageRadius)
        ) {
            EventImage(
                bannerUrl = event.bannerUrl,
                category = event.category,
                modifier = Modifier.fillMaxSize()
            )
            if (isEndingSoon(event.dateStart)) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(CoinbaseSpacing.sm)
                        .clip(CoinbaseRadiusMd)
                        .background(CoinbaseSemanticUp)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "⏳",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "CUPOS LIMITADOS",
                        style = MaterialTheme.typography.labelSmall,
                        color = CoinbaseOnPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                color = CoinbaseInk,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onShare,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Compartir",
                    tint = CoinbaseInk,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onToggleSave,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isSaved) "Quitar de guardados" else "Guardar",
                    tint = if (isSaved) CoinbasePrimary else CoinbaseInk,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = formatEventDiscoverMeta(event),
            style = MaterialTheme.typography.bodyMedium,
            color = CoinbaseMuted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "Desde $0",
            style = MaterialTheme.typography.bodyLarge,
            color = CoinbaseInk,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
internal fun DiscoverHeader(
    userName: String?,
    locationLabel: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.base)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = CoinbasePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Panamá",
                        style = MaterialTheme.typography.labelMedium,
                        color = CoinbaseMuted
                    )
                }
                Text(
                    text = locationLabel,
                    style = MaterialTheme.typography.titleLarge,
                    color = CoinbaseInk,
                    fontWeight = FontWeight.SemiBold
                )
                if (!userName.isNullOrBlank()) {
                    Text(
                        text = "Hola, $userName",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoinbaseMuted
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CoinbaseRadiusPill)
                .background(CoinbaseSurfaceSoft)
                .padding(horizontal = CoinbaseSpacing.base, vertical = CoinbaseSpacing.sm)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = CoinbaseMuted
                )
                androidx.compose.foundation.text.BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = CoinbaseInk),
                    singleLine = true,
                    decorationBox = { inner ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Buscar eventos, lugares o categorías",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CoinbaseMuted
                            )
                        }
                        inner()
                    }
                )
            }
        }
    }
}

@Composable
internal fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = CoinbaseInk,
            fontWeight = FontWeight.SemiBold
        )
        if (actionLabel != null && onActionClick != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = CoinbasePrimary,
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}

@Composable
internal fun CategoryPreviewCard(
    category: CategoryItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val palette = categoryPalette(category.name)
    Box(
        modifier = modifier
            .width(132.dp)
            .height(156.dp)
            .clip(CoinbaseRadiusXl)
            .clickable(onClick = onClick)
    ) {
        CategoryImageBackground(categoryName = category.name)
        CategoryGeometricOverlay(accent = palette.accent)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(CoinbaseSpacing.base),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = categoryEmoji(category.name),
                style = MaterialTheme.typography.headlineSmall
            )
            Column {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = CoinbaseOnPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${category.eventCount} eventos",
                    style = MaterialTheme.typography.labelSmall,
                    color = CoinbaseOnPrimary.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
internal fun CategoryGridCard(
    category: CategoryItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val palette = categoryPalette(category.name)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.82f)
            .clip(CoinbaseRadiusXl)
            .clickable(onClick = onClick)
    ) {
        CategoryImageBackground(categoryName = category.name)
        CategoryGeometricOverlay(accent = palette.accent)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(CoinbaseSpacing.base),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = categoryEmoji(category.name),
                style = MaterialTheme.typography.headlineMedium
            )
            Column {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = CoinbaseOnPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${category.eventCount} eventos disponibles",
                    style = MaterialTheme.typography.labelMedium,
                    color = CoinbaseOnPrimary.copy(alpha = 0.88f)
                )
            }
        }
    }
}

@Composable
private fun CategoryImageBackground(categoryName: String) {
    SubcomposeAsyncImage(
        model = categoryImageUrl(categoryName),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(categoryGradient(categoryName))
            )
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(categoryGradient(categoryName))
            )
        }
    )
}

@Composable
private fun CategoryGeometricOverlay(accent: Color) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            accent.copy(alpha = 0.18f),
                            Color.Black.copy(alpha = 0.28f)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 18.dp, end = 12.dp)
                .size(42.dp)
                .clip(RoundedCornerShape(topStart = 18.dp, bottomEnd = 18.dp))
                .background(CoinbaseCanvas.copy(alpha = 0.18f))
        )
    }
}

@Composable
internal fun FeaturedEventCard(
    event: EventResponse,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .width(260.dp)
            .clip(CoinbaseRadiusXl)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
        ) {
            EventImage(
                bannerUrl = event.bannerUrl,
                category = event.category,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                        )
                    )
                    .padding(CoinbaseSpacing.sm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ver detalles",
                    style = MaterialTheme.typography.labelLarge,
                    color = CoinbaseOnPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CoinbaseSurfaceSoft)
                .padding(CoinbaseSpacing.base)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = CoinbaseInk,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(CoinbaseSpacing.sm))
                Text(
                    text = "Gratis",
                    style = MaterialTheme.typography.labelLarge,
                    color = CoinbasePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(CoinbaseSpacing.xs))
            EventMetaRow(location = event.location, date = formatEventDate(event.dateStart))
        }
    }
}

@Composable
internal fun NearbyEventRow(
    event: EventResponse,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CoinbaseRadiusLg)
            .background(CoinbaseCanvas)
            .clickable(onClick = onClick)
            .padding(CoinbaseSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.base),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CoinbaseRadiusMd)
        ) {
            EventImage(
                bannerUrl = event.bannerUrl,
                category = event.category,
                modifier = Modifier.fillMaxSize()
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleSmall,
                color = CoinbaseInk,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(CoinbaseSpacing.xxs))
            EventMetaRow(location = event.location, date = formatEventDate(event.dateStart))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Cupos",
                style = MaterialTheme.typography.labelSmall,
                color = CoinbaseMuted
            )
            Text(
                text = "${event.capacity}",
                style = MaterialTheme.typography.titleSmall,
                color = CoinbasePrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EventMetaRow(location: String, date: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = CoinbaseMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(2.dp))
            Text(
                text = location,
                style = MaterialTheme.typography.labelSmall,
                color = CoinbaseMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = CoinbaseMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(2.dp))
            Text(
                text = date,
                style = MaterialTheme.typography.labelSmall,
                color = CoinbaseMuted
            )
        }
    }
}

@Composable
internal fun EventImage(
    bannerUrl: String?,
    category: String,
    modifier: Modifier = Modifier
) {
    val sanitizedUrl = sanitizeBannerUrl(bannerUrl)
    if (sanitizedUrl != null) {
        SubcomposeAsyncImage(
            model = sanitizedUrl,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            loading = {
                EventImagePlaceholder(category = category, modifier = Modifier.fillMaxSize())
            },
            error = {
                EventImagePlaceholder(category = category, modifier = Modifier.fillMaxSize())
            }
        )
    } else {
        EventImagePlaceholder(category = category, modifier = modifier)
    }
}

@Composable
private fun EventImagePlaceholder(
    category: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(categoryGradient(category)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = categoryEmoji(category),
            style = MaterialTheme.typography.displaySmall
        )
    }
}

@Composable
internal fun ActiveFilterChip(
    label: String,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(CoinbaseRadiusMd)
            .background(CoinbasePrimary.copy(alpha = 0.08f))
            .clickable(onClick = onClear)
            .padding(horizontal = CoinbaseSpacing.base, vertical = CoinbaseSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xs)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = CoinbasePrimary
        )
        Text(
            text = "×",
            style = MaterialTheme.typography.titleMedium,
            color = CoinbasePrimary
        )
    }
}

@Composable
internal fun DiscoverDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(CoinbaseHairline)
    )
}
