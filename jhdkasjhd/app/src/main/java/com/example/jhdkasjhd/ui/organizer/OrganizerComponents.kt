package com.example.jhdkasjhd.ui.organizer

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.jhdkasjhd.ui.theme.CoinbaseCanvas
import com.example.jhdkasjhd.ui.theme.CoinbaseInk
import com.example.jhdkasjhd.ui.theme.CoinbaseMuted
import com.example.jhdkasjhd.ui.theme.CoinbaseMutedSoft
import com.example.jhdkasjhd.ui.theme.CoinbasePrimary
import com.example.jhdkasjhd.ui.theme.CoinbasePrimaryActive
import com.example.jhdkasjhd.ui.theme.CoinbasePrimaryDisabled
import com.example.jhdkasjhd.ui.theme.CoinbaseRadiusPill
import com.example.jhdkasjhd.ui.theme.CoinbaseSemanticDown
import com.example.jhdkasjhd.ui.theme.CoinbaseSpacing
import com.example.jhdkasjhd.ui.theme.CoinbaseSurfaceSoft
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val CREATE_EVENT_HERO_ASSET = "file:///android_asset/12085253_20944068.svg"
private val CreateEventCoverButtonBg = Color(0xFFD6E4FF)

internal fun localDateToPickerMillis(date: LocalDate): Long {
    return date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

internal fun pickerMillisToLocalDate(millis: Long): LocalDate {
    return Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
}

internal fun formatCreateEventDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.forLanguageTag("es-PA"))
    return date.format(formatter).replaceFirstChar { it.titlecase(Locale.getDefault()) }
}

internal fun formatCreateEventTime(time: LocalTime): String {
    val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.forLanguageTag("es-PA"))
    return time.format(formatter).lowercase(Locale.getDefault())
}

internal fun toIsoDateTime(date: LocalDate, time: LocalTime): String {
    return ZonedDateTime.of(date, time, ZoneId.systemDefault()).toInstant().toString()
}

internal fun parseIsoToDate(iso: String?): LocalDate? {
    if (iso.isNullOrBlank()) return null
    return try {
        Instant.parse(iso).atZone(ZoneId.systemDefault()).toLocalDate()
    } catch (_: Exception) {
        null
    }
}

internal fun parseIsoToTime(iso: String?): LocalTime? {
    if (iso.isNullOrBlank()) return null
    return try {
        Instant.parse(iso).atZone(ZoneId.systemDefault()).toLocalTime()
    } catch (_: Exception) {
        null
    }
}

@Composable
internal fun CreateEventTopBar(
    onClose: () -> Unit,
    onDone: () -> Unit,
    doneEnabled: Boolean,
    isSaving: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CoinbaseSpacing.xs, vertical = CoinbaseSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = CoinbaseInk
            )
        }

        Text(
            text = "Crear evento",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = CoinbaseInk
            ),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        TextButton(
            onClick = onDone,
            enabled = doneEnabled && !isSaving
        ) {
            Text(
                text = if (isSaving) "..." else "Listo",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (doneEnabled && !isSaving) {
                        CoinbasePrimary
                    } else {
                        CoinbasePrimaryDisabled
                    }
                )
            )
        }
    }
}

@Composable
internal fun CreateEventHeroSection(
    bannerUrl: String,
    onBannerClick: () -> Unit,
    modifier: Modifier = Modifier,
    showError: Boolean = false
) {
    val context = LocalContext.current
    val hasBanner = bannerUrl.isNotBlank()

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp))
                .background(CoinbaseSurfaceSoft)
                .clickable(onClick = onBannerClick)
        ) {
            if (hasBanner) {
                AsyncImage(
                    model = bannerUrl.trim(),
                    contentDescription = "Banner del evento",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(CREATE_EVENT_HERO_ASSET)
                        .decoderFactory(SvgDecoder.Factory())
                        .build(),
                    contentDescription = "Ilustración del evento",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            CreateEventCoverPhotoButton(
                text = if (hasBanner) "Cambiar foto" else "Foto de portada",
                onClick = onBannerClick,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (showError) {
            CreateEventFieldError(
                message = if (bannerUrl.isBlank()) {
                    "Agrega la URL del banner en la foto de portada"
                } else {
                    "Ingresa una URL válida (http o https)"
                }
            )
        }
    }
}

@Composable
private fun CreateEventCoverPhotoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CoinbaseRadiusPill)
            .background(CreateEventCoverButtonBg)
            .clickable(onClick = onClick)
            .padding(horizontal = CoinbaseSpacing.md, vertical = CoinbaseSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = null,
            tint = CoinbasePrimaryActive,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = CoinbasePrimaryActive
            )
        )
    }
}

