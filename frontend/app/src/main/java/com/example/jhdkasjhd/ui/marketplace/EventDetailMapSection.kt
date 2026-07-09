package com.example.jhdkasjhd.ui.marketplace

import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.jhdkasjhd.BuildConfig
import com.example.jhdkasjhd.ui.theme.CoinbaseCanvas
import com.example.jhdkasjhd.ui.theme.CoinbaseHairline
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbasePrimary
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusLg
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing
import com.example.jhdkasjhd.ui.theme.CoinbaseSurfaceStrong
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val DefaultPanamaCity = LatLng(8.9824, -79.5199)

@Composable
internal fun EventDetailLocationMap(location: String) {
    val context = LocalContext.current
    val displayLocation = location.ifBlank { "Panamá" }
    var coordinates by remember(displayLocation) { mutableStateOf<LatLng?>(null) }
    var geocodeFailed by remember(displayLocation) { mutableStateOf(false) }
    val hasMapsApiKey = BuildConfig.MAPS_API_KEY.isNotBlank()

    LaunchedEffect(displayLocation) {
        geocodeFailed = false
        coordinates = withContext(Dispatchers.IO) {
            resolveCoordinates(context, displayLocation)
        }
        if (coordinates == null) {
            geocodeFailed = true
            coordinates = DefaultPanamaCity
        }
    }

    val mapPosition = coordinates ?: DefaultPanamaCity
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mapPosition, 14f)
    }

    LaunchedEffect(mapPosition) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(mapPosition, 14f)
    }

    val openInGoogleMaps: () -> Unit = {
        val encoded = Uri.encode(displayLocation)
        val mapsIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("geo:0,0?q=$encoded")
        ).setPackage("com.google.android.apps.maps")
        val fallbackIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/search/?api=1&query=$encoded")
        )
        runCatching {
            context.startActivity(mapsIntent)
        }.onFailure {
            context.startActivity(fallbackIntent)
        }
        Unit
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(CoinbaseRadiusLg)
            .border(1.dp, CoinbaseHairline, CoinbaseRadiusLg)
            .clickable(onClick = openInGoogleMaps)
    ) {
        if (hasMapsApiKey && coordinates != null) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = false),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    scrollGesturesEnabled = false,
                    zoomGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                    rotationGesturesEnabled = false,
                    mapToolbarEnabled = false
                ),
                onMapClick = { openInGoogleMaps() }
            ) {
                Marker(
                    state = MarkerState(position = mapPosition),
                    title = displayLocation
                )
            }
        } else {
            EventDetailLocationMapFallback(
                location = displayLocation,
                geocodeFailed = geocodeFailed
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(CoinbaseSpacing.sm)
                .clip(CoinbaseRadiusLg)
                .background(CoinbaseCanvas.copy(alpha = 0.94f))
                .padding(horizontal = CoinbaseSpacing.sm, vertical = CoinbaseSpacing.xs)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = CoinbasePrimary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Abrir en Google Maps",
                    style = MaterialTheme.typography.labelMedium,
                    color = CoinbasePrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun EventDetailLocationMapFallback(
    location: String,
    geocodeFailed: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CoinbaseSurfaceStrong),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xs),
            modifier = Modifier.padding(horizontal = CoinbaseSpacing.base)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = CoinbasePrimary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = location,
                style = MaterialTheme.typography.bodyMedium,
                color = CoinbaseInk,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (geocodeFailed) {
                    "Toca para abrir la ubicación en Google Maps"
                } else {
                    "Agrega MAPS_API_KEY en local.properties para ver el mapa embebido"
                },
                style = MaterialTheme.typography.bodySmall,
                color = CoinbaseMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun resolveCoordinates(context: android.content.Context, location: String): LatLng? {
    if (location.isBlank()) return null
    return try {
        @Suppress("DEPRECATION")
        val geocoder = Geocoder(context, Locale.getDefault())
        @Suppress("DEPRECATION")
        val results = geocoder.getFromLocationName(location, 1)
        results?.firstOrNull()?.let { address ->
            LatLng(address.latitude, address.longitude)
        }
    } catch (_: Exception) {
        null
    }
}