@Composable
internal fun CreateEventBannerUrlDialog(
    currentUrl: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var url by remember(currentUrl) { mutableStateOf(currentUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Foto de portada",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xs)) {
                Text(
                    text = "Pega la URL de la imagen que quieres mostrar como banner del evento.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CoinbaseMuted
                )
                CreateEventFilledField(
                    value = url,
                    onValueChange = { url = it },
                    placeholder = "https://ejemplo.com/imagen.jpg",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(url.trim())
                    onDismiss()
                },
                enabled = url.trim().isNotBlank()
            ) {
                Text("Aceptar", color = CoinbasePrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = CoinbaseMuted)
            }
        }
    )
}

@Composable
internal fun CreateEventNameField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    showError: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth()) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                color = CoinbaseInk,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp
            ),
            singleLine = true,
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = "*Título del evento*",
                            style = TextStyle(
                                color = CoinbaseMutedSoft,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                                lineHeight = 34.sp
                            )
                        )
                    }
                    inner()
                }
            }
        )
        if (showError && value.isBlank()) {
            CreateEventFieldError("El título es obligatorio")
        }
    }
}

@Composable
internal fun CreateEventDateTimeRow(
    label: String,
    dateLabel: String,
    timeLabel: String,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDateError: Boolean = false,
    showTimeError: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.sm)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = CoinbaseInk
                ),
                modifier = Modifier.width(108.dp)
            )

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(CoinbaseSpacing.xs)
            ) {
                CreateEventPill(
                    text = dateLabel,
                    isPlaceholder = dateLabel == "Fecha",
                    onClick = onDateClick,
                    modifier = Modifier.weight(1f),
                    showError = showDateError
                )
                CreateEventPill(
                    text = timeLabel,
                    isPlaceholder = timeLabel == "Hora",
                    onClick = onTimeClick,
                    modifier = Modifier.weight(1f),
                    showError = showTimeError
                )
            }
        }
        if (showDateError) {
            CreateEventFieldError("La fecha es obligatoria")
        } else if (showTimeError) {
            CreateEventFieldError("La hora es obligatoria")
        }
    }
}

@Composable
internal fun CreateEventPill(
    text: String,
    isPlaceholder: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showError: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (showError) CoinbaseSemanticDown.copy(alpha = 0.08f) else CoinbaseSurfaceSoft)
            .clickable(onClick = onClick)
            .padding(horizontal = CoinbaseSpacing.sm, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isPlaceholder) FontWeight.Normal else FontWeight.Medium,
                color = when {
                    showError -> CoinbaseSemanticDown
                    isPlaceholder -> CoinbaseMutedSoft
                    else -> CoinbaseInk
                }
            ),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
internal fun CreateEventFilledField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minHeight: androidx.compose.ui.unit.Dp = 56.dp,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    showError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(minHeight)
                .clip(RoundedCornerShape(16.dp))
                .background(if (showError) CoinbaseSemanticDown.copy(alpha = 0.08f) else CoinbaseSurfaceSoft)
                .padding(horizontal = CoinbaseSpacing.base, vertical = CoinbaseSpacing.sm)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    color = CoinbaseInk,
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                ),
                singleLine = singleLine,
                keyboardOptions = keyboardOptions,
                decorationBox = { inner ->
                    Box {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = CoinbaseMutedSoft
                            )
                        }
                        inner()
                    }
                }
            )
        }
        if (showError && !errorMessage.isNullOrBlank()) {
            CreateEventFieldError(errorMessage)
        }
    }
}

@Composable
internal fun CreateEventFieldError(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = CoinbaseSemanticDown,
        modifier = Modifier.padding(top = CoinbaseSpacing.xxs, start = 4.dp)
    )
}

@Composable
internal fun CreateEventSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = CoinbaseInk
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateEventDatePickerDialog(
    initialDate: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.let { localDateToPickerMillis(it) }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = datePickerState.selectedDateMillis ?: return@TextButton
                onConfirm(pickerMillisToLocalDate(millis))
                onDismiss()
            }) {
                Text("Aceptar", color = CoinbasePrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = CoinbaseMuted)
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateEventTimePickerDialog(
    initialTime: LocalTime?,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime?.hour ?: 12,
        initialMinute = initialTime?.minute ?: 0,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(LocalTime.of(timePickerState.hour, timePickerState.minute))
                onDismiss()
            }) {
                Text("Aceptar", color = CoinbasePrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = CoinbaseMuted)
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}
